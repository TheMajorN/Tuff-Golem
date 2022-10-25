package com.themajorn.tuffgolem.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class TuffGolemCloakLayer extends GeoLayerRenderer<TuffGolemEntity> {

    private static final ResourceLocation LAYER = new ResourceLocation(GeckoLib.ModID, "textures/entities/tuff_golem_cloak.png");
    private static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(GeckoLib.ModID, "geo/tuff_golem.geo.json");
    private static final ResourceLocation HOLDING_MODEL = new ResourceLocation(GeckoLib.ModID, "geo/tuff_golem_holding.geo.json");
    public TuffGolemCloakLayer(IGeoRenderer<TuffGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, TuffGolemEntity tuffGolem, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (tuffGolem.hasCloak && !tuffGolem.isInvisible()) {
            float[] diffuseColors = tuffGolem.getCloakColor().getTextureDiffuseColors();
            RenderType cameo =  RenderType.armorCutoutNoCull(LAYER);
            matrixStackIn.pushPose();
            //Move or scale the model as you see fit
            matrixStackIn.scale(1.0f, 1.0f, 1.0f);
            matrixStackIn.translate(0.0d, 0.0d, 0.0d);
            if (tuffGolem.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                this.getRenderer().render(this.getEntityModel().getModel(DEFAULT_MODEL), tuffGolem, partialTicks, cameo, matrixStackIn, bufferIn,
                        bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.NO_OVERLAY, diffuseColors[0], diffuseColors[1], diffuseColors[2], 1f);
            } else {
                this.getRenderer().render(this.getEntityModel().getModel(HOLDING_MODEL), tuffGolem, partialTicks, cameo, matrixStackIn, bufferIn,
                        bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.NO_OVERLAY, diffuseColors[0], diffuseColors[1], diffuseColors[2], 1f);
            }
            matrixStackIn.popPose();
        }
    }
}
