package edu.kit.imi.knoholem.cu.rules.rulesconversion;

import edu.kit.imi.knoholem.cu.rules.ontology.OntologyContext;
import edu.kit.imi.knoholem.cu.rules.parser.RuleParser;
import edu.kit.imi.knoholem.cu.rules.parser.RuleParserConfiguration;
import edu.kit.imi.knoholem.cu.rules.swrlentities.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class SWRLRuleConverterTest {

    private OntologyContext ontologyContext;
    @Before
    public void setup() throws URISyntaxException, OWLOntologyCreationException {
        Path ontologyPath = Paths.get(getClass().getResource("/ontology.n3").toURI());

        ontologyContext = OntologyContext.load(ontologyPath.toFile());
    }
    @Test
    public void testFiltersRepeatingAtoms() {
        String ruleLiteral = "IF ZoneID= ZONE ^ Weight= 0.00 ^ Type= Tot_Cool_Reduc ^ Reduction= 5.00% ^ 6>= 14.31 ^ 6<= 15.04 THEN Temperature_Set= 16.09";
        List<Atom> expectedAntecedent = new ArrayList<Atom>();
        expectedAntecedent.add(new ClassAtom("Sensor", new Individual("6")));
        expectedAntecedent.add(new PropertyAtom("hasAnalogValue", new Individual("6"), new Unknown("a")));
        expectedAntecedent.add(new SWRLBuiltIn("greaterThanOrEqual", new Unknown("a"), new Value("14.31")));
        expectedAntecedent.add(new SWRLBuiltIn("lessThanOrEqual", new Unknown("a"), new Value("15.04")));
        SWRLConverter converter = new SWRLConverter(SWRLConverterConfiguration.getDefaultConfiguration());
        RuleParser parser = new RuleParser(RuleParserConfiguration.getDefaultConfiguration());
        assertArrayEquals(expectedAntecedent.toArray(), converter.convertRule(parser.parseRule(ruleLiteral)).getAntecedent().toArray());
    }
}
