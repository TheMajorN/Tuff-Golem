package com.themajorn.tuffgolem.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class TuffGolemCloakLayer extends GeoLayerRenderer<TuffGolemEntity> {
    private static final ResourceLocation CLOAK_LOCATION = new ResourceLocation("textures/entities/tuff_golem_cloak.png");

    public TuffGolemCloakLayer(IGeoRenderer<TuffGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, TuffGolemEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float[] afloat = entityLivingBaseIn.getCloakColor().getTextureDiffuseColors();
        renderCopyModel(this.getEntityModel(), CLOAK_LOCATION, matrixStackIn, bufferIn, packedLightIn, entityLivingBaseIn, partialTicks, afloat[0], afloat[1], afloat[2]);
    }
}
