package HardCoreEnd.random;

public final class FXType{
    public enum Basic{
        ESSENCE_ALTAR_SMOKE,
        LASER_BEAM_DESTROY,
        SPOOKY_LOG_DECAY,
        SPOOKY_LEAVES_DECAY,
        DUNGEON_PUZZLE_BURN,
        DRAGON_EGG_RESET,
        GEM_LINK,
        GEM_TELEPORT_TO,
        ENDER_PEARL_FREEZE,
        IGNEOUS_ROCK_MELT,
        ENDERMAN_BLOODLUST_TRANSFORMATION,
        LOUSE_ARMOR_HIT,
        HOMELAND_ENDERMAN_TP_OVERWORLD,
        FIRE_FIEND_FLAME_ATTACK,
        SHRINE_GLITTER;

        public static FXType.Basic[] values = values();
    }

    public enum Entity{
        CHARM_CRITICAL,
        CHARM_WITCH,
        CHARM_BLOCK_EFFECT,
        CHARM_LAST_RESORT,
        GEM_TELEPORT_FROM,
        ORB_TRANSFORMATION,
        LOUSE_REGEN,
        HOMELAND_ENDERMAN_RECRUIT,
        BABY_ENDERMAN_GROW,
        ENDER_GUARDIAN_DASH,
        SANCTUARY_OVERSEER_SINGLE,
        SIMPLE_TELEPORT,
        SIMPLE_TELEPORT_NOSOUND,
        ENTITY_EXPLOSION_PARTICLE,
        ENDER_EYE_BREAK,
        ENDERMAN_DESPAWN,
        ENDERMAN_TP_FAIL;

        public static FXType.Entity[] values = values();
    }

    public enum Line{
        DRAGON_EGG_TELEPORT,
        SPATIAL_DASH_MOVE,
        CHARM_SLAUGHTER_IMPACT,
        CHARM_DAMAGE_REDIRECTION,
        LOUSE_HEAL_ENTITY,
        ENDERMAN_TELEPORT,
        ENDERMAN_TELEPORT_SEPARATE,
        DUNGEON_PUZZLE_TELEPORT,
        FIRE_FIEND_GOLEM_CALL,
        SANCTUARY_OVERSEER_FULL;

        public static FXType.Line[] values = values();
    }
}
