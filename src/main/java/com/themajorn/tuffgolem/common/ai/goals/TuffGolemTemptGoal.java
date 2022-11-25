package com.themajorn.tuffgolem.common.ai.goals;

import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TuffGolemTemptGoal extends Goal {
    private static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();
    private final TargetingConditions targetingConditions;
    protected final PathfinderMob mob;
    private final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    @Nullable
    protected TuffGolemEntity tuffGolem;
    private int calmDown;
    private boolean isRunning;
    private final Ingredient items;
    private final boolean canScare;

    public TuffGolemTemptGoal(PathfinderMob mob, double speedMod, Ingredient temptIngredient, boolean canScare) {
        this.mob = mob;
        this.speedModifier = speedMod;
        this.items = temptIngredient;
        this.canScare = canScare;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
    }

    public boolean canUse() {
        if (this.calmDown > 0) {
            --this.calmDown;
            return false;
        } else {
            this.tuffGolem = this.mob.level.getNearestEntity(TuffGolemEntity.class, this.targetingConditions, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getBoundingBox().inflate(6.0D, 2.0D, 6.0D));
            return this.tuffGolem != null;
        }
    }

    private boolean shouldFollow(LivingEntity livingEntity) {
        return this.items.test(livingEntity.getMainHandItem()) || this.items.test(livingEntity.getOffhandItem());
    }

    public boolean canContinueToUse() {
        if (this.canScare()) {
            if (this.mob.distanceToSqr(this.tuffGolem) < 36.0D) {
                if (this.tuffGolem.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double)this.tuffGolem.getXRot() - this.pRotX) > 5.0D || Math.abs((double)this.tuffGolem.getYRot() - this.pRotY) > 5.0D) {
                    return false;
                }
            } else {
                this.px = this.tuffGolem.getX();
                this.py = this.tuffGolem.getY();
                this.pz = this.tuffGolem.getZ();
            }

            this.pRotX = (double)this.tuffGolem.getXRot();
            this.pRotY = (double)this.tuffGolem.getYRot();
        }

        return this.canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    public void start() {
        this.px = this.tuffGolem.getX();
        this.py = this.tuffGolem.getY();
        this.pz = this.tuffGolem.getZ();
        this.isRunning = true;
    }

    public void stop() {
        this.tuffGolem = null;
        this.mob.getNavigation().stop();
        this.calmDown = reducedTickDelay(100);
        this.isRunning = false;
    }

    public void tick() {
        this.mob.getLookControl().setLookAt(this.tuffGolem, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr(this.tuffGolem) < 6.25D) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.tuffGolem, this.speedModifier);
        }

    }

    public boolean isRunning() {
        return this.isRunning;
    }
}