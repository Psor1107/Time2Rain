package com.ror2;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa um escopo em uma linguagem de programação.
 * Um escopo pode ter um escopo pai (para suportar escopos aninhados).
 */
public class Scope {
    private Scope parent;
    private Map<String, Variable> variables;

    public Scope(Scope parent) {
        this.parent = parent;
        this.variables = new HashMap<>();
    }

    /**
     * Declara uma variável no escopo atual.
     * @return true se declarou, false se já existia no escopo local
     */
    public boolean declare(String name, Variable var) {
        if (variables.containsKey(name)) {
            return false; // Variável já declarada neste escopo
        }
        variables.put(name, var);
        return true;
    }

    /**
     * Procura uma variável no escopo atual e nos escopos pai.
     */
    public Variable lookup(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null; // Não encontrada
    }

    /**
     * Verifica se uma variável existe no escopo atual ou nos escopos pai.
     */
    public boolean isDeclared(String name) {
        return lookup(name) != null;
    }

    /**
     * Atualiza o valor de uma variável (deve já estar declarada).
     * @return true se conseguiu atualizar, false se não existia
     */
    public boolean assign(String name, double value) {
        Variable var = lookup(name);
        if (var != null) {
            var.setValue(value);
            return true;
        }
        return false;
    }

    /**
     * Atualiza o valor de uma variável booleana.
     */
    public boolean assignBool(String name, boolean value) {
        Variable var = lookup(name);
        if (var != null) {
            var.setBoolValue(value);
            return true;
        }
        return false;
    }

    /**
     * Obtém o valor de uma variável.
     */
    public double getValue(String name) {
        Variable var = lookup(name);
        if (var != null) {
            return var.getValue();
        }
        return 0.0; // Default
    }

    /**
     * Obtém o valor booleano de uma variável.
     */
    public boolean getBoolValue(String name) {
        Variable var = lookup(name);
        if (var != null) {
            return var.getBoolValue();
        }
        return false;
    }

    public Scope getParent() {
        return parent;
    }
}
