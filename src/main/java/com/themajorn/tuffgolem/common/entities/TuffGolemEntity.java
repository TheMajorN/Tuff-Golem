package com.themajorn.tuffgolem.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.themajorn.tuffgolem.common.ai.TuffGolemAi;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import com.themajorn.tuffgolem.core.registry.ModSensors;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private static final EntityDataAccessor<Byte> DATA_IS_PETRIFIED = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_IS_ANIMATED = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_HAS_CLOAK = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private final SimpleContainer inventory = new SimpleContainer(1);

    protected static final ImmutableList<SensorType<? extends Sensor<? super TuffGolemEntity>>> SENSOR_TYPES =
            ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
                            SensorType.NEAREST_PLAYERS,
                            SensorType.HURT_BY,
                            SensorType.NEAREST_ITEMS,
                            ModSensors.NEAREST_ITEM_FRAMES.get());

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES =
            ImmutableList.of(MemoryModuleType.PATH,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                    ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get(),
                    ModMemoryModules.SELECTED_ITEM_FRAME.get(),
                    ModMemoryModules.ITEM_FRAME_POSITION.get(),
                    ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(),
                    ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(),
                    ModMemoryModules.MID_ANIMATE_OR_PETRIFY.get());

    private static final Vec3i TUFF_GOLEM_ITEM_PICKUP_REACH = new Vec3i(2, 1, 2);

    private boolean isPetrifying;
    private boolean isAnimating;
    private boolean isReceiving;
    private boolean isGiving;
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


    // ========================================= SPAWNING & EXISTENCE =============================================== //

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_CLOAK_COLOR, DyeColor.WHITE.getId());
        this.entityData.define(DATA_IS_PETRIFIED, (byte)0);
        this.entityData.define(DATA_IS_ANIMATED, (byte)0);
        this.entityData.define(DATA_HAS_CLOAK, (byte)0);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", this.inventory.createTag());
        tag.putBoolean("PlayerCreated", this.isPlayerCreated());
        tag.putBoolean("isPetrified", this.isPetrified());
        tag.putBoolean("isAnimated", this.isAnimated());
        tag.putBoolean("hasCloak", this.hasCloak());
        tag.putByte("CloakColor", (byte)this.getCloakColor().getId());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.inventory.fromTag(tag.getList("Inventory", 10));
        this.setPlayerCreated(tag.getBoolean("PlayerCreated"));
        this.setPetrified(tag.getBoolean("isPetrified"));
        this.setAnimated(tag.getBoolean("isAnimated"));
        this.setCloak(tag.getBoolean("hasCloak"));
        if (tag.contains("CloakColor", 99)) {
            this.setCloakColor(DyeColor.byId(tag.getInt("CloakColor")));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        RandomSource randomsource = levelAccessor.getRandom();
        TuffGolemAi.initMemories(this, randomsource);
        spawnGroupData = super.finalizeSpawn(levelAccessor, difficulty, spawnType, spawnGroupData, tag);
        this.setCanPickUpLoot(true);
        this.setAnimated(true);
        return spawnGroupData;
    }

    @Override
    protected Vec3i getPickupReach() {
        return TUFF_GOLEM_ITEM_PICKUP_REACH;
    }

    public void setPlayerCreated(boolean playerCreated) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (playerCreated) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
        }
    }

    public void setPetrified(boolean petrified) {
        byte b0 = this.entityData.get(DATA_IS_PETRIFIED);
        if (petrified) {
            this.entityData.set(DATA_IS_PETRIFIED, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_IS_PETRIFIED, (byte)(b0 & -2));
        }
    }

    public void setAnimated(boolean animated) {
        byte b0 = this.entityData.get(DATA_IS_ANIMATED);
        if (animated) {
            this.entityData.set(DATA_IS_ANIMATED, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_IS_ANIMATED, (byte)(b0 & -2));
        }
    }

    public void setCloak(boolean hasCloak) {
        byte b0 = this.entityData.get(DATA_HAS_CLOAK);
        if (hasCloak) {
            this.entityData.set(DATA_HAS_CLOAK, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_HAS_CLOAK, (byte)(b0 & -2));
        }
    }

    @Override
    public @NotNull SimpleContainer getInventory() { return this.inventory; }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive()) {
            if (this.age < 36000) {
                this.age++;
            } else {
                this.age = 1;
            }
        }
    }

    // ====================================== GETTERS, SETTERS, CHECKERS ============================================ //

    public void cancelAnimate() { this.isAnimating = false; }
    public void cancelPetrify() { this.isPetrifying = false; }
    public boolean isPetrified() { return (this.entityData.get(DATA_IS_PETRIFIED) & 1) != 0; }
    public boolean isAnimated() { return (this.entityData.get(DATA_IS_ANIMATED) & 1) != 0; }
    public boolean isAnimating() { return this.isAnimating; }
    public boolean isPetrifying() { return this.isPetrifying; }
    public boolean hasCloak() { return (this.entityData.get(DATA_HAS_CLOAK) & 1) != 0; }
    public boolean isReceiving() { return this.isReceiving; }
    public boolean isGiving() { return this.isGiving; }
    public boolean canPickUpLoot() { return !this.hasItemInHand(); }
    public boolean canBreatheUnderwater() { return true; }
    public boolean hasItemInHand() { return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(); }
    public boolean canTakeItem(@NotNull ItemStack stack) { return false; }
    public boolean isPlayerCreated() { return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0; }
    public DyeColor getCloakColor() { return DyeColor.byId(this.entityData.get(DATA_CLOAK_COLOR)); }
    public void setCloakColor(DyeColor dyeColor) { this.entityData.set(DATA_CLOAK_COLOR, dyeColor.getId()); }
    public int getAge() { return this.age; }
    public float getSpin(float i) { return ((float)this.getAge() + i) / 20.0F + this.bobOffs; }

    // ============================================== AI AND BRAIN ================================================== //

    @Override
    public void aiStep() {
        super.aiStep();
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

    @Override
    protected void pickUpItem(ItemEntity itemOnGround) {
        this.onItemPickup(itemOnGround);
        TuffGolemAi.pickUpItem(this, itemOnGround);
    }

    public void pickOutItem() {
        Vec3i reach = this.getPickupReach();
        if (!this.level.isClientSide
                && this.canPickUpLoot()
                && this.isAlive()
                && !this.dead
                && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
            for (ItemFrame itemFrame : this.level.getEntitiesOfClass(ItemFrame.class, this.getBoundingBox().inflate(reach.getX(), reach.getY(), reach.getZ()))) {
                if (!itemFrame.isRemoved() && !itemFrame.getItem().isEmpty() && !this.hasItemInHand()) {
                    ItemStack itemstack = itemFrame.getItem();
                    if (this.getBrain().getMemory(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get()).isPresent()) {
                        this.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
                        itemFrame.setItem(ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public void putBackItem() {
        Vec3i vec3i = this.getPickupReach();
        if (!this.level.isClientSide
                && !this.canPickUpLoot()
                && this.isAlive()
                && !this.dead
                && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
            for (ItemFrame itemFrame : this.level.getEntitiesOfClass(ItemFrame.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()))) {
                if (!itemFrame.isRemoved() && itemFrame.getItem().isEmpty()) {
                    ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
                    if (this.getBrain().getMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get()).isPresent()) {
                        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        itemFrame.setItem(itemstack);
                    }
                }
            }
        }
    }

    protected void customServerAiStep() {
        this.level.getProfiler().push("tuffGolemBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("tuffGolemActivityUpdate");
        TuffGolemAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    public void petrify() {
        this.isPetrifying = true;
        this.setSpeed(0.0F);
        setAnimated(false);
        setPetrified(true);
    }

    public void animate() {
        this.isAnimating = true;
        this.setSpeed(0.15F);
        setPetrified(false);
        setAnimated(true);

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
        if (specificItemInPlayerHand instanceof BannerItem banner && player.isCrouching() && !this.hasCloak()) {
            DyeColor color = banner.getColor();
            player.setItemInHand(hand, Items.STICK.getDefaultInstance());
            this.setCloak(true);
            this.setCloakColor(color);
        }

        if (itemInPlayerHand.is(Items.STICK) && player.isCrouching() && itemInTuffGolemHand.isEmpty() && this.hasCloak()) {
            player.setItemInHand(hand, Items.WHITE_BANNER.getDefaultInstance());
            this.setCloak(false);
        }

        if (itemInPlayerHand.is(Items.TUFF) && player.isCrouching()) {
            if (this.isAnimated()) {
                petrify();
            } else {
                animate();
            }
        }

        if (itemInTuffGolemHand.isEmpty() && !itemInPlayerHand.isEmpty() && !player.isCrouching() && hasCloak()) {
            isReceiving = true;
            ItemStack playerItemCopy = itemInPlayerHand.copy();
            playerItemCopy.setCount(1);
            this.setItemInHand(InteractionHand.MAIN_HAND, playerItemCopy);
            this.removeInteractionItem(player, itemInPlayerHand);
            this.level.playSound(player, this, SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 2.0F, 1.0F);
            return InteractionResult.SUCCESS;
        } else if (!itemInTuffGolemHand.isEmpty() && hand == InteractionHand.MAIN_HAND && itemInPlayerHand.isEmpty()) {
            isGiving = true;
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

    // =============================================== ANIMATION =================================================== //


    private <E extends IAnimatable> PlayState defaultPredicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.idle", true));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState petrifyPredicate(AnimationEvent<E> event) {
        if (this.isPetrifying && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.petrify", false));
            isPetrifying = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState animatePredicate(AnimationEvent<E> event) {
        if (this.isAnimating && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.animate", false));
            isAnimating = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState receivePredicate(AnimationEvent<E> event) {
        if (this.isReceiving && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.receive", false));
            isReceiving = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState givePredicate(AnimationEvent<E> event) {
        if (this.isGiving && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.give", false));
            isGiving = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                0, this::defaultPredicate));
        data.addAnimationController(new AnimationController(this, "receiveController",
                0, this::receivePredicate));
        data.addAnimationController(new AnimationController(this, "giveController",
                0, this::givePredicate));
        data.addAnimationController(new AnimationController(this, "petrifyController",
                0, this::petrifyPredicate));
        data.addAnimationController(new AnimationController(this, "animateController",
                0, this::animatePredicate));
    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }

}
