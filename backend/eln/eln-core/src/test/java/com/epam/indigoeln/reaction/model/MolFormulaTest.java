package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class MolFormulaTest extends ELNBaseTest {

    @Inject
    IndigoAPI indigo;

    @ParameterizedTest
    @CsvSource(textBlock = """
            water,O,H2O,H<sub>2</sub>O
            glucose,C([C@H]([C@H]([C@H]([C@H](C=O)O)O)O)O)O,C6H12O6,C<sub>6</sub>H<sub>12</sub>O<sub>6</sub>
            calcium hydroxide,[OH-].[OH-].[Ca+2],CaH2O2,CaH<sub>2</sub>O<sub>2</sub>
            aluminum sulfate,[O-]S(=O)(=O)[O-].[O-]S(=O)(=O)[O-].[O-]S(=O)(=O)[O-].[Al+3].[Al+3],Al2O12S3,Al<sub>2</sub>O<sub>12</sub>S<sub>3</sub>
            acetic acid,CC(=O)O,C2H4O2,C<sub>2</sub>H<sub>4</sub>O<sub>2</sub>
            iron oxide,O=[Fe]O[Fe]=O,Fe2O3,Fe<sub>2</sub>O<sub>3</sub>
            ammonium carbonate,C(=O)([O-])[O-].[NH4+].[NH4+],CH8N2O3,CH<sub>8</sub>N<sub>2</sub>O<sub>3</sub>
            sodium ion,[Na+],Na,Na
            """)
    void testFormatMolFormula(String name, String smiles, String expectedIndigo, String expectedFormatted) {
        IndigoMolecule molecule = indigo.loadMolecule(smiles);
        String grossFormula = molecule.molecularFormula();
        assertThat(grossFormula).isEqualTo(expectedIndigo);
        String formatted = new MolFormula(molecule.molecularFormula()).toHTMLString();
        assertThat(formatted).isEqualTo(expectedFormatted);
    }

    @Test
    void testFormatOldFormulaWithSpaces() {
        String formatted = new MolFormula("C6 H12 O6").toHTMLString();
        assertThat(formatted).isEqualTo("C<sub>6</sub>H<sub>12</sub>O<sub>6</sub>");
    }

    @Test
    void testFormatHTMLFormula() {
        String formatted = new MolFormula("C<sub>6</sub>H<sub>12</sub>O<sub>6</sub>").toHTMLString();
        assertThat(formatted).isEqualTo("C<sub>6</sub>H<sub>12</sub>O<sub>6</sub>");
    }

    @ParameterizedTest
    @CsvSource({"''", "'6C'", "'C6!'", "'C6Xx!'", "'C6,H12'"})
    void testRejectsInvalidFormula(String formula) {
        assertThatThrownBy(() -> new MolFormula(formula))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid formula");
    }
}
