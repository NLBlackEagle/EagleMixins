package eaglemixins.mixin.otg;

import com.pg85.otg.forge.commands.BaseCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseCommand.class)
public interface OTGBaseCommandAccessor {
    @Accessor(value = "usage", remap = false)
    void eaglemixins$setUsage(String usage);

    @Accessor(value = "description", remap = false)
    void eaglemixins$setDescription(String description);
}
