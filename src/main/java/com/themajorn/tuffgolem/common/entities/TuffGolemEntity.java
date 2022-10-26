package com.themajorn.tuffgolem.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.ai.TuffGolemAi;
import com.themajorn.tuffgolem.core.registry.ModGameEvents;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TuffGolemEntity extends AbstractGolem implements IAnimatable, InventoryCarrier {

    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_CLOAK_COLOR = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.INT);
    private final SimpleContainer inventory = new SimpleContainer(1);

    protected static final ImmutableList<SensorType<? extends Sensor<? super TuffGolemEntity>>> SENSOR_TYPES =
            ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
                            SensorType.NEAREST_PLAYERS,
                            SensorType.HURT_BY,
                            SensorType.NEAREST_ITEMS);

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES =
            ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

    private boolean isPetrified;
    public boolean hasCloak;
    private boolean isAnimating;
    private int age;
    public final float bobOffs;

    private AnimationFactory factory = new AnimationFactory(this);

    public TuffGolemEntity(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
        this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CLOAK_COLOR, DyeColor.WHITE.getId());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide && this.isPetrified && !this.isAnimating && !this.isPathFinding() && this.onGround) {
            this.isAnimating = true;
            this.level.broadcastEntityEvent(this, (byte)8);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive()) {
            /*
            if (this.isInWaterRainOrBubble() || this.isOnGround()) {
                this.isPetrified = true;
                if (this.isAnimating && !this.level.isClientSide) {
                    this.level.broadcastEntityEvent(this, (byte)56);
                    this.cancelAnimate();
                }
            } else if ((this.isPetrified || this.isAnimating) && this.isAnimating) {
                if (this.animateAnim == 0.0F) {
                    this.playSound(SoundEvents.ANVIL_USE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    this.gameEvent(ModGameEvents.ENTITY_ANIMATE);
                }

                this.animateAnim0 = this.animateAnim;
                this.animateAnim += 0.05F;
                if (this.animateAnim0 >= 2.0F) {
                    this.isPetrified = false;
                    this.isAnimating = false;
                    this.animateAnim0 = 0.0F;
                    this.animateAnim = 0.0F;
                }

                if (this.animateAnim > 0.4F) {
                    float f = (float)this.getY();
                    int i = (int)(Mth.sin((this.animateAnim - 0.4F) * (float)Math.PI) * 7.0F);
                    Vec3 vec3 = this.getDeltaMovement();

                    for(int j = 0; j < i; ++j) {
                        float f1 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                        float f2 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                        this.level.addParticle(ParticleTypes.POOF, this.getX() + (double)f1, (double)(f + 0.8F), this.getZ() + (double)f2, vec3.x, vec3.y, vec3.z);
                    }
                }
            }
*/
            if (this.age < 36000) {
                this.age++;
            } else {
                this.age = 1;
            }
        }
    }

    public void cancelAnimate() {
        this.isAnimating = false;
    }

    public boolean isPetrified() {
        return this.isPetrified;
    }
    @Override
    public void handleEntityEvent(byte handleByte) {
        if (handleByte == 8) {
            this.isAnimating = true;
        } else if (handleByte == 56) {
            this.cancelAnimate();
        } else {
            super.handleEntityEvent(handleByte);
        }
    }

    protected Brain.@NotNull Provider<TuffGolemEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        return TuffGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public @NotNull Brain<TuffGolemEntity> getBrain() {
        return (Brain<TuffGolemEntity>)super.getBrain();
    }

    protected void customServerAiStep() {
        this.level.getProfiler().push("tuffGolemBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("tuffGolemActivityUpdate");
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
        Item specificItemInPlayerHand = itemInPlayerHand.getItem();

        if (specificItemInPlayerHand instanceof DyeItem && player.isCrouching()) {
            DyeColor dyecolor = ((DyeItem)specificItemInPlayerHand).getDyeColor();
            if (dyecolor != this.getCloakColor()) {
                this.setCloakColor(dyecolor);
                if (!player.getAbilities().instabuild) {
                    itemInPlayerHand.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (specificItemInPlayerHand instanceof BannerItem && player.isCrouching()) {
            player.setItemInHand(hand, Items.STICK.getDefaultInstance());
            hasCloak = true;
        }

        if (itemInPlayerHand.is(Items.STICK) && player.isCrouching()) {
            player.setItemInHand(hand, Items.WHITE_BANNER.getDefaultInstance());
            hasCloak = false;
        }

        if (itemInTuffGolemHand.isEmpty() && !itemInPlayerHand.isEmpty() && !player.isCrouching() && this.hasCloak) {
            ItemStack playerItemCopy = itemInPlayerHand.copy();
            playerItemCopy.setCount(1);
            this.setItemInHand(InteractionHand.MAIN_HAND, playerItemCopy);
            this.removeInteractionItem(player, itemInPlayerHand);
            this.level.playSound(player, this, SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 2.0F, 1.0F);
            return InteractionResult.SUCCESS;
        } else if (!itemInTuffGolemHand.isEmpty() && hand == InteractionHand.MAIN_HAND && itemInPlayerHand.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.level.playSound(player, this, SoundEvents.ANVIL_STEP, SoundSource.NEUTRAL, 2.0F, 1.0F);
            this.swing(InteractionHand.MAIN_HAND);

            for(ItemStack itemClearStack : this.getInventory().removeAllItems()) {
                BehaviorUtils.throwItem(this, itemClearStack, this.position());
            }

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

    public boolean isPlayerCreated() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setPlayerCreated(boolean playerCreated) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (playerCreated) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
        } else {
           this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
        }

    }

    public DyeColor getCloakColor() {
        return DyeColor.byId(this.entityData.get(DATA_CLOAK_COLOR));
    }

    public void setCloakColor(DyeColor dyeColor) {
        this.entityData.set(DATA_CLOAK_COLOR, dyeColor.getId());
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PlayerCreated", this.isPlayerCreated());
        tag.putByte("CloakColor", (byte)this.getCloakColor().getId());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setPlayerCreated(tag.getBoolean("PlayerCreated"));
        if (tag.contains("CloakColor", 99)) {
            this.setCloakColor(DyeColor.byId(tag.getInt("CloakColor")));
        }
    }

    public int getAge() {
        return this.age;
    }

    public float getSpin(float i) {
        return ((float)this.getAge() + i) / 20.0F + this.bobOffs;
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
    public @NotNull SimpleContainer getInventory() { return this.inventory; }
}
