package com.themajorn.tuffgolem.client.models;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TuffGolemModel extends AnimatedGeoModel<TuffGolemEntity> {
    @Override
    public void setLivingAnimations(TuffGolemEntity entity, Integer uniqueID, AnimationEvent customPredicate) {

    }

    @Override
    public ResourceLocation getModelResource(TuffGolemEntity object) {
        if (object.hasItemInHand()) {
            return new ResourceLocation(TuffGolem.MOD_ID, "geo/tuff_golem_holding.geo.json");
        } else {
            return new ResourceLocation(TuffGolem.MOD_ID, "geo/tuff_golem.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(TuffGolemEntity object) {
        return new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TuffGolemEntity animatable) {
        return new ResourceLocation(TuffGolem.MOD_ID, "animations/tuff_golem.animation.json");
    }
}
