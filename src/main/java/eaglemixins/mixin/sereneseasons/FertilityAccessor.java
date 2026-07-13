package eaglemixins.mixin.sereneseasons;

import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import sereneseasons.init.ModFertility;

/**
 * Read-only access to ModFertility's private static allListedPlants field.
 * This is the sanctioned Mixin way to reach a target's private members without
 * merging new public methods into the target class itself (which triggers
 * "non-private static method" validation errors).
 */
@Mixin(value = ModFertility.class, remap = false)
public interface FertilityAccessor
{
    @Accessor("allListedPlants")
    static HashSet<String> getAllListedPlants()
    {
        throw new AssertionError(); // body replaced by Mixin at transform time
    }
}
