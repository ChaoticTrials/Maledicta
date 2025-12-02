package de.melanx.maledicta.registration;

import de.melanx.maledicta.capabilities.EnergyCollector;
import net.minecraft.core.component.DataComponentType;
import org.moddingx.libx.annotation.registration.RegisterClass;

import java.util.function.UnaryOperator;

@RegisterClass(registry = "DATA_COMPONENT_TYPE")
public class ModDataComponentTypes {

    public static final DataComponentType<EnergyCollector> energyCollector = ModDataComponentTypes.builder(builder -> builder.persistent(EnergyCollector.CODEC));

    private static <T> DataComponentType<T> builder(UnaryOperator<DataComponentType.Builder<T>> builder) {
        return builder.apply(new DataComponentType.Builder<>()).build();
    }
}
