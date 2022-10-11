package com.themajorn.tuffgolem.client.renderers;

import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TuffGolemRenderer extends GeoEntityRenderer<TuffGolemEntity> {
    public TuffGolemRenderer(EntityRendererProvider.Context renderManager, AnimatedGeoModel<TuffGolemEntity> modelProvider) {
        super(renderManager, modelProvider);
    }
}
