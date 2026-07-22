package com.levi.aeroweights;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    /** When the added weight line is allowed to appear. */
    public enum Display {
        /** Whenever the tooltip is shown. */
        ALWAYS,
        /** Only while holding shift. */
        SHIFT,
        /** Never - effectively disables the mod. */
        NEVER
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<Display> DISPLAY = BUILDER
            .comment("When to show the weight line this mod adds.",
                    "Weights that Create: Aeronautics already prints itself are never duplicated,",
                    "so this only controls the ones it leaves out.")
            .defineEnum("display", Display.ALWAYS);

    public static final ModConfigSpec.BooleanValue SHOW_VALUE = BUILDER
            .comment("Append the exact number, e.g. 'Normal (1 kpg)' instead of just 'Normal'.")
            .define("showValue", true);

    public static final ModConfigSpec.BooleanValue SHOW_WEIGHTLESS = BUILDER
            .comment("Show a line for blocks that weigh nothing at all (torches, plants, rails...).")
            .define("showWeightless", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
