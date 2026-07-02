package com.ror2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ror2.parser.RoR2BaseVisitor;
import com.ror2.parser.RoR2Parser;

/**
 * Analisador semântico que valida o programa.
 * Usa tabela de símbolos para rastrear variáveis.
 * Coleta erros semânticos (variáveis não declaradas, tipos incompatíveis, etc).
 */
public class SemanticVisitor extends RoR2BaseVisitor<Void> {
    private List<String> errors;
    private List<String> warnings;
    private SymbolTable symbolTable;

    private String survivorName;
    private String difficulty;
    private int time;
    private int stage;
    private Set<String> artifacts;
    private Map<String, Integer> items;

    public SemanticVisitor() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        this.survivorName = "";
        this.difficulty = "";
        this.time = 0;
        this.stage = 0;
        this.artifacts = new HashSet<>();
        this.items = new HashMap<>();
    }

    @Override
    public Void visitProgram(RoR2Parser.ProgramContext ctx) {
        return visitRunDeclaration(ctx.runDeclaration());
    }

    @Override
    public Void visitRunDeclaration(RoR2Parser.RunDeclarationContext ctx) {
        return visitRunBody(ctx.runBody());
    }

    @Override
    public Void visitRunBody(RoR2Parser.RunBodyContext ctx) {
        // Inicializar com valores padrão (serão atualizados se encontrados)
        symbolTable.initializeBuiltins(1, 0);
        
        for (RoR2Parser.RunStatementContext stmt : ctx.runStatement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Void visitSurvivorStatement(RoR2Parser.SurvivorStatementContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (!Database.isValidSurvivor(name)) {
            errors.add("Erro Semântico: Sobrevivente não reconhecido - " + name);
        } else {
            survivorName = name;
        }
        return null;
    }

    @Override
    public Void visitDifficultyStatement(RoR2Parser.DifficultyStatementContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (!Database.isValidDifficulty(name)) {
            errors.add("Erro Semântico: Dificuldade não reconhecida - " + name);
        } else {
            difficulty = name;
        }
        return null;
    }

    @Override
    public Void visitTimeStatement(RoR2Parser.TimeStatementContext ctx) {
        time = Integer.parseInt(ctx.INTEGER().getText());
        symbolTable.assign("Time", (double) time);
        return null;
    }

    @Override
    public Void visitStageStatement(RoR2Parser.StageStatementContext ctx) {
        stage = Integer.parseInt(ctx.INTEGER().getText());
        symbolTable.assign("Stage", (double) stage);
        return null;
    }

    @Override
    public Void visitArtifactsStatement(RoR2Parser.ArtifactsStatementContext ctx) {
        if (ctx.artifactList() != null) {
            for (var token : ctx.artifactList().IDENTIFIER()) {
                String artifactName = token.getText();
                if (!Database.isValidArtifact(artifactName)) {
                    errors.add("Erro Semântico: Artefato não reconhecido - " + artifactName);
                } else if (!artifacts.add(artifactName)) {
                    errors.add("Erro Semântico: Artefato duplicado - " + artifactName);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitVariableDeclaration(RoR2Parser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        double value = evaluateExpression(ctx.expression());

        // Tenta declarar na tabela. Se retornar false, a variável já existe neste escopo!
        boolean declarou = symbolTable.declare(varName, Variable.Type.NUMERIC, value);
        if (!declarou) {
            errors.add("Erro: Variável '" + varName + "' já foi declarada neste escopo.");
            return null;
        }

        items.put(varName, (int) value);
        return null;
    }

    @Override
    public Void visitAssignment(RoR2Parser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        
        // Verificar se a variável foi declarada
        if (!symbolTable.isDeclared(varName)) {
            errors.add("Erro: Variável '" + varName + "' não foi declarada antes de ser reatribuída.");
            return null;
        }

        // Avaliar expressão
        double value = evaluateExpression(ctx.expression());
        
        // Atualizar variável
        symbolTable.assign(varName, value);
        items.put(varName, (int) value);
        
        return null;
    }

    @Override
    public Void visitIfStatement(RoR2Parser.IfStatementContext ctx) {
        // Apenas avalia a expressão para validar as variáveis usadas nela
        evaluateLogicalExpression(ctx.logicalExpression());
        
        // SEMPRE entra no escopo para checar erros semânticos internos
        symbolTable.pushScope();
        for (RoR2Parser.BlockStatementContext stmt : ctx.blockStatement()) {
            visit(stmt);
        }
        symbolTable.popScope();
        
        return null;
    }

    @Override
    public Void visitItemsBlock(RoR2Parser.ItemsBlockContext ctx) {
        double totalEquipments = 0;

        for (RoR2Parser.ItemLineContext line : ctx.itemLine()) {
            visit(line); // Checa se o item é válido e exibe os avisos (warnings)
            
            String itemName = line.IDENTIFIER().getText();
            
            // Só faz a checagem se o item realmente existir no Banco de Dados
            if (Database.isValidItem(itemName)) {
                // Pergunta pro Database se esse item é um equipamento
                if (Database.toItem(itemName).isEquipment()) {
                    totalEquipments += evaluateExpression(line.expression());
                }
            }
        }
        
        if (totalEquipments > 1) {
            errors.add("Erro: Permitido apenas 1 item de Equipamento (ex: ExecutiveCard), mas encontrados " + (long)totalEquipments + ".");
        }
        
        return null;
    }

    @Override
    public Void visitItemLine(RoR2Parser.ItemLineContext ctx) {
        double quantity = evaluateExpression(ctx.expression());
        String itemName = ctx.IDENTIFIER().getText();
        
        if (!Database.isValidItem(itemName)) {
            errors.add("Erro Semântico: Item não reconhecido - " + itemName);
            return null;
        }
        
        if ("LensMakersGlasses".equals(itemName) && quantity > 10) {
            warnings.add("Aviso: Limite de 10 LensMakersGlasses para 100% de chance de crítico. Você tem " + (int)quantity + ".");
        }
        
        return null;
    }

    /**
     * Avalia uma expressão matemática, validando que todas as variáveis estão declaradas.
     */
    private double evaluateExpression(RoR2Parser.ExpressionContext ctx) {
        double result = evaluateMultiplicativeExpr(ctx.multiplicativeExpr(0));
        
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            String op = ctx.getChild(i * 2 - 1).getText();
            double right = evaluateMultiplicativeExpr(ctx.multiplicativeExpr(i));
            
            if ("+".equals(op)) {
                result += right;
            } else if ("-".equals(op)) {
                result -= right;
            }
        }
        
        return result;
    }

    private double evaluateMultiplicativeExpr(RoR2Parser.MultiplicativeExprContext ctx) {
        double result = evaluateUnaryExpr(ctx.unaryExpr(0));
        
        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            String op = ctx.getChild(i * 2 - 1).getText();
            double right = evaluateUnaryExpr(ctx.unaryExpr(i));
            
            if ("*".equals(op)) {
                result *= right;
            } else if ("/".equals(op)) {
                if (right != 0) {
                    result /= right;
                } else {
                    errors.add("Erro: Divisão por zero.");
                    return 0;
                }
            }
        }
        
        return result;
    }

    private double evaluateUnaryExpr(RoR2Parser.UnaryExprContext ctx) {
        double value = evaluatePrimaryExpr(ctx.primaryExpr());
        
        // Verificar se há operador unário
        if (ctx.getChildCount() > 1) {
            String op = ctx.getChild(0).getText();
            if ("-".equals(op)) {
                value = -value;
            }
        }
        
        return value;
    }

    private double evaluatePrimaryExpr(RoR2Parser.PrimaryExprContext ctx) {
        if (ctx.INTEGER() != null) {
            return Double.parseDouble(ctx.INTEGER().getText());
        } else if (ctx.IDENTIFIER() != null) {
            String varName = ctx.IDENTIFIER().getText();
            if (this instanceof SemanticVisitor && !symbolTable.isDeclared(varName)) {
                errors.add("Erro: Variável '" + varName + "' não foi declarada.");
                return 0;
            }
            
            return symbolTable.getValue(varName);
        } else if (ctx.expression() != null) {
            return evaluateExpression(ctx.expression());
        } else if (ctx.getText().equals("Stage")) {
            return symbolTable.getValue("Stage");
        } else if (ctx.getText().equals("Time")) {
            return symbolTable.getValue("Time");
        }
        return 0;
    }

    /**
     * Avalia uma expressão lógica, validando tipos.
     */
    private boolean evaluateLogicalExpression(RoR2Parser.LogicalExpressionContext ctx) {
        return evaluateLogicalOrExpr(ctx.logicalOrExpr());
    }

    private boolean evaluateLogicalOrExpr(RoR2Parser.LogicalOrExprContext ctx) {
        boolean result = evaluateLogicalAndExpr(ctx.logicalAndExpr(0));
        
        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            result = result || evaluateLogicalAndExpr(ctx.logicalAndExpr(i));
        }
        
        return result;
    }

    private boolean evaluateLogicalAndExpr(RoR2Parser.LogicalAndExprContext ctx) {
        boolean result = evaluateRelationalExpr(ctx.relationalExpr(0));
        
        for (int i = 1; i < ctx.relationalExpr().size(); i++) {
            result = result && evaluateRelationalExpr(ctx.relationalExpr(i));
        }
        
        return result;
    }

    private boolean evaluateRelationalExpr(RoR2Parser.RelationalExprContext ctx) {
        double left = evaluateExpression(ctx.expression(0));
        
        if (ctx.expression().size() == 1) {
            // Sem operador relacional
            return left != 0;
        }
        
        // Há um operador relacional
        String op = ctx.getChild(1).getText();
        double right = evaluateExpression(ctx.expression(1));
        
        switch (op) {
            case "==":
                return left == right;
            case "!=":
                return left != right;
            case "<":
                return left < right;
            case ">":
                return left > right;
            case "<=":
                return left <= right;
            case ">=":
                return left >= right;
            default:
                return false;
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public String getSurvivorName() {
        return survivorName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getTime() {
        return time;
    }

    public int getStage() {
        return stage;
    }

    public Set<String> getArtifacts() {
        return artifacts;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
