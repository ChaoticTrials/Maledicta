package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.DamageTypeProviderBase;

public class DamageTypeProvider extends DamageTypeProviderBase {

    public static final ResourceKey<DamageType> KARMA = ResourceKey.create(Registries.DAMAGE_TYPE, Maledicta.getInstance().resource("maledicta_karma"));

    public final Holder<DamageType> maledictaKarma = this.damageType("maledicta_karma", 0.0f)
            .deathMessageType(DeathMessageType.INTENTIONAL_GAME_DESIGN)
            .build();

    public DamageTypeProvider(DatagenContext context) {
        super(context);
    }
}
