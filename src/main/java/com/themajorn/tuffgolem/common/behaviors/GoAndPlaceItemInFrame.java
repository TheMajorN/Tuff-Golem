package com.themajorn.tuffgolem.common.behaviors;

import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class GoAndPlaceItemInFrame<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {

    private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 2;
    private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;

    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;

    public GoAndPlaceItemInFrame(Function<LivingEntity, Optional<PositionTracker>> function, float i) {
        super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.REGISTERED));
        this.targetPositionGetter = function;
    }

    private boolean canPlaceItemInTarget(E e) {
        if (e.getMainHandItem().isEmpty()) {
            return false;
        } else {
            Optional<PositionTracker> optional = this.targetPositionGetter.apply(e);
            return optional.isPresent();
        }
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel serverLevel, @NotNull E e) {
        return this.canPlaceItemInTarget(e);
    }

    protected boolean canStillUse(@NotNull ServerLevel serverLevel, @NotNull E e, long i) {
        return this.canPlaceItemInTarget(e);
    }

    public static void placeItem(LivingEntity entity, ItemStack itemStack, ItemFrame itemFrame) {
        entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        itemFrame.setItem(itemStack);
        Level level = entity.level;
        if (level.getGameTime() % 7L == 0L && level.random.nextDouble() < 0.9D) {
            float f = Util.getRandom(Allay.THROW_SOUND_PITCHES, level.getRandom());
            level.playSound((Player)null, entity, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 1.0F, f);
        }
    }


}
