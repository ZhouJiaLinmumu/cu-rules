package edu.kit.imi.knoholem.cu.rules.rulesconversion;

import edu.kit.imi.knoholem.cu.rules.atoms.Literal;
import edu.kit.imi.knoholem.cu.rules.atoms.Operator;
import edu.kit.imi.knoholem.cu.rules.atoms.Predicate;
import edu.kit.imi.knoholem.cu.rules.atoms.SensitivityAnalysisRule;
import edu.kit.imi.knoholem.cu.rules.atoms.processing.PredicateMap;
import edu.kit.imi.knoholem.cu.rules.atoms.processing.PredicateMapEntry;
import edu.kit.imi.knoholem.cu.rules.swrlentities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts sensitivity analysis rules from the syntactical in the SWRL representation.
 */
public class SWRLConverter {

    private SWRLConverterConfiguration configuration;

    public SWRLConverter(SWRLConverterConfiguration configuration) {
        this.configuration = configuration;
    }

    public SWRLRule convertRule(SensitivityAnalysisRule rule) {
        try {
            Unknowns unknowns = new Unknowns();
            List<Atom> antecedent = collectAntecedent(rule.getAntecedent(), unknowns);
            List<Atom> consequent = collectConsequent(rule.getConsequent(), unknowns);

            SWRLRule swrlRule = new SWRLRule();
            swrlRule.setAntecedent(antecedent);
            swrlRule.setConsequent(consequent);
            swrlRule.setMetadata(rule.getMetadata());
            return swrlRule;
        } catch (Exception e) {
            throw new SWRLConverterError(rule, e);
        }
    }

    private List<Atom> collectAntecedent(List<Predicate> inputAntecedent, Unknowns unknowns) {
        List<Atom> result = new ArrayList<Atom>();
        for (PredicateMapEntry entry : new PredicateMap(inputAntecedent).byLeftOperand()) {
            if (entry.isSingular()) {
                result.addAll(convertSensorValuePredicate(entry.getFirstPredicate(), unknowns));
            } else {
                result.addAll(convertSensorValuePredicates(entry, unknowns));
            }
        }
        return result;
    }

    private List<Atom> convertSensorValuePredicate(Predicate predicate, Unknowns unknowns) {
        List<Atom> result = new LinkedList<Atom>();

        String className = configuration.sensorClass(predicate);
        String individualName = predicate.getLeftOperand().asString();
        String propertyName = configuration.sensorValueProperty(predicate);

        if (className == null) {
            throw new IllegalArgumentException("Individual type not found: " + individualName);
        }

        result.add(new ClassAtom(className, new Individual(individualName)));

        Unknown unknown = unknowns.nextUnknown();
        result.addAll(createIndividualPropertyAssignment(individualName, propertyName, predicate.getOperator(), predicate.getRightOperand(), unknown));

        return result;
    }


    private Collection<? extends Atom> convertSensorValuePredicates(PredicateMapEntry predicates, Unknowns unknowns) {
        List<Atom> atoms = new LinkedList<Atom>();

        Individual individual = new Individual(predicates.getClassifier().asString());
        Unknown unknown = unknowns.nextUnknown();

        atoms.add(new ClassAtom(configuration.sensorClass(predicates.getFirstPredicate()), individual));
        atoms.add(new PropertyAtom(configuration.sensorValueProperty(predicates.getFirstPredicate()), individual, unknown));

        for (Predicate predicate : predicates.getPredicates()) {
            Value value = new Value(predicate.getRightOperand().asString());
            atoms.add(new SWRLBuiltIn(builtIn(predicate.getOperator()), unknown, value));
        }

        return atoms;
    }

    private List<Atom> collectConsequent(List<Predicate> consequentPredicates, Unknowns unknowns) {
        List<Atom> result = new ArrayList<Atom>();
        for (Predicate predicate : consequentPredicates) {
            result.addAll(convertConsequentPredicate(predicate, unknowns));
        }
        return result;
    }

    private List<Atom> convertConsequentPredicate(Predicate predicate, Unknowns unknowns) {
        String individualName = predicate.getLeftOperand().asString();
        String propertyName = configuration.sensorValueProperty(predicate);
        Operator operator = predicate.getOperator();
        Literal value = predicate.getRightOperand();

        return createIndividualPropertyAssignment(individualName, propertyName, operator, value, unknowns.nextUnknown());
    }

    private List<Atom> createIndividualPropertyAssignment(String individualName, String propertyName, Operator operator, Literal value, Unknown unknown) {
        List<Atom> result = new LinkedList<Atom>();

        Individual individual = new Individual(individualName);

        if (propertyName.equals(OntologySWRLConverterConfiguration.HAS_BINARY_VALUE)) {
            String binaryValue = value.asDouble().equals(0d) ? "false" : "true";
            result.add(new PropertyAtom(propertyName, individual, new Value(binaryValue)));
        } else {
            result.add(new PropertyAtom(propertyName, individual, unknown));
            result.add(new SWRLBuiltIn(builtIn(operator), unknown, new Value(value.asString())));
        }

        return result;
    }

    private String builtIn(Operator operator) {
        switch (operator) {
            case GREATER_THAN_OR_EQUAL:
                return SWRLExpression.SWRL_GREATER_THAN_OR_EQUAL;
            case GREATER_THAN:
                return SWRLExpression.SWRL_GREATER_THAN;
            case LESS_THAN_OR_EQUAL:
                return SWRLExpression.SWRL_LESS_THAN_OR_EQUAL;
            case LESS_THAN:
                return SWRLExpression.SWRL_LESS_THAN;
            case EQUAL:
                return SWRLExpression.SWRL_EQUAL;
            default:
                throw new RuntimeException();
        }
    }
}
