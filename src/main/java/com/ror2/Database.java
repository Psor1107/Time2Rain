package com.ror2;

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
        Beetle(80),
        Lemurian(120),
        Golem(480);

        private final int baseHp;

        Enemy(int baseHp) {
            this.baseHp = baseHp;
        }

        public int getBaseHp() {
            return baseHp;
        }
    }

    public enum Item {
        SoldierSyringe,
        LensMakersGlasses,
        BrilliantBehemoth;

        public boolean isEquipment() {
            return this == BrilliantBehemoth;
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
            if (item.name().equalsIgnoreCase(name)) {
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
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Item inválido: " + name);
    }
}
