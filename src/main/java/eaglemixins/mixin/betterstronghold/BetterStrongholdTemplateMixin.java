package eaglemixins.mixin.betterstronghold;

import com.unseen.bt.common.world.stronghold.BetterStrongholdTemplate;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.BetterStrongholdConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Fits BetterStronghold (Unseen's Structures) pieces into the terrain:
 * - every piece except the center (portal) room is cut off per block where it would generate into air (caves, ravines, open sky)
 * - the center room is always placed completely (it gets moved to solid ground beforehand, see BetterStrongholdStartMixin)
 * - data markers are only handled inside the chunk being populated. The mod handles all markers of a piece in every chunk it
 *   touches, which deletes already placed chests/spawners in neighbouring chunks and causes cascading worldgen (library: 18k markers).
 * - chests get configurable loot tables per piece type (vanilla stronghold tables by default), spawners get configurable entities
 */
@Mixin(BetterStrongholdTemplate.class)
public abstract class BetterStrongholdTemplateMixin extends StructureComponentTemplate {

    @Unique private static final String CENTER_ROOM = "portal_room";

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
        boolean centerRoom = CENTER_ROOM.equals(((ModStructureTemplateAccessor) (Object) this).eaglemixins$getPieceName());
        final boolean cutOff = !centerRoom && ForgeConfigHandler.betterstronghold.cutOffIntoAir;

        this.placeSettings.setBoundingBox(sbb);
        ITemplateProcessor base = new BlockRotationProcessor(this.templatePosition, this.placeSettings);
        // The Forge template code skips everything outside of sbb before calling the processor, so this never loads other chunks
        ITemplateProcessor processor = (w, pos, info) -> cutOff && w.isAirBlock(pos) ? null : base.processBlock(w, pos, info);
        this.template.addBlocksToWorld(world, this.templatePosition, processor, this.placeSettings, 18);

        Map<BlockPos, String> markers = this.template.getDataBlocks(this.templatePosition, this.placeSettings);
        for (Map.Entry<BlockPos, String> entry : markers.entrySet()) {
            this.handleDataMarker(entry.getValue(), entry.getKey(), world, rand, sbb);
        }
        return true;
    }

    @Inject(method = {"handleDataMarker", "func_186175_a"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$onlyMarkersInChunk(String function, BlockPos pos, World world, Random rand, StructureBoundingBox sbb, CallbackInfo ci) {
        if (sbb != null && !sbb.isVecInside(pos)) ci.cancel();
    }

    /**
     * Runs after the mod handled the marker (rolled whether the chest/spawner stays). Chests and spawners sit directly below their marker.
     */
    @Inject(method = {"handleDataMarker", "func_186175_a"}, at = @At("RETURN"), remap = false)
    private void eaglemixins$tweakChestsAndSpawners(String function, BlockPos pos, World world, Random rand, StructureBoundingBox sbb, CallbackInfo ci) {
        if (function.startsWith("chest")) {
            TileEntity te = world.getTileEntity(pos.down());
            if (!(te instanceof TileEntityChest)) return;
            String table = eaglemixins$getLootTable(((ModStructureTemplateAccessor) (Object) this).eaglemixins$getPieceName());
            if (table != null && !table.trim().isEmpty()) ((TileEntityChest) te).setLootTable(new ResourceLocation(table.trim()), rand.nextLong());
        } else if (function.startsWith("mob")) {
            TileEntity te = world.getTileEntity(pos.down());
            if (te instanceof TileEntityMobSpawner) eaglemixins$setSpawnerEntities((TileEntityMobSpawner) te, rand);
        }
    }

    @Unique
    private static String eaglemixins$getLootTable(String pieceName) {
        BetterStrongholdConfig cfg = ForgeConfigHandler.betterstronghold;
        if (pieceName == null) return cfg.corridorLootTable;
        if (pieceName.startsWith("library")) return cfg.libraryLootTable;
        if (pieceName.startsWith("cross")) return cfg.crossingLootTable;
        return cfg.corridorLootTable;
    }

    @Unique
    private static void eaglemixins$setSpawnerEntities(TileEntityMobSpawner spawner, Random rand) {
        BetterStrongholdConfig cfg = ForgeConfigHandler.betterstronghold;
        List<BetterStrongholdConfig.SpawnerEntry> entries = cfg.getSpawnerEntities();
        if (entries.isEmpty()) return;

        NBTTagList potentials = new NBTTagList();
        BetterStrongholdConfig.SpawnerEntry first = cfg.pickSpawnerEntity(rand);
        if (cfg.mixedSpawners) {
            for (BetterStrongholdConfig.SpawnerEntry entry : entries) potentials.appendTag(eaglemixins$spawnerEntity(entry.id, entry.itemWeight).toCompoundTag());
        } else {
            potentials.appendTag(eaglemixins$spawnerEntity(first.id, 1).toCompoundTag());
        }

        MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();
        NBTTagCompound tag = logic.writeToNBT(new NBTTagCompound());
        tag.setTag("SpawnPotentials", potentials);
        tag.setTag("SpawnData", eaglemixins$spawnerEntity(first.id, 1).getNbt());
        logic.readFromNBT(tag);
        spawner.markDirty();
    }

    @Unique
    private static WeightedSpawnerEntity eaglemixins$spawnerEntity(ResourceLocation id, int weight) {
        NBTTagCompound entity = new NBTTagCompound();
        entity.setString("id", id.toString());
        return new WeightedSpawnerEntity(weight, entity);
    }
}
