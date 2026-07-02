package com.ror2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ror2.parser.RoR2BaseVisitor;
import com.ror2.parser.RoR2Parser;

/**
 * Interpreta e executa o programa, acumulando itens e computando
 * a taxa de morte (TTK) dos inimigos baseado em sobrevivente, dificuldade e estágio.
 */
public class InterpreterVisitor extends RoR2BaseVisitor<Void> {
    private SymbolTable symbolTable;
    private String survivorName;
    private String difficultyName;
    private int time;
    private int stage;
    private Set<String> artifacts;
    private Map<String, Integer> itemCount;

    public InterpreterVisitor(SemanticVisitor semanticVisitor) {
        this.symbolTable = semanticVisitor.getSymbolTable();
        this.survivorName = semanticVisitor.getSurvivorName();
        this.difficultyName = semanticVisitor.getDifficulty();
        this.time = semanticVisitor.getTime();
        this.stage = semanticVisitor.getStage();
        this.artifacts = semanticVisitor.getArtifacts();
        this.itemCount = new HashMap<>();
    }

    @Override
    public Void visitProgram(RoR2Parser.ProgramContext ctx) {
        // Executar a declaração de Run
        visit(ctx.runDeclaration());
        return null;
    }

    @Override
    public Void visitRunDeclaration(RoR2Parser.RunDeclarationContext ctx) {
        return visit(ctx.runBody());
    }

    @Override
    public Void visitRunBody(RoR2Parser.RunBodyContext ctx) {
        for (RoR2Parser.RunStatementContext stmt : ctx.runStatement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Void visitSurvivorStatement(RoR2Parser.SurvivorStatementContext ctx) {
        // Já preenchido do SemanticVisitor
        return null;
    }

    @Override
    public Void visitDifficultyStatement(RoR2Parser.DifficultyStatementContext ctx) {
        // Já preenchido do SemanticVisitor
        return null;
    }

    @Override
    public Void visitTimeStatement(RoR2Parser.TimeStatementContext ctx) {
        // Já preenchido do SemanticVisitor
        return null;
    }

    @Override
    public Void visitStageStatement(RoR2Parser.StageStatementContext ctx) {
        // Já preenchido do SemanticVisitor
        return null;
    }

    @Override
    public Void visitArtifactsStatement(RoR2Parser.ArtifactsStatementContext ctx) {
        // Já preenchido do SemanticVisitor
        return null;
    }

    @Override
    public Void visitVariableDeclaration(RoR2Parser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        double value = evaluateExpression(ctx.expression());
        
        symbolTable.declare(varName, Variable.Type.NUMERIC, value);
        
        return null;
    }

    @Override
    public Void visitAssignment(RoR2Parser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        double value = evaluateExpression(ctx.expression());
        symbolTable.assign(varName, value);
        return null;
    }

    @Override
    public Void visitIfStatement(RoR2Parser.IfStatementContext ctx) {
        boolean condition = evaluateLogicalExpression(ctx.logicalExpression());
        
        if (condition) {
            symbolTable.pushScope();
            
            for (RoR2Parser.BlockStatementContext stmt : ctx.blockStatement()) {
                visit(stmt);
            }
            
            symbolTable.popScope();
        }
        
        return null;
    }

    @Override
    public Void visitItemsBlock(RoR2Parser.ItemsBlockContext ctx) {
        for (RoR2Parser.ItemLineContext line : ctx.itemLine()) {
            visit(line);
        }
        return null;
    }

    @Override
    public Void visitItemLine(RoR2Parser.ItemLineContext ctx) {
        int amount = (int) evaluateExpression(ctx.expression());
        String name = ctx.IDENTIFIER().getText();
        itemCount.merge(name, amount, Integer::sum);
        return null;
    }

    /**
     * Gera o relatório final com cálculos de TTK (Time To Kill).
     */
    public String generateReport() {
        Database.Survivor survivor = Database.toSurvivor(survivorName);
        Database.Difficulty difficulty = Database.toDifficulty(difficultyName);

        double baseDps = survivor.getBaseDps();
        int lensCount = itemCount.getOrDefault("LensMakersGlasses", 0);
        int missileCount = itemCount.getOrDefault("AtGMissileMk1", 0);
        boolean hasClover = itemCount.containsKey("LeafClover");

        // Bônus de crítico (LensMakersGlasses)
        double lensBonus = Math.min(1.0, lensCount * 0.10);
        
        // Bônus de míssil (AtG Missile)
        double missileBonusPer = missileCount > 0 && hasClover ? 0.30 : 0.15;
        double missileMultiplier = 1.0 + missileCount * missileBonusPer;
        
        // DPS final
        double playerDps = baseDps * (1.0 + lensBonus) * missileMultiplier;

        List<String> lines = new ArrayList<>();
        lines.add("╔════════════════════════════════════════════════════════╗");
        lines.add("║              RISK OF RAIN 2 - RUN REPORT               ║");
        lines.add("╚════════════════════════════════════════════════════════╝");
        lines.add("");
        lines.add("┌─ RUN CONFIGURATION ─┐");
        lines.add(String.format("  Survivor:   %s", survivor.name()));
        lines.add(String.format("  Difficulty: %s", difficulty.name()));
        lines.add(String.format("  Time:       %d minutos", time));
        lines.add(String.format("  Stage:      %d", stage));
        if (!artifacts.isEmpty()) {
            lines.add(String.format("  Artifacts:  %s", artifacts));
        }
        lines.add("");
        lines.add("┌─ INVENTORY ─┐");
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            lines.add(String.format("  %sx %s", entry.getValue(), entry.getKey()));
        }
        lines.add("");
        lines.add("┌─ DPS CALCULATION ─┐");
        lines.add(String.format("  Base DPS:           %.2f", baseDps));
        lines.add(String.format("  Crit Bonus (Lens):  +%.1f%%", lensBonus * 100));
        lines.add(String.format("  Missile Bonus:      +%.1f%%", (missileMultiplier - 1.0) * 100));
        lines.add("  ─────────────────────────────");
        lines.add(String.format("  Final DPS:          %.2f", playerDps));
        lines.add("");
        lines.add("┌─ ENEMY TTK (Time To Kill) ─┐");
        lines.add("");

        for (Database.Enemy enemy : Database.getEnemiesForStage(stage)) {
            double enemyHp = enemy.getBaseHp() * (1.0 + ((stage - 1) * 0.15));
            enemyHp *= difficulty.getMultiplier();
            if (artifacts.stream().anyMatch(a -> a.equalsIgnoreCase("Swarms"))) {
                enemyHp /= 2.0;
            }
            double ttk = playerDps > 0 ? enemyHp / playerDps : Double.POSITIVE_INFINITY;
            lines.add(String.format("  %-20s: %7.2fs", enemy.name(), ttk));
        }
        
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Avalia uma expressão matemática.
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
                }
            }
        }
        
        return result;
    }

    private double evaluateUnaryExpr(RoR2Parser.UnaryExprContext ctx) {
        double value = evaluatePrimaryExpr(ctx.primaryExpr());
        
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
     * Avalia uma expressão lógica.
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
            return left != 0;
        }
        
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
}
