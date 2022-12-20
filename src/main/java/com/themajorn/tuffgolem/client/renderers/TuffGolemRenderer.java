package com.themajorn.tuffgolem.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.client.models.TuffGolemModel;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
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

    public TuffGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TuffGolemModel());
        this.addLayer(new TuffGolemCloakLayer(this));
        this.shadowRadius = 0.3F;
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull TuffGolemEntity instance) {
        if (instance.hasCloak()) {
            if (instance.isPetrified()) {
                return new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified_cloaked.png");
            } else {
                return new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem_cloaked.png");
            }
        } else {
            if (instance.isPetrified()) {
                return new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified.png");
            } else {
                return new ResourceLocation(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
            }
        }
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
        if ("cape".equals(boneName)) {
            return currentEntity.getMainHandItem();
        }
        return null;
    }

    @Override
    protected ItemTransforms.TransformType getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        if ("cape".equals(boneName)) {
            return ItemTransforms.TransformType.GROUND;
        }
        return ItemTransforms.TransformType.NONE;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @Override
    protected void preRenderItem(PoseStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {
        if (shouldRenderAsBlock(item)) {
            matrixStack.translate(0.0, 0.05, -0.55);
            matrixStack.scale(1.1F, 1.1F, 1.1F);
        } else {
            matrixStack.translate(0.0, 0.22, -0.55);
            matrixStack.scale(0.7F, 0.7F, 0.7F);
            float f3 = currentEntity.getSpin(5.5F);
            matrixStack.mulPose(Vector3f.YP.rotation(f3));
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
    private boolean shouldRenderAsBlock(ItemStack item) {
        return item == animatable.getItemBySlot(EquipmentSlot.MAINHAND)
                && item.getItem() instanceof BlockItem
                && !(item.getItem() instanceof ItemNameBlockItem)
                && !(item.getItem() instanceof StandingAndWallBlockItem)
                && !(item.getItem() instanceof BedItem)
                && !(item.getItem() instanceof PlaceOnWaterBlockItem)
                && !item.is(ItemTags.SAPLINGS)
                && !item.is(Items.MANGROVE_PROPAGULE)
                && !item.is(ItemTags.FLOWERS)
                && !item.is(ItemTags.CANDLES)
                && !item.is(ItemTags.DOORS)
                && !item.is(Items.BAMBOO)
                && !item.is(Items.KELP)
                && !item.is(Items.LADDER)
                && !item.is(Items.TURTLE_EGG)
                && !item.is(Items.CAKE)
                && !item.is(Items.CAULDRON)

                && !item.is(Items.CHAIN)
                && !item.is(Items.COBWEB)
                && !item.is(Items.FERN)
                && !item.is(Items.LARGE_FERN)
                && !item.is(Items.GRASS)
                && !item.is(Items.TALL_GRASS)
                && !item.is(Items.SEAGRASS)
                && !item.is(Items.RAIL)
                && !item.is(Items.POWERED_RAIL)
                && !item.is(Items.DETECTOR_RAIL)
                && !item.is(Items.ACTIVATOR_RAIL)
                && !item.is(Items.HOPPER)
                && !item.is(Items.WEEPING_VINES)
                && !item.is(Items.VINE)
                && !item.is(Items.TWISTING_VINES)
                && !item.is(Items.WARPED_ROOTS)
                && !item.is(Items.WARPED_FUNGUS)
                && !item.is(Items.CRIMSON_ROOTS)
                && !item.is(Items.CRIMSON_FUNGUS)
                ;
    }
}
