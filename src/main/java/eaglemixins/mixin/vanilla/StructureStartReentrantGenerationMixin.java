package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * StructureStart.generateStructure walks the piece list and removes failed pieces while iterating it.
 * If placing a piece loads a neighbouring chunk that still has to populate (cascading worldgen), that population
 * runs generateStructure of the same start again, removes pieces from the same list, and the outer loop then
 * crashes with a ConcurrentModificationException (seen with Better Mineshafts on OTG worlds).
 * This iterates a snapshot instead: pieces removed by a nested call are skipped and removals go to the real list.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartReentrantGenerationMixin {
    @WrapOperation(
            method = "generateStructure",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", remap = false)
    )
    private Iterator<StructureComponent> eaglemixins$snapshotIterator(List<StructureComponent> components, Operation<Iterator<StructureComponent>> original) {
        final List<StructureComponent> snapshot = new ArrayList<>(components);
        return new Iterator<StructureComponent>() {
            private int index = 0;
            private StructureComponent current = null;

            @Override
            public boolean hasNext() {
                // skip pieces a nested generateStructure call removed in the meantime
                while (index < snapshot.size() && !components.contains(snapshot.get(index))) index++;
                return index < snapshot.size();
            }

            @Override
            public StructureComponent next() {
                if (!hasNext()) throw new NoSuchElementException();
                current = snapshot.get(index++);
                return current;
            }

            @Override
            public void remove() {
                if (current == null) throw new IllegalStateException();
                components.remove(current);
                current = null;
            }
        };
    }
}
