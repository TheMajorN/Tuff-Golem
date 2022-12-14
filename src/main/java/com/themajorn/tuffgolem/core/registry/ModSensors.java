package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.ai.TuffGolemAi;
import com.themajorn.tuffgolem.common.ai.sensors.NearestItemFrameSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSensors<U extends Sensor<?>> {

    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, TuffGolem.MOD_ID);

    public static final RegistryObject<SensorType<NearestItemFrameSensor>> NEAREST_ITEM_FRAMES = SENSORS.register("nearest_item_frames",
            () -> new SensorType<>(NearestItemFrameSensor::new));

    public static final RegistryObject<SensorType<TemptingSensor>> TUFF_GOLEM_TEMPTATIONS = SENSORS.register("tuff_golem_temptations",
            () -> new SensorType<>(() -> new TemptingSensor(TuffGolemAi.getTemptations())));

}
