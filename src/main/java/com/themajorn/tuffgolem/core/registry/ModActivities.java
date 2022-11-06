package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModActivities {

    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, TuffGolem.MOD_ID);


    public static final RegistryObject<Activity> ANIMATE = ACTIVITIES.register("animate",
            () -> new Activity("animate"));

    public static final RegistryObject<Activity> TAKE_ITEM = ACTIVITIES.register("take_item",
            () -> new Activity("take_item"));

}
