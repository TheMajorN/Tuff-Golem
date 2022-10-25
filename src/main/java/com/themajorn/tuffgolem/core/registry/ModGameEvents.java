package com.themajorn.tuffgolem.core.registry;

import net.minecraft.core.Registry;
import net.minecraft.world.level.gameevent.GameEvent;

public class ModGameEvents {

    public static final GameEvent ENTITY_ANIMATE = register("entity_animate");

    private static GameEvent register(String p_157823_) {
        return register(p_157823_, 16);
    }

    private static GameEvent register(String p_157825_, int p_157826_) {
        return Registry.register(Registry.GAME_EVENT, p_157825_, new GameEvent(p_157825_, p_157826_));
    }

}
