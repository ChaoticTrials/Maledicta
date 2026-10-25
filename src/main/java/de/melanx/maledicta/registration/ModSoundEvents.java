package de.melanx.maledicta.registration;

import de.melanx.maledicta.Maledicta;
import net.minecraft.sounds.SoundEvent;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "SOUND_EVENT")
public class ModSoundEvents {

    public static final SoundEvent heartbeat = SoundEvent.createFixedRangeEvent(Maledicta.getInstance().id("heartbeat"), 1);
}
