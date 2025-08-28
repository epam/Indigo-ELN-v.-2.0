package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.epam.indigoeln.eln.util.CustomAssertions.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.LISA_USERNAME)
class SignatureTemplateServiceTest extends BaseTest {

    List<SignatureBlock> blocks;

    @BeforeAll
    void setUpAll() {
        blocks = List.of(new SignatureBlock(testHelper.getBartUserRef(), SignatureReason.WITNESS), new SignatureBlock(null, SignatureReason.AUTHOR));
    }

    @Test
    void testCreateSignatureTemplateValidation() {
        assertThatClientCall(() -> signatureClient.createSignatureTemplate(new SignatureTemplateRequest(null, List.of())))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateSignatureTemplate() {
        SignatureTemplateDetailsDTO signatureTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("testCreateSignatureTemplate", blocks));
        assertThat(signatureTemplate.getId()).isNotNull();
        assertThat(signatureTemplate.getName()).isEqualTo("testCreateSignatureTemplate");
        assertThat(signatureTemplate.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
        assertThat(signatureTemplate.getCreatedAt()).isNotNull();
        assertThat(signatureTemplate.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
        assertThat(signatureTemplate.getModifiedAt()).isNotNull();
        assertThat(signatureTemplate.getBlocks()).isEqualTo(blocks);
    }

    @Test
    void testGetSignatureTemplate() {
        SignatureTemplateDetailsDTO createdSignatureTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("testGetSignatureTemplate", blocks));
        SignatureTemplateDetailsDTO loadedSignatureTemplate = signatureClient.getSignatureTemplate(createdSignatureTemplate.getId());
        assertThat(loadedSignatureTemplate).usingRecursiveComparison().isEqualTo(createdSignatureTemplate);
    }

    @Test
    void testGetSignatureTemplates() {
        signatureClient.createSignatureTemplate(new SignatureTemplateRequest("testGetSignatureTemplates", blocks));
        Page<SignatureTemplateDTO> signatureTemplates = signatureClient.getSignatureTemplates(Paging.DEFAULT);
        assertThat(signatureTemplates.getItems()).first().satisfies(signatureTemplate -> {
            assertThat(signatureTemplate.getId()).isNotNull();
            assertThat(signatureTemplate.getName()).isEqualTo("testGetSignatureTemplates");
            assertThat(signatureTemplate.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
            assertThat(signatureTemplate.getCreatedAt()).isNotNull();
            assertThat(signatureTemplate.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
            assertThat(signatureTemplate.getModifiedAt()).isNotNull();
        });
    }
    
    @Test
    void testEditSignatureTemplate() {
        SignatureTemplateDetailsDTO signatureTemplate = signatureClient.createSignatureTemplate(new SignatureTemplateRequest("testEditSignatureTemplate", blocks));
        SignatureTemplateDetailsDTO notModified = signatureClient.editSignatureTemplate(signatureTemplate.getId(), new SignatureTemplateEditRequest(null, null));
        assertThat(notModified).usingRecursiveComparison(TestHelper.COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(signatureTemplate);
        List<SignatureBlock> newBlocks = blocks.reversed();
        SignatureTemplateDetailsDTO modified = signatureClient.editSignatureTemplate(signatureTemplate.getId(), new SignatureTemplateEditRequest(Optional.of("testEditSignatureTemplate_new"), Optional.of(newBlocks)));
        assertThat(modified.getName()).isEqualTo("testEditSignatureTemplate_new");
        assertThat(modified.getBlocks()).isEqualTo(newBlocks);
        SignatureTemplateDetailsDTO saved = signatureClient.getSignatureTemplate(signatureTemplate.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }
}
