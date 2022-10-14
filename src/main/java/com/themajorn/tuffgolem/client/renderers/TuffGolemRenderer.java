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

public class TuffGolemRenderer extends ExtendedGeoEntityRenderer<TuffGolemEntity> {

    private static final ResourceLocation TUFF_GOLEM_TEXTURE = new ResourceLocation("textures/entities/tuff_golem.png");

    public TuffGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TuffGolemModel());
        this.shadowRadius = 0.3F;
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull TuffGolemEntity instance) {
        return TUFF_GOLEM_TEXTURE;
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ResourceLocation getTextureForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @Override
    protected ItemTransforms.TransformType getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @Override
    protected void preRenderItem(PoseStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {

    }

    @Override
    protected void preRenderBlock(PoseStack matrixStack, BlockState block, String boneName, TuffGolemEntity currentEntity) {

    }

    @Override
    protected void postRenderItem(PoseStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {

    }

    @Override
    protected void postRenderBlock(PoseStack matrixStack, BlockState block, String boneName, TuffGolemEntity currentEntity) {

    }

    @Override
    public RenderType getRenderType(TuffGolemEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        stack.scale(1.0F, 1.0F, 1.4F);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}
