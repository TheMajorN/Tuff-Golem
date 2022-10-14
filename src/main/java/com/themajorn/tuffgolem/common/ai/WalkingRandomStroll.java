package com.themajorn.tuffgolem.common.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.phys.Vec3;

public class WalkingRandomStroll extends RandomStroll {
    public WalkingRandomStroll(float f) {
        this(f, true);
    }

    public WalkingRandomStroll(float f, boolean b) {
        super(f, b);
    }

    protected Vec3 getTargetPos(PathfinderMob mob) {
        Vec3 vec3 = mob.getViewVector(0.0F);
        return AirAndWaterRandomPos.getPos(mob, this.maxHorizontalDistance, this.maxVerticalDistance, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
    }
}
