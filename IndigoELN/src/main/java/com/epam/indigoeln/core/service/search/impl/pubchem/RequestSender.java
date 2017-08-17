package com.epam.indigoeln.core.service.search.impl.pubchem;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.chemistry.sdf.SdUnit;
import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.sd.SDService;
import com.epam.indigoeln.core.service.search.impl.pubchem.dto.CidsDTO;
import com.epam.indigoeln.core.service.search.impl.pubchem.dto.Structure;
import com.epam.indigoeln.core.service.search.impl.pubchem.dto.WaitingDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.ScalarValueDTO;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.impl.pubchem.PubChemConst.*;

@Component
public class RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);

    private static final RestTemplate restTemplate;
    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(TIMEOUT);
        factory.setConnectTimeout(TIMEOUT);
        restTemplate = new RestTemplate(factory);
    }

    private final SDService sdService;

    private final CalculationService calculationService;

    @Autowired
    public RequestSender(SDService sdService, CalculationService calculationService) {
        this.sdService = sdService;
        this.calculationService = calculationService;
    }

    public Collection<ProductBatchDetailsDTO> sendRequest(RequestEntity requestEntity){
        WaitingDTO waitingDTO = sendAsyncRequest(requestEntity);
        return sendCheckRequest(waitingDTO);
    }

    private WaitingDTO sendAsyncRequest(RequestEntity requestEntity){
        ResponseEntity<WaitingDTO> response = restTemplate.exchange(requestEntity, WaitingDTO.class);
        if (response.getStatusCode() == HttpStatus.ACCEPTED){
            return response.getBody();
        }else {
            throw new IndigoRuntimeException("PubChem answered with " + response.getStatusCode() + "status code");
        }
    }

    private Collection<ProductBatchDetailsDTO> sendCheckRequest(WaitingDTO waitingDTO){
        URI uri = new UriTemplate(GET_BY_KEY_URI).expand(waitingDTO.getListKey());
        RequestEntity<Void> requestEntity = RequestEntity.get(uri).build();
        ResponseEntity<CidsDTO> response = restTemplate.exchange(requestEntity, CidsDTO.class);
        long time = 0;
        while (response.getStatusCode() != HttpStatus.OK){
            if (time > TIMEOUT){
                throw new IndigoRuntimeException("PubChem timeout exceed exception");
            }
            try {
                Thread.sleep(DELAY);
                time += DELAY;
            } catch (InterruptedException e) {
                LOGGER.error("PubChem sleep exception",e);
            }
            response = restTemplate.exchange(requestEntity, CidsDTO.class);
        }
        //parallel stream can't be used here because of async request. If request is interrupted rest template continues to work
        return Lists.partition(response.getBody().getCids(), CHUNK_COUNT).stream()
                .flatMap(cids -> getByCids(cids).stream())
                .collect(Collectors.toList());
    }

    private Collection<ProductBatchDetailsDTO> getByCids(List<String> cids){
        if (!cids.isEmpty()) {
            Optional<String> value = cids.stream().reduce((cid1, cid2) -> cid1 + "," + cid2);
            URI uri = new UriTemplate(GET_BY_CIDS_URI).expand();

            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add(CID_PARAM,value.get());

            RequestEntity<LinkedMultiValueMap<String, String>> requestEntity = RequestEntity.post(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params);

            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();
                try (InputStream is = new ByteArrayInputStream(body.getBytes())){
                    try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        Collection<SdUnit> parse = sdService.parse(r);
                        return parse.stream().map(this::convert).collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    throw new IndigoRuntimeException("Error occurred while parsing SD file.", e);
                }
            }
        }
        return Collections.emptyList();
    }

    private ProductBatchDetailsDTO convert(SdUnit sdUnit){
        HashMap<String, Object> properties = new HashMap<>();

        Structure structure = new Structure();
        structure.setImage(calculationService.getStructureWithImage(sdUnit.getMol()).getImage());
        structure.setMolfile(sdUnit.getMol());

        properties.put("structure",structure);
        properties.put("compoundId",sdUnit.getInfoPortion().get(COMPOUND_CID));
        properties.put("formula",sdUnit.getInfoPortion().get(MOLECULAR_FORMULA));
        properties.put("chemicalName",sdUnit.getInfoPortion().get(IUPAC_CAS_NAME));
        String weight = sdUnit.getInfoPortion().get(MOLECULAR_WEIGHT);
        properties.put("molWeight",new ScalarValueDTO(Double.valueOf(weight), null, false, false));

        ProductBatchDetailsDTO productBatchDetailsDTO = new ProductBatchDetailsDTO();
        productBatchDetailsDTO.setDetails(properties);
        return productBatchDetailsDTO;
    }
}
