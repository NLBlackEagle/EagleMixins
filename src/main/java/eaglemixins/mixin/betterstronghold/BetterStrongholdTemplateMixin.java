package eaglemixins.mixin.betterstronghold;

import com.unseen.bt.common.world.stronghold.BetterStrongholdTemplate;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.BetterStrongholdConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Fits BetterStronghold (Unseen's Structures) pieces into the terrain:
 * - every piece except the center (portal) room is cut off per block where it would generate into air (caves, ravines, open sky)
 * - the center room is always placed completely and the empty space around it is filled up, so it sits inside solid blocks
 * - data markers are only handled inside the chunk being populated. The mod handles all markers of a piece in every chunk it
 *   touches, which deletes already placed chests/spawners in neighbouring chunks and causes cascading worldgen (library: 18k markers).
 * - chests get configurable loot tables per piece type (vanilla stronghold tables by default), spawners get configurable entities
 */
@Mixin(BetterStrongholdTemplate.class)
public abstract class BetterStrongholdTemplateMixin extends StructureComponentTemplate {

    @Unique private static final String CENTER_ROOM = "portal_room";
    @Unique private static IBlockState eaglemixins$filler;
    @Unique private static String eaglemixins$fillerName;

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
        BetterStrongholdConfig cfg = ForgeConfigHandler.betterstronghold;
        boolean centerRoom = CENTER_ROOM.equals(((ModStructureTemplateAccessor) (Object) this).eaglemixins$getPieceName());
        int shell = centerRoom ? Math.max(0, cfg.centerRoomShell) : 0;

        this.placeSettings.setBoundingBox(sbb);
        ITemplateProcessor base = new BlockRotationProcessor(this.templatePosition, this.placeSettings);

        // Area of this piece that is placed this pass, the Forge template code skips everything outside of sbb before the processor
        StructureBoundingBox area = null;
        BitSet templateCells = null;
        if (shell > 0 && this.boundingBox != null && this.boundingBox.intersectsWith(sbb)) {
            area = new StructureBoundingBox(
                    Math.max(this.boundingBox.minX, sbb.minX), Math.max(this.boundingBox.minY, sbb.minY), Math.max(this.boundingBox.minZ, sbb.minZ),
                    Math.min(this.boundingBox.maxX, sbb.maxX), Math.min(this.boundingBox.maxY, sbb.maxY), Math.min(this.boundingBox.maxZ, sbb.maxZ));
            templateCells = new BitSet(area.getXSize() * area.getYSize() * area.getZSize());
        }
        final StructureBoundingBox recordArea = area;
        final BitSet recorded = templateCells;
        final boolean cutOff = !centerRoom && cfg.cutOffIntoAir;

        ITemplateProcessor processor = (w, pos, info) -> {
            if (recorded != null && recordArea.isVecInside(pos)) recorded.set(eaglemixins$index(recordArea, pos.getX(), pos.getY(), pos.getZ()));
            if (cutOff && w.isAirBlock(pos)) return null;
            return base.processBlock(w, pos, info);
        };
        this.template.addBlocksToWorld(world, this.templatePosition, processor, this.placeSettings, 18);

        if (recorded != null) eaglemixins$encase(world, recordArea, recorded, shell);

        Map<BlockPos, String> markers = this.template.getDataBlocks(this.templatePosition, this.placeSettings);
        for (Map.Entry<BlockPos, String> entry : markers.entrySet()) {
            this.handleDataMarker(entry.getValue(), entry.getKey(), world, rand, sbb);
        }
        return true;
    }

    /**
     * Fills air/liquids that are not part of the room template but within `shell` blocks of it, as if the room was dug into solid ground.
     */
    @Unique
    private static void eaglemixins$encase(World world, StructureBoundingBox area, BitSet templateCells, int shell) {
        IBlockState filler = eaglemixins$getFiller();
        int sx = area.getXSize(), sy = area.getYSize(), sz = area.getZSize();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < sx; x++) for (int y = 0; y < sy; y++) for (int z = 0; z < sz; z++) {
            int wx = area.minX + x, wy = area.minY + y, wz = area.minZ + z;
            if (templateCells.get(eaglemixins$index(area, wx, wy, wz))) continue;
            if (!eaglemixins$nearTemplate(area, templateCells, wx, wy, wz, shell)) continue;

            pos.setPos(wx, wy, wz);
            IBlockState state = world.getBlockState(pos);
            if (state.getMaterial().isLiquid() || state.getBlock().isReplaceable(world, pos)) {
                world.setBlockState(pos, filler, 2);
            }
        }
    }

    @Unique
    private static boolean eaglemixins$nearTemplate(StructureBoundingBox area, BitSet templateCells, int wx, int wy, int wz, int shell) {
        for (int x = Math.max(area.minX, wx - shell); x <= Math.min(area.maxX, wx + shell); x++)
            for (int y = Math.max(area.minY, wy - shell); y <= Math.min(area.maxY, wy + shell); y++)
                for (int z = Math.max(area.minZ, wz - shell); z <= Math.min(area.maxZ, wz + shell); z++)
                    if (templateCells.get(eaglemixins$index(area, x, y, z))) return true;
        return false;
    }

    @Unique
    private static int eaglemixins$index(StructureBoundingBox area, int wx, int wy, int wz) {
        return ((wx - area.minX) * area.getYSize() + (wy - area.minY)) * area.getZSize() + (wz - area.minZ);
    }

    @Unique
    private static IBlockState eaglemixins$getFiller() {
        String name = ForgeConfigHandler.betterstronghold.centerRoomFiller;
        if (eaglemixins$filler == null || !name.equals(eaglemixins$fillerName)) {
            Block block = Block.getBlockFromName(name);
            eaglemixins$filler = (block == null || block == Blocks.AIR ? Blocks.STONE : block).getDefaultState();
            eaglemixins$fillerName = name;
        }
        return eaglemixins$filler;
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
