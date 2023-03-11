package de.melanx.maledicta.registration;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.moddingx.libx.annotation.registration.RegisterClass;

import java.util.Set;

@RegisterClass(registry = "POINT_OF_INTEREST_TYPE")
public class ModPoiTypes {

    public static final PoiType poiType = new PoiType(Set.copyOf(ModBlocks.maledictusAufero.getStateDefinition().getPossibleStates()), 0, 1);
}
