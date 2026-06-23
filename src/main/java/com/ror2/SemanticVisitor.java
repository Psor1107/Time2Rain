package com.ror2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ror2.parser.RoR2BaseVisitor;
import com.ror2.parser.RoR2Parser;

public class SemanticVisitor extends RoR2BaseVisitor<Void> {
    private final List<String> errors = new ArrayList<>();
    private final Set<String> artifacts = new HashSet<>();
    private final Map<String, Integer> items = new HashMap<>();
    private int equipmentCount = 0;
    private String survivorName;
    private String difficulty;

    public List<String> getErrors() {
        return errors;
    }

    public String getSurvivorName() {
        return survivorName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public Set<String> getArtifacts() {
        return artifacts;
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
    public Void visitItemLine(RoR2Parser.ItemLineContext ctx) {
        int amount = Integer.parseInt(ctx.INTEGER().getText());
        String name = ctx.IDENTIFIER().getText();
        if (!Database.isValidItem(name)) {
            errors.add("Erro Semântico: Item não reconhecido - " + name);
        } else {
            items.merge(name, amount, Integer::sum);
            if (Database.toItem(name).isEquipment()) {
                equipmentCount += amount;
            }
        }
        return null;
    }

    @Override
    public Void visitProgram(RoR2Parser.ProgramContext ctx) {
        super.visitProgram(ctx);

        if (survivorName == null) {
            errors.add("Erro Semântico: Run deve conter Survivor");
        }
        if (difficulty == null) {
            errors.add("Erro Semântico: Run deve conter Difficulty");
        }
        if (items.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            errors.add("Erro Semântico: Run deve conter pelo menos um item");
        }
        if (items.keySet().stream().filter(item -> Database.toItem(item).isEquipment()).count() > 1) {
            errors.add("Erro Semântico: Apenas 1 item do tipo Equipamento é permitido");
        }
        return null;
    }
}
