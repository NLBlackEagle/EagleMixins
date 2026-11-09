package eaglemixins.mixin.betterstronghold;

import com.unseen.bt.common.world.base.ModStructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModStructureTemplate.class, remap = false)
public interface ModStructureTemplateAccessor {
    @Accessor("pieceName")
    String eaglemixins$getPieceName();
}
