package eaglemixins.mixin.betterstronghold;

import com.unseen.bt.common.world.stronghold.MapGenBetterStronghold;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Vanilla points eyes of ender (and /locate) at the chunk the stronghold was planned in. Strongholds can get moved to solid ground
 * (see BetterStrongholdStartMixin), so point at the center of the actual portal room instead when its start is known.
 */
@Mixin(MapGenBetterStronghold.class)
public abstract class MapGenBetterStrongholdMixin extends MapGenStronghold {

    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        BlockPos planned = super.getNearestStructurePos(world, pos, findUnexplored);
        if (planned == null) return null;

        this.initializeStructureData(world);
        StructureStart start = this.structureMap.get(ChunkPos.asLong(planned.getX() >> 4, planned.getZ() >> 4));
        if (start == null || start.getComponents().isEmpty()) return planned;

        StructureBoundingBox room = start.getComponents().get(0).getBoundingBox();
        return new BlockPos((room.minX + room.maxX) >> 1, planned.getY(), (room.minZ + room.maxZ) >> 1);
    }
}
