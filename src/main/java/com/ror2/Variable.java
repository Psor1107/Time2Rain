package com.ror2;

/**
 * Representa uma variável na tabela de símbolos.
 */
public class Variable {
    public enum Type {
        NUMERIC,   // double
        LOGIC      // boolean
    }

    private String name;
    private Type type;
    private double numericValue;
    private boolean logicValue;

    public Variable(String name, Type type) {
        this.name = name;
        this.type = type;
        this.numericValue = 0.0;
        this.logicValue = false;
    }

    public Variable(String name, Type type, double initialValue) {
        this.name = name;
        this.type = type;
        this.numericValue = initialValue;
        this.logicValue = false;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public double getValue() {
        return numericValue;
    }

    public void setValue(double value) {
        this.numericValue = value;
    }

    public boolean getBoolValue() {
        return logicValue;
    }

    public void setBoolValue(boolean value) {
        this.logicValue = value;
    }

    @Override
    public String toString() {
        return String.format("Variable{name='%s', type=%s, value=%.0f}", name, type, numericValue);
    }
}
