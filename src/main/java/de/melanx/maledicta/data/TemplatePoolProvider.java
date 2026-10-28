package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.sandbox.TemplateProviderBase;

public class TemplatePoolProvider extends TemplateProviderBase {

    public static final ResourceKey<StructureTemplatePool> CURSED_CAIRN = ResourceKey.create(Registries.TEMPLATE_POOL, Maledicta.getInstance().id("cursed_cairn"));

    public final Holder<StructureTemplatePool> cursedCairn = this.template()
            .single(3, "cursed_cairn/intact")
            .single(2, "cursed_cairn/ruined")
            .single(1, "cursed_cairn/collapsed")
            .build();

    public TemplatePoolProvider(DatagenContext ctx) {
        super(ctx);
    }
}
