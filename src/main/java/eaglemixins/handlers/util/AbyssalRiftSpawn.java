package eaglemixins.handlers.util;

import eaglemixins.util.Ref;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface AbyssalRiftSpawn {
    boolean isAbyssalRiftSpawn();
    void setAbyssalRiftSpawn(boolean isAbyssalRiftSpawn);

    default void updateSpawnPosition(World world, float x, float y, float z) {
        BlockPos pos = new BlockPos(x, y, z);
        ResourceLocation biomeReg = world.getBiome(pos).getRegistryName();
        this.setAbyssalRiftSpawn(biomeReg != null && biomeReg.equals(Ref.abyssalRiftReg));
    }

    static boolean isAbyssalRiftSpawn(Entity entity) {
        return entity instanceof AbyssalRiftSpawn && ((AbyssalRiftSpawn) entity).isAbyssalRiftSpawn();
    }
}