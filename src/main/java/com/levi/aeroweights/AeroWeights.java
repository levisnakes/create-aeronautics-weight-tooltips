package com.levi.aeroweights;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fills in the block weights that Create: Aeronautics (Simulated) leaves off the item tooltip.
 *
 * <p>Simulated only prints a mass line when the block's Sable mass differs from the default of
 * 1 kpg, and only when its own {@code displayProperties} condition is satisfied (goggles by
 * default). Everything else silently shows nothing, which makes it impossible to tell "this block
 * weighs the normal amount" apart from "this block has no weight data". This mod adds the missing
 * line and never duplicates one Simulated already wrote.
 */
@Mod(value = AeroWeights.MOD_ID, dist = Dist.CLIENT)
public class AeroWeights {

    public static final String MOD_ID = "aeroweights";
    public static final Logger LOGGER = LoggerFactory.getLogger("AeroWeights");

    public AeroWeights(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        // LOWEST so Create: Aeronautics has already written its own lines by the time we look for
        // one - otherwise we would not see it and would add a duplicate.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, WeightTooltip::onItemTooltip);
    }
}
