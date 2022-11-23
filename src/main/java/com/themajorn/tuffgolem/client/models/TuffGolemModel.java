package com.themajorn.tuffgolem.client.models;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TuffGolemModel extends AnimatedGeoModel<TuffGolemEntity> {

    @Override
    public ResourceLocation getModelResource(TuffGolemEntity object) {
        if (object.getMainHandItem().isEmpty()) {
            return  new ResourceLocation(TuffGolem.MOD_ID, "geo/tuff_golem.geo.json");
        } else {
            return  new ResourceLocation(TuffGolem.MOD_ID, "geo/tuff_golem_holding.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(TuffGolemEntity object) {
        if (object.isPetrified()) {
            return  new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified.png");
        } else {
            return  new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(TuffGolemEntity animatable) {
            if (animatable.getMainHandItem().isEmpty()) {
                if (animatable.isPetrified()) {
                    return new ResourceLocation(TuffGolem.MOD_ID, "animations/tuff_golem_petrified.animation.json");
                } else {
                    return new ResourceLocation(TuffGolem.MOD_ID, "animations/tuff_golem.animation.json");
                }
            } else {
                return new ResourceLocation(TuffGolem.MOD_ID, "animations/tuff_golem_holding.animation.json");
            }
    }
}
