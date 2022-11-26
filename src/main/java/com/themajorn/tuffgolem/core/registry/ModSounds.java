package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TuffGolem.MOD_ID);

    public static final RegistryObject<SoundEvent> ANIMATE_SOUND = SOUNDS.register("animate_sound",
            () -> new SoundEvent(new ResourceLocation(TuffGolem.MOD_ID, "animate_sound")));

    public static final RegistryObject<SoundEvent> PETRIFY_SOUND = SOUNDS.register("petrify_sound",
            () -> new SoundEvent(new ResourceLocation(TuffGolem.MOD_ID, "petrify_sound")));

    public static final RegistryObject<SoundEvent> RECEIVE_SOUND = SOUNDS.register("receive_sound",
            () -> new SoundEvent(new ResourceLocation(TuffGolem.MOD_ID, "receive_sound")));

    public static final RegistryObject<SoundEvent> GIVE_SOUND = SOUNDS.register("give_sound",
            () -> new SoundEvent(new ResourceLocation(TuffGolem.MOD_ID, "give_sound")));

}
