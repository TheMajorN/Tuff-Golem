package com.themajorn.tuffgolem.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.themajorn.tuffgolem.common.ai.TuffGolemAi;
import com.themajorn.tuffgolem.common.ai.behaviors.MoveToRedstoneLampGoal;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import com.themajorn.tuffgolem.core.registry.ModSensors;
import com.themajorn.tuffgolem.core.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
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

import java.util.Objects;
import java.util.UUID;

public class TuffGolemEntity extends AbstractGolem implements IAnimatable, InventoryCarrier {

    // === DATA ACCESSOR VARIABLES === //

    private static final EntityDataAccessor<Integer> DATA_CLOAK_COLOR = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STACK_SIZE = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEIGHT_DIMENSION_STATE = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WIDTH_DIMENSION_STATE = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_CREATED = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_IS_PETRIFIED = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_STATE_LOCKED = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_IS_ANIMATED = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_HAS_CLOAK = SynchedEntityData.defineId(TuffGolemEntity.class, EntityDataSerializers.BYTE);

    // === SENSOR INITIALIZATION === //

    protected static final ImmutableList<SensorType<? extends Sensor<? super TuffGolemEntity>>> SENSOR_TYPES =
            ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
                            SensorType.NEAREST_PLAYERS,
                            SensorType.HURT_BY,
                            SensorType.NEAREST_ITEMS,
                            ModSensors.NEAREST_ITEM_FRAMES.get(),
                            ModSensors.TUFF_GOLEM_TEMPTATIONS.get());

    // === MEMORY MODULE INITIALIZATION === //

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES =
            ImmutableList.of(MemoryModuleType.PATH,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                    ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get(),
                    ModMemoryModules.NEAREST_REDSTONE_LAMP_MEMORY.get(),
                    ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get(),
                    ModMemoryModules.SELECTED_ITEM_FRAME.get(),
                    ModMemoryModules.ITEM_FRAME_POSITION.get(),
                    ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(),
                    ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(),
                    ModMemoryModules.GO_TO_REDSTONE_LAMP_COOLDOWN_TICKS.get(),
                    ModMemoryModules.MID_ANIMATE_OR_PETRIFY.get(),
                    MemoryModuleType.TEMPTING_PLAYER,
                    MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                    MemoryModuleType.IS_TEMPTED,
                    ModMemoryModules.STACK_TARGET.get());

    // === VARIABLES === //

    private static final Vec3i TUFF_GOLEM_ITEM_PICKUP_REACH = new Vec3i(2, 1, 2);

    private final SimpleContainer inventory = new SimpleContainer(1);

    @javax.annotation.Nullable
    private UUID stackCause;

    private final AnimationFactory factory = new AnimationFactory(this);

    // === PRIMITIVE VARIABLES === //

    private double rideDimensions = 1.0D;

    private int age;

    private int wantsToStack;

    public final float bobOffs;
    public boolean isPetrifying;
    public boolean isAnimating;
    public boolean isGiving;
    public boolean isReceiving;


    // ============================================= CONSTRUCTOR ==================================================== //
    public TuffGolemEntity(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
        this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
    }

    // ============================================== ATTRIBUTES ==================================================== //

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public void registerGoals() {
        this.goalSelector.addGoal(0, new MoveToRedstoneLampGoal(this, 1.15, 20));
    }

    // ============================================= DATA SYNCING =================================================== //
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PLAYER_CREATED, (byte) 0);
        this.entityData.define(DATA_CLOAK_COLOR, DyeColor.WHITE.getId());
        this.entityData.define(DATA_STACK_SIZE, 1);
        this.entityData.define(DATA_HEIGHT_DIMENSION_STATE, 1);
        this.entityData.define(DATA_WIDTH_DIMENSION_STATE, 1);
        this.entityData.define(DATA_IS_PETRIFIED, (byte) 0);
        this.entityData.define(DATA_STATE_LOCKED, (byte) 0);
        this.entityData.define(DATA_IS_ANIMATED, (byte) 0);
        this.entityData.define(DATA_HAS_CLOAK, (byte)0 );
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", this.inventory.createTag());
        tag.putBoolean("PlayerCreated", this.isPlayerCreated());
        tag.putBoolean("isPetrified", this.isPetrified());
        tag.putBoolean("isAnimated", this.isAnimated());
        tag.putBoolean("hasCloak", this.hasCloak());
        tag.putBoolean("stateLocked", this.stateLocked());
        tag.putByte("CloakColor", (byte) this.getCloakColor().getId());
        tag.putByte("stackSize", (byte) this.getStackSize());
        tag.putInt("wantsToStack", this.wantsToStack);
        tag.putInt("heightDimensionState", this.getHeightDimensionState());
        tag.putInt("widthDimensionState", this.getWidthDimensionState());
        if (this.stackCause != null) {
            tag.putUUID("stackCause", this.stackCause);
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.inventory.fromTag(tag.getList("Inventory", 10));
        this.setPlayerCreated(tag.getBoolean("PlayerCreated"));
        this.setPetrified(tag.getBoolean("isPetrified"));
        this.lockState(tag.getBoolean("stateLocked"));
        this.setAnimated(tag.getBoolean("isAnimated"));
        this.setHeightDimensionState(Math.min(tag.getInt("heightDimensionState"), 2));
        this.setWidthDimensionState(Math.min(tag.getInt("widthDimensionState"), 2));
        this.setCloak(tag.getBoolean("hasCloak"));
        if (tag.contains("CloakColor", 99)) {
            this.setCloakColor(DyeColor.byId(tag.getInt("CloakColor")));
        }
        this.setStackSize(tag.getInt("stackSize"));
        this.wantsToStack = tag.getInt("wantsToStack");
        this.stackCause = tag.hasUUID("stackCause") ? tag.getUUID("stackCause") : null;
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (DATA_HEIGHT_DIMENSION_STATE.equals(accessor) || DATA_WIDTH_DIMENSION_STATE.equals(accessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(accessor);
    }

    // ================================================ SOUNDS ====================================================== //

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.15F, 2.0F);
    }

    protected SoundEvent getHurtSound(DamageSource p_27517_) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    // ========================================= SPAWNING & EXISTENCE =============================================== //

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        RandomSource randomsource = levelAccessor.getRandom();
        TuffGolemAi.initMemories(this, randomsource);
        spawnGroupData = super.finalizeSpawn(levelAccessor, difficulty, spawnType, spawnGroupData, tag);
        this.isPetrifying = false;
        this.isAnimating = false;
        this.isGiving = false;
        this.isReceiving = false;
        this.setCanPickUpLoot(false);
        this.setAnimated(true);
        this.setPetrified(false);
        this.lockState(false);
        this.setPassengersRidingOffset(0.9D);
        return spawnGroupData;
    }

    public void setPlayerCreated(boolean playerCreated) {
        byte b0 = this.entityData.get(DATA_PLAYER_CREATED);
        if (playerCreated) {
            this.entityData.set(DATA_PLAYER_CREATED, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_PLAYER_CREATED, (byte)(b0 & -2));
        }
    }

    public boolean isPlayerCreated() { return (this.entityData.get(DATA_PLAYER_CREATED) & 1) != 0; }

    public void tick() {
        super.tick();
        if (this.isAlive()) {
            if (this.age < 36000) {
                this.age++;
            } else {
                this.age = 1;
            }
        }
        if (isStacked()) {
            this.setYRot(this.getVehicle().getYRot());
            if (getTuffGolemBelow().isPetrified()) {
                this.setAnimated(false);
                this.setPetrified(true);
            } else {
                this.setPetrified(false);
                this.setAnimated(true);
            }
        }

        if (this.isPetrified()) {
            this.moveTo(this.getX(), this.getY(), this.getZ());
            if (this.getYRot() >= 0 && this.getYRot() < 90) {
                this.setYRot(0.0F);
            }
            else if (this.getYRot() >= 90 && this.getYRot() < 179) {
                this.setYRot(90.0F);
            }
            else if (this.getYRot() <= -1 && this.getYRot() > -90) {
                this.setYRot(-0.1F);
            }
            else if (this.getYRot() <= -90 && this.getYRot() > -179) {
                this.setYRot(-90.0F);
            } else {
                this.setYRot(0.0F);
            }
        }

    }

    public boolean canBreatheUnderwater() { return true; }

    public Vec3i getPickupReach() { return TUFF_GOLEM_ITEM_PICKUP_REACH; }

    public int getAge() { return this.age; }

    public float getSpin(float i) { return ((float)this.getAge() + i) / 20.0F + this.bobOffs; }

    protected InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemInPlayerHand = player.getItemInHand(hand);
        ItemStack itemInTuffGolemHand = this.getItemInHand(InteractionHand.MAIN_HAND);
        Item specificItemInPlayerHand = itemInPlayerHand.getItem();

        // STACK AND UNSTACK
        if (itemInPlayerHand.is(Items.COPPER_INGOT)) {
            if (!this.level.isClientSide && this.canStack() && !this.isPassenger() && !this.wantsToStack()) {
                this.usePlayerItem(player, hand, itemInPlayerHand);
                this.setWantsToStack(player);
                return InteractionResult.SUCCESS;
            }
            else if (!this.level.isClientSide && this.isPassenger() && this.getPassengers().isEmpty()) {
                TuffGolemEntity lowestGolem = this.getBottomTuffGolem(this);
                this.stopRiding();
                if (lowestGolem.getFirstPassenger() != null) {
                    lowestGolem.setHeightDimensionState(lowestGolem.getNumOfTuffGolemsAbove(lowestGolem, 1));
                    lowestGolem.setPassengersRidingOffset(lowestGolem.getNumOfTuffGolemsAbove(lowestGolem, 1));
                } else {
                    lowestGolem.setHeightDimensionState(1);
                    lowestGolem.setWidthDimensionState(1);
                }
                if (this.isPetrified()) {
                    animate();
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }

        // ADD CLOAK
        else if (specificItemInPlayerHand instanceof BannerItem banner && !this.hasCloak()) {
            DyeColor color = banner.getColor();
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, Items.STICK.getDefaultInstance());
            }
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER);
            this.setCloak(true);
            this.setCloakColor(color);
            return InteractionResult.SUCCESS;
        }

        // CHANGE COLOR OF CLOAK
        else if (specificItemInPlayerHand instanceof DyeItem && player.isCrouching()) {
            DyeColor dyecolor = ((DyeItem)specificItemInPlayerHand).getDyeColor();
            if (dyecolor != this.getCloakColor()) {
                this.playSound(SoundEvents.DYE_USE);
                this.setCloakColor(dyecolor);
                if (!player.getAbilities().instabuild) {
                    itemInPlayerHand.shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }

        // REMOVE CLOAK
        else if (itemInPlayerHand.is(Items.STICK)
                && player.isCrouching()
                && itemInTuffGolemHand.isEmpty()
                && this.hasCloak()) {
            DyeColor color = getCloakColor();
            player.setItemInHand(hand, BannerItem.byId(color.getId()).getDefaultInstance());
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER);
            this.setCloak(false);
            return InteractionResult.SUCCESS;
        }

        // ENABLE STATE CHANGE
        else if (itemInPlayerHand.is(Items.HONEYCOMB) && player.isCrouching() && !stateLocked()) {
            this.lockState(true);
            usePlayerItem(player, hand, itemInPlayerHand);
            playSound(SoundEvents.HONEYCOMB_WAX_ON);
            return InteractionResult.SUCCESS;
        }

        // DISABLE STATE CHANGE
        else if (itemInPlayerHand.is(Items.WATER_BUCKET) && player.isCrouching() && stateLocked()) {
            this.lockState(false);
            player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
            playSound(SoundEvents.BUCKET_EMPTY);
            return InteractionResult.SUCCESS;
        }

        // ANIMATE AND PETRIFY - DEBUG ONLY - REMOVE FOR FINAL VERSION
        else if (itemInPlayerHand.is(Items.TUFF) && player.isCrouching()) {
            if (this.isAnimated()) {
                petrify();
            } else {
                animate();
            }
            return InteractionResult.SUCCESS;
        }

        // GIVE ITEM TO TUFF GOLEM
        else if (itemInTuffGolemHand.isEmpty() && !itemInPlayerHand.isEmpty() && hasCloak()) {
            this.isReceiving = true;
            ItemStack playerItemCopy = itemInPlayerHand.copy();
            playerItemCopy.setCount(1);
            this.playSound(ModSounds.RECEIVE_SOUND.get(), 0.3F, 1.0F);
            this.setItemInHand(InteractionHand.MAIN_HAND, playerItemCopy);
            this.removeInteractionItem(player, itemInPlayerHand);
            return InteractionResult.SUCCESS;
        }

        // TAKE ITEM FROM TUFF GOLEM
        else if (!itemInTuffGolemHand.isEmpty() && hand == InteractionHand.MAIN_HAND && itemInPlayerHand.isEmpty()) {
            this.isGiving = true;
            this.playSound(ModSounds.GIVE_SOUND.get(), 0.3F, 1.0F);
            this.swing(InteractionHand.MAIN_HAND);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            for(ItemStack itemClearStack : this.getInventory().removeAllItems()) {
                BehaviorUtils.throwItem(this, itemClearStack, this.position());
            }

            player.addItem(itemInTuffGolemHand);
            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, hand);
        }
    }

    // =========================================== INVENTORY & LOOT ================================================= //

    public SimpleContainer getInventory() { return this.inventory; }

    public boolean canPickUpLoot() { return !this.hasItemInHand() && this.hasCloak(); }

    public boolean hasItemInHand() { return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(); }

    public boolean canTakeItem(@NotNull ItemStack stack) { return false; }

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

    protected void usePlayerItem(Player player, InteractionHand hand, ItemStack itemStack) {
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
    }

    private void removeInteractionItem(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    // =============================================== STACKING ===================================================== //

    public int getStackSize() { return this.entityData.get(DATA_STACK_SIZE); }

    public void setStackSize(int size) { this.entityData.set(DATA_STACK_SIZE, size); }

    public int getMaxStackSize() {
        return 5; }

    public boolean isStacked() {
        return this.isPassenger() && this.getVehicle() instanceof TuffGolemEntity;
    }

    public TuffGolemEntity getTuffGolemBelow() {
        return (TuffGolemEntity) this.getVehicle();
    }

    public double getPassengersRidingOffset() {
        return rideDimensions;
    }

    public void setPassengersRidingOffset(double offset) {
        rideDimensions = offset;
    }

    public int getNumOfTuffGolemsAbove(TuffGolemEntity tuffGolem, int i) {
        if (!tuffGolem.isVehicle()) {
            return i;
        } else {
            return getNumOfTuffGolemsAbove((TuffGolemEntity) tuffGolem.getFirstPassenger(), i + 1);
        }
    }

    public TuffGolemEntity getBottomTuffGolem(TuffGolemEntity tuffGolem) {
        while (tuffGolem.getVehicle() != null) {
            tuffGolem = (TuffGolemEntity) tuffGolem.getVehicle();
        }
        return tuffGolem;
    }

    public TuffGolemEntity getTopTuffGolem(TuffGolemEntity tuffGolem) {
        while (tuffGolem.getFirstPassenger() != null) {
            tuffGolem = (TuffGolemEntity) tuffGolem.getFirstPassenger();
        }
        return tuffGolem;
    }

    public int getHeightDimensionState() {
        return this.entityData.get(DATA_HEIGHT_DIMENSION_STATE);
    }

    public void setHeightDimensionState(int state) {
        this.entityData.set(DATA_HEIGHT_DIMENSION_STATE, state);
    }

    public int getWidthDimensionState() {
        return this.entityData.get(DATA_WIDTH_DIMENSION_STATE);
    }

    public void setWidthDimensionState(int state) {
        this.entityData.set(DATA_WIDTH_DIMENSION_STATE, state);
    }

    public void resetDimensionState() {
        this.setWidthDimensionState(1);
        this.setHeightDimensionState(1);
    }

    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getWidthScale(this.getWidthDimensionState()), getHeightScale(this.getHeightDimensionState()));
    }

    private static float getHeightScale(int scale) {
        return switch (scale) {
            case 1 -> 0.99F;
            case 2 -> 1.9F;
            case 3 -> 2.9F;
            case 4 -> 3.9F;
            case 5 -> 4.9F;
            default -> 0.99F;
        };
    }

    private static float getWidthScale(int scale) {
        return switch (scale) {
            case 1 -> 1.0F;
            case 2 -> 0.85F;
            default -> 1.0F;
        };
    }

    public boolean canStack() { return this.wantsToStack <= 0; }

    public void setWantsToStack(@javax.annotation.Nullable Player player) {
        this.wantsToStack = 600;
        if (player != null) {
            this.stackCause = player.getUUID();
        }
        this.level.broadcastEntityEvent(this, (byte)18);
    }

    public boolean wantsToStack() { return this.wantsToStack > 0; }

    public void resetWantsToStack() {
        this.wantsToStack = 0;
    }

    // =========================================== ANIMATE & PETRIFY ================================================ //

    public void petrify() {
        this.isPetrifying = true;
        this.playSound(ModSounds.PETRIFY_SOUND.get(), 0.3F, 1.0F);
        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.0F);
        setAnimated(false);
        setPetrified(true);
        setCanPickUpLoot(false);
    }

    public void animate() {
        this.isAnimating = true;
        this.playSound(ModSounds.ANIMATE_SOUND.get(), 0.3F, 1.0F);
        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.15F);
        setPetrified(false);
        setAnimated(true);
        setCanPickUpLoot(true);
    }

    public void setPetrified(boolean petrified) {
        byte b0 = this.entityData.get(DATA_IS_PETRIFIED);
        if (petrified) {
            this.entityData.set(DATA_IS_PETRIFIED, (byte)(b0 | 1));

        } else {
            this.entityData.set(DATA_IS_PETRIFIED, (byte)(b0 & -2));
        }
    }

    public void lockState(boolean locked) {
        byte b0 = this.entityData.get(DATA_STATE_LOCKED);
        if (locked) {
            this.entityData.set(DATA_STATE_LOCKED, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_STATE_LOCKED, (byte)(b0 & -2));
        }
    }
    public boolean stateLocked() { return (this.entityData.get(DATA_STATE_LOCKED) & 1) != 0; }

    public void setAnimated(boolean animated) {
        byte b0 = this.entityData.get(DATA_IS_ANIMATED);
        if (animated) {
            this.entityData.set(DATA_IS_ANIMATED, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_IS_ANIMATED, (byte)(b0 & -2));
        }
    }

    public boolean isAnimated() { return (this.entityData.get(DATA_IS_ANIMATED) & 1) != 0; }

    public boolean isPetrified() { return (this.entityData.get(DATA_IS_PETRIFIED) & 1) != 0; }

    // ================================================ CLOAK ======================================================= //
    public boolean hasCloak() { return (this.entityData.get(DATA_HAS_CLOAK) & 1) != 0; }

    public DyeColor getCloakColor() { return DyeColor.byId(this.entityData.get(DATA_CLOAK_COLOR)); }

    public void setCloakColor(DyeColor dyeColor) { this.entityData.set(DATA_CLOAK_COLOR, dyeColor.getId()); }

    // ========================================= GIVE & RECEIVE ITEMS =============================================== //


    // ============================================== AI & BRAIN ==================================================== //

    public void aiStep() {
        super.aiStep();
        if (this.getAge() != 0) {
            this.wantsToStack = 0;
        }

        if (this.wantsToStack > 0) {
            --this.wantsToStack;
            if (this.wantsToStack % 10 == 0) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                this.level.addParticle(ParticleTypes.POOF, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        }

        if (!this.isPassenger() && this.getNumOfTuffGolemsAbove(this, 1) != this.getHeightDimensionState()) {
            this.setHeightDimensionState(this.getNumOfTuffGolemsAbove(this, 1));
            if (this.getFirstPassenger() == null) {
                this.setWidthDimensionState(1);
            }
        }
        if (this.level.getBlockState(this.getOnPos()).getBlock() instanceof HopperBlock
                || this.level.getBlockState(this.getOnPos().below()).getBlock() instanceof HopperBlock
                && !this.getMainHandItem().isEmpty()) {
            this.spawnAtLocation(this.getMainHandItem());
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
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

    protected Brain.@NotNull Provider<TuffGolemEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        return TuffGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<TuffGolemEntity> getBrain() {
        return (Brain<TuffGolemEntity>)super.getBrain();
    }


    // =============================================== ANIMATION ==================================================== //

    public void setCloak(boolean hasCloak) {
        byte b0 = this.entityData.get(DATA_HAS_CLOAK);
        if (hasCloak) {
            this.entityData.set(DATA_HAS_CLOAK, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_HAS_CLOAK, (byte)(b0 & -2));
        }
    }

    private <E extends IAnimatable> PlayState defaultPredicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.idle", true));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState animatePredicate(AnimationEvent<E> event) {
        if (this.isAnimating && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.animate", false));
            this.isAnimating = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState petrifyPredicate(AnimationEvent<E> event) {
        if (this.isPetrifying && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.petrify", false));
            this.isPetrifying = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState receivePredicate(AnimationEvent<E> event) {
        if (this.isReceiving && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.receive", false));
            this.isReceiving = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState givePredicate(AnimationEvent<E> event) {
        if (this.isGiving && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.give", false));
            this.isGiving = false;
        }
        return PlayState.CONTINUE;
    }

    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                0, this::defaultPredicate));
        data.addAnimationController(new AnimationController(this, "receiveController",
                0, this::receivePredicate));
        data.addAnimationController(new AnimationController(this, "giveController",
                0, this::givePredicate));
        data.addAnimationController(new AnimationController(this, "animateController",
                0, this::animatePredicate));
        data.addAnimationController(new AnimationController(this, "petrifyController",
                0, this::petrifyPredicate));
    }

    public AnimationFactory getFactory() { return this.factory; }

}
