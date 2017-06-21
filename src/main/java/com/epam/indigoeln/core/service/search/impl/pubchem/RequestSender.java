package com.epam.indigoeln.core.service.search.impl.pubchem;

import com.chemistry.enotebook.utils.sdf.SdUnit;
import com.epam.indigoeln.IndigoRuntimeException;
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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.impl.pubchem.PubChemConst.*;

@Component
public class RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);

    private static RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private SDService sdService;

    @Autowired
    private CalculationService calculationService;

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
        //TODO SET TIMEOUT
        return Lists.partition(response.getBody().getCids(), CHUNK_COUNT).stream()
                .parallel()
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
                try {
                    Collection<SdUnit> parse = sdService.parse(new ByteArrayInputStream(body.getBytes()));
                    return parse.stream().map(this::convert).collect(Collectors.toList());
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
        structure.setImage(calculationService.getStructureWithImage(sdUnit.getMol(),"molecule").getImage());
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
