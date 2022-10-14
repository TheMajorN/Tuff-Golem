package com.themajorn.tuffgolem.core.registry;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.schedule.Activity;

public class ModActivities {

    public static final Activity ANIMATE = register("animate");
    public static final Activity PETRIFY = register("petrify");
    public static final Activity PICK_OUT = register("pick_out");
    public static final Activity PUT_BACK = register("put_back");


    private static Activity register(String string) {
        return Registry.register(Registry.ACTIVITY, string, new Activity(string));
    }

}
