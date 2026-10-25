package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModSoundEvents;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.SoundDefinitionProviderBase;

public class SoundsProvider extends SoundDefinitionProviderBase {

    public SoundsProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.sound(ModSoundEvents.heartbeat)
                .with("heartbeat")
                .subtitle("subtitle.maledicta.heartbeat");
    }
}
