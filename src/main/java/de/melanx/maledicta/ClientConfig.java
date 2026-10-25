package de.melanx.maledicta;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig(value = "client", client = true)
public class ClientConfig {

    @Config({"Slightly zoom the view to the beat of the heartbeat while the Curse of Doom is counting down",
            "Disable this if you are sensitive to moving images"})
    public static boolean heartbeatZoom = true;

    @Config({"Distort the picture to the beat of the heartbeat while the Curse of Doom is counting down",
            "Disable this if you are sensitive to moving images, or if it does not work with your shader pack"})
    public static boolean heartbeatShader = true;
}
