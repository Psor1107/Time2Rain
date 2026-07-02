package com.ror2;

/**
 * Gerencia a tabela de símbolos do programa.
 * Suporta escopos aninhados para controle de blocos (if, etc).
 */
public class SymbolTable {
    private Scope globalScope;
    private Scope currentScope;

    public SymbolTable() {
        this.globalScope = new Scope(null);
        this.currentScope = this.globalScope;
    }

    /**
     * Inicializa as variáveis built-in como Stage e Time.
     */
    public void initializeBuiltins(int stage, int time) {
        globalScope.declare("Stage", new Variable("Stage", Variable.Type.NUMERIC, stage));
        globalScope.declare("Time", new Variable("Time", Variable.Type.NUMERIC, time));
    }

    /**
     * Entra em um novo escopo (ex: dentro de um if).
     */
    public void pushScope() {
        currentScope = new Scope(currentScope);
    }

    /**
     * Sai do escopo atual voltando ao escopo pai.
     */
    public void popScope() {
        if (currentScope.getParent() != null) {
            currentScope = currentScope.getParent();
        }
    }

    /**
     * Declara uma variável no escopo atual.
     * @return true se conseguiu, false se já existia
     */
    public boolean declare(String name, Variable.Type type) {
        return currentScope.declare(name, new Variable(name, type));
    }

    /**
     * Declara uma variável com valor inicial.
     */
    public boolean declare(String name, Variable.Type type, double value) {
        return currentScope.declare(name, new Variable(name, type, value));
    }

    /**
     * Procura uma variável no escopo atual e superiores.
     */
    public Variable lookup(String name) {
        return currentScope.lookup(name);
    }

    /**
     * Verifica se uma variável foi declarada.
     */
    public boolean isDeclared(String name) {
        return currentScope.isDeclared(name);
    }

    /**
     * Atribui um valor a uma variável (deve estar declarada).
     */
    public boolean assign(String name, double value) {
        return currentScope.assign(name, value);
    }

    /**
     * Atribui um valor lógico a uma variável.
     */
    public boolean assignBool(String name, boolean value) {
        return currentScope.assignBool(name, value);
    }

    /**
     * Obtém o valor numérico de uma variável.
     */
    public double getValue(String name) {
        return currentScope.getValue(name);
    }

    /**
     * Obtém o valor lógico de uma variável.
     */
    public boolean getBoolValue(String name) {
        return currentScope.getBoolValue(name);
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }
}
