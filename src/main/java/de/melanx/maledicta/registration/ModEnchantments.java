package de.melanx.maledicta.registration;

import de.melanx.maledicta.enchtantments.CurseOfKarma;
import net.minecraft.world.item.enchantment.Enchantment;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENCHANTMENT_REGISTRY")
public class ModEnchantments {

    public static final Enchantment curseOfKarma = new CurseOfKarma();
}
