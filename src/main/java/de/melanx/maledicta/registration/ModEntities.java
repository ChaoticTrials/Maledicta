package de.melanx.maledicta.registration;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.lightning.ColoredLightningBoltEntity;
import de.melanx.maledicta.lightning.LightningArcEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENTITY_TYPE")
public class ModEntities {

    public static final EntityType<ColoredLightningBoltEntity> lightningBolt = EntityType.Builder.of(ColoredLightningBoltEntity::new, MobCategory.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(ResourceKey.create(Registries.ENTITY_TYPE, Maledicta.getInstance().id("lightning_bolt")));
    public static final EntityType<LightningArcEntity> lightningArc = EntityType.Builder.of(LightningArcEntity::new, MobCategory.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(8).updateInterval(Integer.MAX_VALUE).build(ResourceKey.create(Registries.ENTITY_TYPE, Maledicta.getInstance().id("lightning_arc")));
}
