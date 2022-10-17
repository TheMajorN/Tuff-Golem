package com.themajorn.tuffgolem.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.client.models.TuffGolemModel;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.example.client.model.entity.ExampleExtendedRendererEntityModel;
import software.bernie.example.entity.ExtendedRendererEntity;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

import javax.annotation.Nullable;

public class TuffGolemRenderer extends ExtendedGeoEntityRenderer<TuffGolemEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TuffGolem.MOD_ID,
            "textures/entities/tuff_golem.png");
    private static final ResourceLocation MODEL_RESLOC = new ResourceLocation(TuffGolem.MOD_ID,
            "geo/tuff_golem.geo.json");
    public TuffGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager,
                new TuffGolemModel<TuffGolemEntity>(MODEL_RESLOC, TEXTURE, "tuff_golem"));
        this.shadowRadius = 0.3F;
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull TuffGolemEntity instance) {
        return  new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return bone.getName().startsWith("armor");
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ResourceLocation getTextureForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, TuffGolemEntity currentEntity) {
        if ("held_item".equals(boneName)) {
            return currentEntity.getMainHandItem();
        }
        return null;
    }

    @Override
    protected ItemTransforms.TransformType getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        if ("held_item".equals(boneName)) {
            return ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        }
        return ItemTransforms.TransformType.NONE;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @Override
    public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    protected void preRenderItem(PoseStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {
        if (item == this.mainHand || item == this.offHand) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            boolean isShield = item.getItem() instanceof ShieldItem;
            if (item == this.mainHand) {
                if (isShield) {
                    matrixStack.translate(0.0, 1.125, 0.0);
                } else {
                    matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
                    matrixStack.translate(0.0, 0.6, -0.45);
                    matrixStack.scale(0.7f, 0.7f, 0.7f);
                    Minecraft.getInstance().getItemRenderer().renderStatic(
                            this.currentEntityBeingRendered.getItemInHand(InteractionHand.MAIN_HAND),
                            ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                            1, 1, matrixStack, this.rtb, 1);
                }
            }
        }
    }

    @Override
    protected void postRenderItem(PoseStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {

    }

    @Override
    protected void preRenderBlock(PoseStack matrixStack, BlockState block, String boneName, TuffGolemEntity currentEntity) {

    }

    @Override
    protected void postRenderBlock(PoseStack matrixStack, BlockState block, String boneName, TuffGolemEntity currentEntity) {

    }
}
