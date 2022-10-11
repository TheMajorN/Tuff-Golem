package com.themajorn.tuffgolem.client.models;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TuffGolemModel extends AnimatedGeoModel {
    @Override
    public void setLivingAnimations(Object entity, Integer uniqueID, AnimationEvent customPredicate) {

    }

    @Override
    public ResourceLocation getModelResource(Object object) {
        return null;
    }

    @Override
    public ResourceLocation getTextureResource(Object object) {
        return null;
    }

    @Override
    public ResourceLocation getAnimationResource(Object animatable) {
        return null;
    }
}
