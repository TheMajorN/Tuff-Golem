package com.themajorn.tuffgolem.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.client.models.TuffGolemModel;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

import javax.annotation.Nullable;

public class TuffGolemRenderer extends GeoEntityRenderer<TuffGolemEntity> {

    public TuffGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TuffGolemModel());
        this.addLayer(new TuffGolemCloakLayer(this));
        this.shadowRadius = 0.3F;
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull TuffGolemEntity instance) {
        return  new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
    }

    @Override
    public RenderType getRenderType(TuffGolemEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        stack.scale(1.0F, 1.0F, 1.4F);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}
