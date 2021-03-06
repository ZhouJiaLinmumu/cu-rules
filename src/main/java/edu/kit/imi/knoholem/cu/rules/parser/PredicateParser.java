package edu.kit.imi.knoholem.cu.rules.parser;

class PredicateParser {

    private final String literal;
    private final int indexOfOperator;

    PredicateParser(String literal) {
        if (indexOfOperator(literal) < 0) {
            throw new IllegalArgumentException("No operator in predicate + \"" + literal + "\"");
        }

        this.literal = literal.trim();
        this.indexOfOperator = indexOfOperator(this.literal);
    }

    public String getLeftLiteral() {
        return literal.substring(0, indexOfOperator).trim();
    }

    public String getOperator() {
        StringBuilder operator = new StringBuilder();

        operator.append(literal.charAt(indexOfOperator));
        if (literal.charAt(indexOfOperator + 1) == '=') {
            operator.append(literal.charAt(indexOfOperator + 1));
        }

        return operator.toString();
    }

    public String getRightLiteral() {
        return literal.substring(indexOfOperator + getOperator().length()).trim();
    }

    @Override
    public String toString() {
        return "PredicateParser{" +
                "literal='" + literal + '\'' +
                ", indexOfOperator=" + indexOfOperator +
                '}';
    }

    private int indexOfOperator(String literal) {
        for (int i = 0; i < literal.length(); i++) {
            if (isPartOfOperator(literal, i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isPartOfOperator(String literal, int index) {
        return literal.charAt(index) == '>' || literal.charAt(index) == '<' || literal.charAt(index) == '=';
    }

}
