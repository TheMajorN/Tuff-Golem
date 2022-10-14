package com.themajorn.tuffgolem.client.renderevents;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.client.renderers.TuffGolemRenderer;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TuffGolem.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobRenderEvents {

    @SubscribeEvent
    public static void entityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TUFF_GOLEM.get(), TuffGolemRenderer::new);
    }

    @SubscribeEvent
    public static void entityAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TUFF_GOLEM.get(), TuffGolemEntity.setAttributes().build());
    }
}
