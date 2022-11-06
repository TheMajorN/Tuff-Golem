package com.themajorn.tuffgolem.common.ai.sensors;

import com.google.common.collect.ImmutableSet;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.decoration.ItemFrame;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NearestItemFrameSensor extends Sensor<Mob> {
    private static final long XZ_RANGE = 32L;
    private static final long Y_RANGE = 16L;
    public static final int MAX_DISTANCE_TO_WANTED_ITEM = 32;

    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get());
    }

    protected void doTick(ServerLevel level, Mob mob) {
        Brain<?> brain = mob.getBrain();
        List<ItemFrame> list = level.getEntitiesOfClass(ItemFrame.class, mob.getBoundingBox().inflate(32.0D, 16.0D, 32.0D), (entity) -> {
            return true;
        });
        list.sort(Comparator.comparingDouble(mob::distanceToSqr));
        Optional<ItemFrame> optional = list.stream().filter((itemFrame) -> {
            if (!itemFrame.getItem().isEmpty()) {
                return mob.wantsToPickUp(itemFrame.getItem());
            }
                return !mob.wantsToPickUp(itemFrame.getItem());
            })
            .filter((itemFrame) -> {
                if (!itemFrame.getItem().isEmpty()) {
                    return itemFrame.closerThan(mob, 32.0D);
                }
                return false;
                })
                .filter(mob::hasLineOfSight).findFirst();
        brain.setMemory(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get(), optional);
    }
}
