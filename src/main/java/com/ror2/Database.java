package com.ror2;

import java.util.List;

// Alguns valores mockados, porem em sua maioria com base nos dados reais
// ou uma tentativa de simula-los. Alguns valores podem ser ajustados posteriormente para melhor refletir a realidade do jogo.

public final class Database {
    public enum Survivor {
        Commando(12.0),
        Huntress(15.0);

        private final double baseDps;

        Survivor(double baseDps) {
            this.baseDps = baseDps;
        }

        public double getBaseDps() {
            return baseDps;
        }
    }

    public enum Difficulty {
        Drizzle(0.5),
        Rainstorm(1.0),
        Monsoon(1.5);

        private final double multiplier;

        Difficulty(double multiplier) {
            this.multiplier = multiplier;
        }

        public double getMultiplier() {
            return multiplier;
        }
    }

    public enum Enemy {
        LesserWisp(35),
        Beetle(80),
        Lemurian(80),
        Imp(140),
        Golem(480),
        GreaterWisp(500),
        ElderLemurian(900),
        Parent(900);

        private final int baseHp;

        Enemy(int baseHp) {
            this.baseHp = baseHp;
        }

        public int getBaseHp() {
            return baseHp;
        }
    }

    public enum Item {
        LensMakersGlasses("LensMakersGlasses"),
        AtGMissileMk1("AtGMissileMk1"),
        LeafClover("LeafClover"),
        ExecutiveCard("ExecutiveCard");

        private final String displayName;

        Item(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isEquipment() {
            return this == ExecutiveCard;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static boolean isValidSurvivor(String name) {
        for (Survivor survivor : Survivor.values()) {
            if (survivor.name().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidDifficulty(String name) {
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty.name().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidItem(String name) {
        for (Item item : Item.values()) {
            if (item.getDisplayName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidArtifact(String name) {
        return "Swarms".equalsIgnoreCase(name) || "Command".equalsIgnoreCase(name);
    }

    public static Survivor toSurvivor(String name) {
        for (Survivor survivor : Survivor.values()) {
            if (survivor.name().equalsIgnoreCase(name)) {
                return survivor;
            }
        }
        throw new IllegalArgumentException("Survivor inválido: " + name);
    }

    public static Difficulty toDifficulty(String name) {
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty.name().equalsIgnoreCase(name)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Difficulty inválida: " + name);
    }

    public static Item toItem(String name) {
        for (Item item : Item.values()) {
            if (item.getDisplayName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Item inválido: " + name);
    }

    public static List<Enemy> getEnemiesForStage(int stage) {
        int cycle = ((stage - 1) % 5) + 1;
        return switch (cycle) {
            case 1 -> List.of(Enemy.Beetle, Enemy.Lemurian, Enemy.Golem);
            case 2 -> List.of(Enemy.Golem, Enemy.Lemurian, Enemy.LesserWisp);
            case 3 -> List.of(Enemy.Lemurian, Enemy.LesserWisp, Enemy.Imp);
            case 4 -> List.of(Enemy.Imp, Enemy.ElderLemurian, Enemy.GreaterWisp);
            case 5 -> List.of(Enemy.ElderLemurian, Enemy.GreaterWisp, Enemy.Parent);
            default -> List.of(Enemy.Beetle, Enemy.Lemurian, Enemy.Golem);
        };
    }
}
