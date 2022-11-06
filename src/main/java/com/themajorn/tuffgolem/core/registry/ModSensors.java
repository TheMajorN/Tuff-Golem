package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.ai.sensors.NearestItemFrameSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSensors<U extends Sensor<?>> {

    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, TuffGolem.MOD_ID);

    public static final RegistryObject<SensorType<NearestItemFrameSensor>> NEAREST_ITEM_FRAMES = SENSORS.register("nearest_item_frames",
            () -> new SensorType<>(NearestItemFrameSensor::new));


}
