package com.themajorn.tuffgolem.client.models;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TuffGolemModel<T extends LivingEntity & IAnimatable> extends AnimatedGeoModel<T> {

    protected final ResourceLocation MODEL_RESLOC;
    protected final ResourceLocation TEXTURE_DEFAULT;
    protected final String ENTITY_REGISTRY_PATH_NAME;

    public TuffGolemModel(ResourceLocation model, ResourceLocation textureDefault,
                                              String entityName) {
        super();
        this.MODEL_RESLOC = model;
        this.TEXTURE_DEFAULT = textureDefault;
        this.ENTITY_REGISTRY_PATH_NAME = entityName;
    }

    @Override
    public ResourceLocation getModelResource(T object) {
        if (!object.getMainHandItem().isEmpty()) {
            return new ResourceLocation(TuffGolem.MOD_ID, "geo/tuff_golem_holding.geo.json");
        } else {
            return new ResourceLocation(TuffGolem.MOD_ID, "geo/tuff_golem.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        if (!animatable.getMainHandItem().isEmpty()) {
            return new ResourceLocation(TuffGolem.MOD_ID, "animations/tuff_golem.animation.json");
        } else {
            return new ResourceLocation(TuffGolem.MOD_ID, "animations/tuff_golem_holding.animation.json");
        }
    }
}
