package com.ror2;

import com.ror2.parser.RoR2BaseVisitor;
import com.ror2.parser.RoR2Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InterpreterVisitor extends RoR2BaseVisitor<String> {
    private String survivorName;
    private String difficultyName;
    private int time;
    private int stage;
    private final Set<String> artifacts;
    private final Map<String, Integer> items = new HashMap<>();

    public InterpreterVisitor(Set<String> artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public String visitSurvivorStatement(RoR2Parser.SurvivorStatementContext ctx) {
        survivorName = ctx.IDENTIFIER().getText();
        return null;
    }

    @Override
    public String visitDifficultyStatement(RoR2Parser.DifficultyStatementContext ctx) {
        difficultyName = ctx.IDENTIFIER().getText();
        return null;
    }

    @Override
    public String visitTimeStatement(RoR2Parser.TimeStatementContext ctx) {
        time = Integer.parseInt(ctx.INTEGER().getText());
        return null;
    }

    @Override
    public String visitStageStatement(RoR2Parser.StageStatementContext ctx) {
        stage = Integer.parseInt(ctx.INTEGER().getText());
        return null;
    }

    @Override
    public String visitItemLine(RoR2Parser.ItemLineContext ctx) {
        int amount = Integer.parseInt(ctx.INTEGER().getText());
        String name = ctx.IDENTIFIER().getText();
        items.merge(name, amount, Integer::sum);
        return null;
    }

    @Override
    public String visitProgram(RoR2Parser.ProgramContext ctx) {
        super.visitProgram(ctx);

        Database.Survivor survivor = Database.toSurvivor(survivorName);
        Database.Difficulty difficulty = Database.toDifficulty(difficultyName);

        double baseDps = survivor.getBaseDps();
        double syringeMultiplier = 1.0 + items.getOrDefault("SoldierSyringe", 0) * 0.15;
        double glassesMultiplier = 1.0 + items.getOrDefault("LensMakersGlasses", 0) * 0.10;
        double behemothMultiplier = items.containsKey("BrilliantBehemoth") ? 1.6 : 1.0;
        double playerDps = baseDps * syringeMultiplier * glassesMultiplier * behemothMultiplier;

        List<String> lines = new ArrayList<>();
        lines.add("Run Report");
        lines.add("==========");
        lines.add("Survivor: " + survivor.name());
        lines.add("Difficulty: " + difficulty.name());
        lines.add("Time: " + time);
        lines.add("Stage: " + stage);
        lines.add("Artifacts: " + artifacts);
        lines.add("Items: " + items);
        lines.add("");
        lines.add(String.format("Player DPS: %.2f", playerDps));
        lines.add("");
        lines.add("Enemy TTK Summary:");
        lines.add("------------------");

        for (Database.Enemy enemy : Database.Enemy.values()) {
            double enemyHp = enemy.getBaseHp() * (1 + (stage * 0.3) + (time * 0.05 * difficulty.getMultiplier()));
            if (artifacts.stream().anyMatch(a -> a.equalsIgnoreCase("Swarms"))) {
                enemyHp /= 2.0;
            }
            double ttk = enemyHp / playerDps;
            lines.add(String.format("%s: %.2fs", enemy.name(), ttk));
        }

        return String.join(System.lineSeparator(), lines);
    }
}
