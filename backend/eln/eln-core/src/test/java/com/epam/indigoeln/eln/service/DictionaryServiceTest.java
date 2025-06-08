package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.model.DictionaryDTO;
import com.epam.indigoeln.eln.model.DictionaryRef;
import com.epam.indigoeln.eln.model.DictionaryRequest;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.JOHN_USERNAME)
public class DictionaryServiceTest extends BaseTest {

    @BeforeAll
    @SneakyThrows
    void setUpClass() {
        testHelper.cleanupDatabase();
        testHelper.createTestUsers();
    }

    @Test
    @Order(1)
    void testGetDictionaries() {
        assertThat(miscClient.getDictionaries()).containsExactly(Dictionary.values());
    }

    @Test
    @Order(2)
    void testGetDictionaryFull() {
        List<DictionaryDTO> original = miscClient.getDictionaryFull(Dictionary.THERAPEUTIC_AREA);
        assertThat(original).isNotEmpty();
    }

    @Test
    @Order(3)
    void testUpdateDictionary() {
        List<DictionaryDTO> list1 = miscClient.updateDictionary(Dictionary.THERAPEUTIC_AREA, List.of(
                new DictionaryRequest(null, "A", null, false)
        ));
        List<DictionaryDTO> list2 = miscClient.updateDictionary(Dictionary.THERAPEUTIC_AREA, List.of(
                new DictionaryRequest(null, "0", null, null),
                new DictionaryRequest(list1.getFirst().getId(), "Anew", null, null),
                new DictionaryRequest(null, "B", null, null)
        ));
        assertThat(list2).map(DictionaryDTO::getName).containsExactly("0", "Anew", "B");
        assertThat(list2.get(1).getId()).isEqualTo(list1.getFirst().getId());
    }

    @Test
    @Order(4)
    void testGetDictionary() {
        List<DictionaryRef> list = miscClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        var names = assertThat(list).map(DictionaryRef::getName);
        names.containsExactly("0", "Anew", "B");
    }

    @Test
    @Order(999)
    void testRestoreOriginal() {
        // it should be a tearDown method, but authorization is not available in tearDown, so use regular test
        miscClient.updateDictionary(Dictionary.THERAPEUTIC_AREA, List.of(
                new DictionaryRequest(null, "A", null, false),
                new DictionaryRequest(null, "B", null, false)
        ));
        List<DictionaryDTO> result = miscClient.getDictionaryFull(Dictionary.THERAPEUTIC_AREA);
        assertThat(result).isNotEmpty();
    }
}
