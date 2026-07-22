package com.levi.aeroweights;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reads Sable's per-blockstate mass without compiling against Sable.
 *
 * <p>Sable is PolyForm Shield licensed, so its jar is never bundled or checked in; everything here
 * goes through reflection against classes the modpack already provides. If any lookup fails the
 * bridge disables itself and the mod simply adds no tooltip lines.
 */
final class SablePhysics {

    private static final String TYPES = "dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes";
    private static final String PROPERTY_TYPE = TYPES + "$PhysicsBlockPropertyType";
    private static final String STATE_EXTENSION = "dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension";
    private static final String REGISTRY_OBJECT = "foundry.veil.platform.registry.RegistryObject";

    /** {@code BlockStateExtension#sable$getProperty(PhysicsBlockPropertyType)}. */
    private static Method getProperty;
    /** The registered MASS property instance. */
    private static Object massType;
    /** {@code BlockBehaviour#hasCollision}, mirroring how Simulated zeroes out pass-through blocks. */
    private static Field hasCollision;

    private static boolean initialised;
    private static boolean available;

    private SablePhysics() {
    }

    /**
     * @return the block's mass in kpg, or {@code null} when Sable is missing or not ready yet
     */
    static Double massOf(BlockState state) {
        if (!init()) {
            return null;
        }
        try {
            if (!hasCollision(state)) {
                return 0.0D;
            }
            Object mass = getProperty.invoke(state, massType);
            return mass instanceof Number number ? number.doubleValue() : null;
        } catch (Throwable t) {
            AeroWeights.LOGGER.debug("Failed to read Sable mass for {}", state, t);
            return null;
        }
    }

    private static boolean hasCollision(BlockState state) throws IllegalAccessException {
        // Simulated treats blocks you can walk through as weightless regardless of their Sable mass.
        return hasCollision == null || hasCollision.getBoolean(state.getBlock());
    }

    private static synchronized boolean init() {
        if (initialised) {
            return available;
        }
        initialised = true;
        try {
            Class<?> propertyType = Class.forName(PROPERTY_TYPE);
            Class<?> extension = Class.forName(STATE_EXTENSION);
            getProperty = extension.getMethod("sable$getProperty", propertyType);

            Object massHolder = Class.forName(TYPES).getField("MASS").get(null);
            massType = Class.forName(REGISTRY_OBJECT).getMethod("get").invoke(massHolder);

            hasCollision = resolveHasCollision();
            available = massType != null;
        } catch (Throwable t) {
            AeroWeights.LOGGER.info("Sable block properties unavailable, weight tooltips disabled ({})", t.toString());
            available = false;
        }
        return available;
    }

    private static Field resolveHasCollision() {
        try {
            Field field = BlockBehaviour.class.getDeclaredField("hasCollision");
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            // Not fatal - we just lose the "walk-through blocks are weightless" nuance.
            AeroWeights.LOGGER.debug("Could not read BlockBehaviour#hasCollision", t);
            return null;
        }
    }
}
