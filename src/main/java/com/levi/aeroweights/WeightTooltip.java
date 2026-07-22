package com.levi.aeroweights;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Appends the weight line to block tooltips that are missing one.
 *
 * <p>Wording, colours, thresholds and number formatting are copied from Create: Aeronautics so the
 * added lines are indistinguishable from the ones it writes itself. The one tier it has no name for
 * - exactly 1 kpg, the default - is called "Normal" here.
 */
final class WeightTooltip {

    /** Red used by Simulated for the two heaviest tiers. */
    private static final int HEAVY_RED = 0xFF7171;

    private static final String UNIT_KEY = "simulated.unit.mass";
    private static final String FALLBACK_UNIT_KEY = "aeroweights.unit.mass";

    /** Ordered heaviest-last; index matches {@link #FALLBACK_LABEL_KEYS}. */
    private static final String[] LABEL_KEYS = {
            "simulated.tooltip.mass.none",
            "simulated.tooltip.mass.super_light",
            "simulated.tooltip.mass.light",
            "aeroweights.tooltip.mass.normal",
            "simulated.tooltip.mass.heavy",
            "simulated.tooltip.mass.super_heavy",
            "simulated.tooltip.mass.absurdly_heavy",
    };

    private static final String[] FALLBACK_LABEL_KEYS = {
            "aeroweights.tooltip.mass.none",
            "aeroweights.tooltip.mass.super_light",
            "aeroweights.tooltip.mass.light",
            "aeroweights.tooltip.mass.normal",
            "aeroweights.tooltip.mass.heavy",
            "aeroweights.tooltip.mass.super_heavy",
            "aeroweights.tooltip.mass.absurdly_heavy",
    };

    private static final DecimalFormat FORMAT = new DecimalFormat();

    static {
        FORMAT.setDecimalSeparatorAlwaysShown(false);
        FORMAT.setMaximumFractionDigits(2);
        FORMAT.setMinimumIntegerDigits(1);
    }

    /** Resolved label text, cached until the game language changes. */
    private static String[] labelCache;
    private static String labelCacheLanguage;

    private WeightTooltip() {
    }

    static void onItemTooltip(ItemTooltipEvent event) {
        Config.Display display = Config.DISPLAY.get();
        if (display == Config.Display.NEVER) {
            return;
        }
        if (display == Config.Display.SHIFT && !Screen.hasShiftDown()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        List<Component> lines = event.getToolTip();
        if (alreadyShowsWeight(lines)) {
            return;
        }

        Double mass = SablePhysics.massOf(blockItem.getBlock().defaultBlockState());
        if (mass == null) {
            return;
        }
        if (mass <= 0.0D && !Config.SHOW_WEIGHTLESS.get()) {
            return;
        }

        lines.add(Component.literal("").append(buildLine(mass)));
    }

    private static MutableComponent buildLine(double mass) {
        int tier = tierOf(mass);
        MutableComponent line = Component.empty().append(labelComponent(tier));
        if (!Config.SHOW_VALUE.get()) {
            return line;
        }
        MutableComponent value = Component.literal(" (")
                .append(Component.translatable(unitKey(), FORMAT.format(mass)))
                .append(")")
                .withStyle(ChatFormatting.DARK_GRAY);
        return line.append(value);
    }

    /** Mirrors Simulated's thresholds, with the 1 kpg default it skips slotted in at index 3. */
    private static int tierOf(double mass) {
        if (mass <= 0.0D) {
            return 0;
        }
        if (mass <= 0.25D) {
            return 1;
        }
        if (mass <= 0.5D) {
            return 2;
        }
        if (mass == 1.0D) {
            return 3;
        }
        if (mass < 4.0D) {
            return 4;
        }
        return mass < 50.0D ? 5 : 6;
    }

    private static MutableComponent labelComponent(int tier) {
        MutableComponent label = Component.translatable(labelKey(tier));
        return switch (tier) {
            case 0 -> label.withStyle(ChatFormatting.GRAY);
            case 1 -> label.withStyle(ChatFormatting.AQUA);
            case 2 -> label.withStyle(ChatFormatting.GREEN);
            case 3 -> label.withStyle(ChatFormatting.WHITE);
            case 4 -> label.withStyle(ChatFormatting.YELLOW);
            default -> label.withColor(HEAVY_RED);
        };
    }

    private static String labelKey(int tier) {
        String key = LABEL_KEYS[tier];
        return I18n.exists(key) ? key : FALLBACK_LABEL_KEYS[tier];
    }

    private static String unitKey() {
        return I18n.exists(UNIT_KEY) ? UNIT_KEY : FALLBACK_UNIT_KEY;
    }

    /**
     * True when Create: Aeronautics already wrote a mass line for this item, which it does as
     * "&lt;tier&gt; (&lt;n&gt; kpg)". Matching on the translated tier names rather than the unit keeps
     * the "Floating (n kpg)" line - which shares the unit - from being mistaken for one.
     */
    private static boolean alreadyShowsWeight(List<Component> lines) {
        String[] labels = labels();
        for (Component line : lines) {
            String text = line.getString();
            if (text.isEmpty()) {
                continue;
            }
            for (String label : labels) {
                if (!label.isEmpty() && (text.equals(label) || text.startsWith(label + " ("))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] labels() {
        String language = I18n.get("language.code");
        if (labelCache == null || !language.equals(labelCacheLanguage)) {
            String[] resolved = new String[LABEL_KEYS.length];
            for (int i = 0; i < LABEL_KEYS.length; i++) {
                String key = LABEL_KEYS[i];
                resolved[i] = I18n.exists(key) ? I18n.get(key) : "";
            }
            labelCache = resolved;
            labelCacheLanguage = language;
        }
        return labelCache;
    }
}
