package com.themajorn.tuffgolem.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.themajorn.tuffgolem.common.ai.TuffGolemAi;
import com.themajorn.tuffgolem.core.registry.ModActivities;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TuffGolemEntity extends AbstractGolem implements IAnimatable, InventoryCarrier {
    protected static final ImmutableList<SensorType<? extends Sensor<? super TuffGolemEntity>>> SENSOR_TYPES =
            (ImmutableList<SensorType<? extends Sensor<? super TuffGolemEntity>>>) ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY,
            MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
            MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS,
            MemoryModuleType.RAM_TARGET, MemoryModuleType.IS_PANICKING);
    private AnimationFactory factory = new AnimationFactory(this);
    public TuffGolemEntity(EntityType<? extends AbstractGolem> entityType, Level level) { super(entityType, level); }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    protected Brain.@NotNull Provider<TuffGolemEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        return TuffGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public static void updateActivity(TuffGolemEntity tuffGolem) {
        tuffGolem.getBrain().setActiveActivityToFirstValid(ImmutableList.of(ModActivities.ANIMATE,
                                                                            ModActivities.PICK_OUT,
                                                                            Activity.IDLE,
                                                                            ModActivities.PUT_BACK,
                                                                            ModActivities.PETRIFY));
    }

    public @NotNull Brain<TuffGolemEntity> getBrain() {
        return (Brain<TuffGolemEntity>)super.getBrain();
    }

    protected void customServerAiStep() {
        this.level.getProfiler().push("tuffGolemBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("tuffGolemActivityUpdate");
        TuffGolemEntity.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    public boolean canPickUpLoot() {
        return this.hasItemInHand();
    }

    public boolean hasItemInHand() {
        return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
    }

    public boolean canTakeItem(@NotNull ItemStack stack) {
        return false;
    }

    protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemInPlayerHand = player.getItemInHand(hand);
        ItemStack itemInTuffGolemHand = this.getItemInHand(InteractionHand.MAIN_HAND);

        if (itemInTuffGolemHand.isEmpty() && !itemInPlayerHand.isEmpty()) {
            ItemStack playerItemCopy = itemInPlayerHand.copy();
            playerItemCopy.setCount(1);
            this.setItemInHand(InteractionHand.MAIN_HAND, playerItemCopy);
            this.removeInteractionItem(player, itemInPlayerHand);
            this.level.playSound(player, this, SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.getBrain().setMemory(ModMemoryModules.PLAYER_WHO_GAVE_ITEM, player.getUUID());
            return InteractionResult.SUCCESS;
        } else if (!itemInTuffGolemHand.isEmpty() && hand == InteractionHand.MAIN_HAND && itemInPlayerHand.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.level.playSound(player, this, SoundEvents.ANVIL_STEP, SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.swing(InteractionHand.MAIN_HAND);

            for(ItemStack itemClearStack : this.getInventory().removeAllItems()) {
                BehaviorUtils.throwItem(this, itemClearStack, this.position());
            }

            this.getBrain().eraseMemory(ModMemoryModules.PLAYER_WHO_GAVE_ITEM);
            player.addItem(itemInTuffGolemHand);
            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, hand);
        }
    }

    private void removeInteractionItem(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }

    @Override
    public SimpleContainer getInventory() {
        return null;
    }
}
