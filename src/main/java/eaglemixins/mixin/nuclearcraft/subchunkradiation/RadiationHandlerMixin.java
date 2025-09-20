package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import eaglemixins.capability.ChunkRadiationSource;
import nc.ModCheck;
import nc.capability.radiation.entity.IEntityRads;
import nc.capability.radiation.source.IRadiationSource;
import nc.config.NCConfig;
import nc.radiation.*;
import nc.tile.radiation.ITileRadiationEnvironment;
import nc.util.DamageSources;
import nc.util.ItemStackHelper;
import nc.util.StructureHelper;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static nc.config.NCConfig.radiation_world_chunks_per_tick;

@Mixin(RadiationHandler.class)
public abstract class RadiationHandlerMixin {
    @Shadow(remap = false) @Final private static Random RAND;
    @Shadow(remap = false) private static BlockPos newRandomOffsetPos() { return new BlockPos(0,0,0); }
    @Shadow(remap = false) private static void spawnFeralGhoul(World world, EntityLiving entityLiving) {}
    @Shadow(remap = false) private static EnumFacing tile_side;
    @Shadow(remap = false) private static BlockPos newRandomPosInChunk(Chunk chunk) { return new BlockPos(0,0,0); }
    @Shadow(remap = false) private static void mutateTerrain(World world, Chunk chunk, double radiation) {}
    @Shadow(remap = false) private static Chunk getRandomAdjacentChunk(ChunkProviderServer chunkProvider, Chunk chunk) { return null; }

    /**
     * @author Nischhelm
     * @reason quick overwrite for subchunk radiation
     */
    @SubscribeEvent
    @Overwrite(remap = false)
    public void updateWorldRadiation(TickEvent.WorldTickEvent event) {
        if (!NCConfig.radiation_enabled_public) return;

        if (event.phase != TickEvent.Phase.START || event.side == Side.CLIENT || !(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer)event.world;

        ChunkRadiationSource sourceWithPlayer = null;
        int subChunkPlayer = -1;

        ChunkProviderServer chunkProvider = world.getChunkProvider();
        Collection<Chunk> loadedChunks = chunkProvider.getLoadedChunks();
        int chunkArrSize = loadedChunks.size();
        Chunk[] chunkArray = loadedChunks.toArray(new Chunk[chunkArrSize]);
        int chunkStart = RAND.nextInt(chunkArrSize + 1);
        int chunksPerTick = Math.min(radiation_world_chunks_per_tick, chunkArrSize);
        int tickMult = chunkArrSize > 0 ? Math.max(1, chunkArrSize/chunksPerTick) : 1;

        BiomeProvider biomeProvider = world.getBiomeProvider();
        int dimension = world.provider.getDimension();
        BlockPos randomOffsetPos = newRandomOffsetPos();
        String randomStructure = ModCheck.cubicChunksLoaded() || RadStructures.STRUCTURE_LIST.isEmpty() ? null : RadStructures.STRUCTURE_LIST.get(RAND.nextInt(RadStructures.STRUCTURE_LIST.size()));

        if (chunkArrSize > 0) for (int i = chunkStart; i < chunkStart + chunksPerTick; i++) {
            Chunk chunk = chunkArray[i % chunkArrSize];
            Biome biomeAtOffset = chunk.getBiome(randomOffsetPos, biomeProvider);
            if (!chunk.isLoaded()) continue;

            IRadiationSource chunkSource = RadiationHelper.getRadiationSource(chunk);
            if (!(chunkSource instanceof ChunkRadiationSource)) return;
            ChunkRadiationSource chunkRadSource = (ChunkRadiationSource) chunkSource;

            for (int subchunk = 0; subchunk < chunk.getEntityLists().length; subchunk++) {
                chunkRadSource.setSubchunk(subchunk);

                for (Entity entity : chunk.getEntityLists()[subchunk]) {
                    if (entity instanceof EntityPlayer) {
                        subChunkPlayer = subchunk;
                        sourceWithPlayer = chunkRadSource;
                        RadiationHelper.transferRadsFromInventoryToChunkBuffer(((EntityPlayer)entity).inventory, chunkSource);
                    }
                    else if (NCConfig.radiation_dropped_items && entity instanceof EntityItem) {
                        RadiationHelper.transferRadiationFromStackToChunkBuffer(((EntityItem)entity).getItem(), chunkSource, 1D);
                    }
                    else if (entity instanceof EntityLiving) {
                        EntityLiving entityLiving = (EntityLiving)entity;
                        IEntityRads entityRads = RadiationHelper.getEntityRadiation(entityLiving);
                        if (entityRads == null) continue;

                        entityRads.setExternalRadiationResistance(RadiationHelper.getEntityArmorRadResistance(entityLiving));

                        if (NCConfig.radiation_entity_decay_rate > 0D) {
                            entityRads.setTotalRads(entityRads.getTotalRads()*Math.pow(1D - NCConfig.radiation_entity_decay_rate, tickMult), false);
                        }

                        RadiationHelper.transferRadsFromSourceToEntity(chunkSource, entityRads, entityLiving, tickMult);

                        if (entityRads.getPoisonBuffer() > 0D) {
                            double poisonRads = Math.min(entityRads.getPoisonBuffer(), entityRads.getRecentPoisonAddition()*tickMult/NCConfig.radiation_poison_time);
                            entityRads.setTotalRads(entityRads.getTotalRads() + poisonRads, false);
                            entityRads.setPoisonBuffer(entityRads.getPoisonBuffer() - poisonRads);
                            if (entityRads.getPoisonBuffer() == 0D) entityRads.resetRecentPoisonAddition();
                        }
                        else entityRads.resetRecentPoisonAddition();

                        if (entityLiving instanceof IMob) {
                            RadiationHelper.applyPotionEffects(entityLiving, entityRads, RadPotionEffects.MOB_RAD_LEVEL_LIST, RadPotionEffects.MOB_EFFECTS_LIST);
                        }
                        else {
                            if (entityRads.isFatal()) {
                                if (NCConfig.entity_register[0] && entityLiving instanceof INpc) {
                                    spawnFeralGhoul(world, entityLiving);
                                }
                                else {
                                    entityLiving.attackEntityFrom(DamageSources.FATAL_RADS, Float.MAX_VALUE);
                                }
                            }
                            else {
                                RadiationHelper.applyPotionEffects(entityLiving, entityRads, RadPotionEffects.ENTITY_RAD_LEVEL_LIST, RadPotionEffects.ENTITY_DEBUFF_LIST);
                            }
                        }
                        entityRads.setRadiationLevel(entityRads.getRadiationLevel()*Math.pow(1D - NCConfig.radiation_decay_rate, tickMult));
                    }
                }
            }
            chunkRadSource.resetSubchunk();

            chunkRadSource.setAffectsAllSubchunks();
            chunkSource.setScrubbingFraction(0D);
            chunkSource.setEffectiveScrubberCount(0D);
            chunkRadSource.resetSubchunk();

            Collection<TileEntity> tileCollection = chunk.getTileEntityMap().values();

            for (TileEntity tile : tileCollection) {
                chunkRadSource.setSubchunk(tile.getPos());
                RadiationHelper.transferRadiationFromProviderToChunkBuffer(tile, tile_side, chunkSource);
            }
            chunkRadSource.resetSubchunk();

            chunkRadSource.setAffectsAllSubchunks();
            if (RadWorlds.RAD_MAP.containsKey(dimension)) {
                RadiationHelper.addToSourceBuffer(chunkSource, RadWorlds.RAD_MAP.get(dimension));
            }
            chunkRadSource.resetSubchunk();

            chunkRadSource.setAffectsAllSubchunks();
            if (!RadBiomes.DIM_BLACKLIST.contains(dimension)) {
                Double biomeRadiation = RadBiomes.RAD_MAP.get(biomeAtOffset);
                if (biomeRadiation != null) RadiationHelper.addToSourceBuffer(chunkSource, biomeRadiation);
            }
            chunkRadSource.resetSubchunk();

            BlockPos randomChunkPos = newRandomPosInChunk(chunk);
            chunkRadSource.setSubchunk(randomChunkPos);

            if (randomStructure != null && StructureHelper.CACHE.isInStructure(world, randomStructure, randomChunkPos)) {
                Double structureRadiation = RadStructures.RAD_MAP.get(randomStructure);
                if (structureRadiation != null) RadiationHelper.addToSourceBuffer(chunkSource, structureRadiation);
            }

            if (NCConfig.radiation_check_blocks && i == chunkStart) {
                int packed = RecipeItemHelper.pack(ItemStackHelper.blockStateToStack(world.getBlockState(randomChunkPos)));
                if (RadSources.STACK_MAP.containsKey(packed)) {
                    RadiationHelper.addToSourceBuffer(chunkSource, RadSources.STACK_MAP.get(packed));
                }
            }
            chunkRadSource.resetSubchunk();

            for (TileEntity tile : tileCollection) {
                if (tile instanceof ITileRadiationEnvironment) {
                    ITileRadiationEnvironment tileRad = (ITileRadiationEnvironment) tile;
                    int subchunkIndex = MathHelper.clamp(tile.getPos().getY() >> 4, 0, 15);
                    tileRad.setCurrentChunkRadiationLevel(chunkRadSource.getSubchunkRadiationLevel(subchunkIndex));
                    tileRad.setCurrentChunkRadiationBuffer(chunkRadSource.getSubchunkRadiationBuffer(subchunkIndex));

                    chunkRadSource.setSubchunk(subchunkIndex);
                    RadiationHelper.addScrubbingFractionToChunk(chunkSource, tileRad);
                    chunkRadSource.resetSubchunk();
                }
            }

            for(int subchunk = 0; subchunk < 16; subchunk++) {
                chunkRadSource.setSubchunk(subchunk);
                if (NCConfig.radiation_scrubber_alt) {
                    double scrubbers = chunkSource.getEffectiveScrubberCount();
                    double scrubbingFraction = RadiationHelper.getAltScrubbingFraction(scrubbers);

                    RadiationHelper.addToSourceBuffer(chunkSource, -scrubbingFraction * chunkSource.getRadiationBuffer());
                    chunkSource.setScrubbingFraction(scrubbingFraction);

                }

                double changeRate = (chunkSource.getRadiationLevel() < chunkSource.getRadiationBuffer()) ? NCConfig.radiation_spread_rate : NCConfig.radiation_decay_rate * (1D - chunkSource.getScrubbingFraction()) + NCConfig.radiation_spread_rate * chunkSource.getScrubbingFraction();

                double newLevel = Math.max(0D, chunkSource.getRadiationLevel() + (chunkSource.getRadiationBuffer() - chunkSource.getRadiationLevel()) * changeRate);
                if (NCConfig.radiation_chunk_limit >= 0D) {
                    newLevel = Math.min(newLevel, NCConfig.radiation_chunk_limit);
                }
                if (!RadBiomes.LIMIT_MAP.isEmpty() && RadBiomes.LIMIT_MAP.containsKey(biomeAtOffset)) {
                    newLevel = Math.min(newLevel, RadBiomes.LIMIT_MAP.get(biomeAtOffset));
                }
                if (!RadWorlds.LIMIT_MAP.isEmpty() && RadWorlds.LIMIT_MAP.containsKey(dimension)) {
                    newLevel = Math.min(newLevel, RadWorlds.LIMIT_MAP.get(dimension));
                }

                chunkSource.setRadiationLevel(newLevel);
                mutateTerrain(world, chunk, newLevel);

                chunkRadSource.resetSubchunk();
            }
        }

        if(subChunkPlayer != -1 && sourceWithPlayer != null){
            ChunkRadiationSource finalSourceWithPlayer = sourceWithPlayer;
            int finalSubChunkPlayer = subChunkPlayer;
            world.playerEntities.forEach(p -> p.sendMessage(new TextComponentString(
                        finalSubChunkPlayer+
                            " " + finalSourceWithPlayer.getSubchunkRadiationLevel(finalSubChunkPlayer)+
                            " "+finalSourceWithPlayer.getSubchunkRadiationBuffer(finalSubChunkPlayer)+
                            " "+finalSourceWithPlayer.getSubchunkScrubbingFraction(finalSubChunkPlayer)+
                            " "+finalSourceWithPlayer.getSubchunkEffectiveScrubberCount(finalSubChunkPlayer)
            )));
        }

        if (chunkArrSize > 0) for (int i = chunkStart; i < chunkStart + chunksPerTick; i++) {
            Chunk chunk = chunkArray[i % chunkArrSize];

            eaglemixins$spreadRadiationInSubchunks(chunk);

            // Emptying buffers here too!
            RadiationHelper.spreadRadiationFromChunk(chunk, getRandomAdjacentChunk(chunkProvider, chunk));
        }

        tile_side = EnumFacing.byIndex(tile_side.getIndex() + 1);
    }

    @Unique
    private static void eaglemixins$spreadRadiationInSubchunks(Chunk chunk) {
        if (chunk == null || !chunk.isLoaded()) return;
        IRadiationSource chunkSource = RadiationHelper.getRadiationSource(chunk);
        if (!(chunkSource instanceof ChunkRadiationSource)) return;
        ChunkRadiationSource chunkRadSource = (ChunkRadiationSource) chunkSource;

        double[] dRad = new double[16];
        Arrays.fill(dRad, 0);

        for (int subchunk = 0; subchunk < 16; subchunk++) {
            chunkRadSource.setSubchunk(subchunk);
            if (chunkSource.isRadiationNegligible()) continue;
            double radLvlCurr = chunkRadSource.getSubchunkRadiationLevel(subchunk);

            for (int dSubchunk = -1; dSubchunk <= 1; dSubchunk += 2) {
                int nextsubchunk = subchunk + dSubchunk;
                if (nextsubchunk < 0 || nextsubchunk > 15) continue;

                double radLvlNext = chunkRadSource.getSubchunkRadiationLevel(nextsubchunk);
                if (radLvlNext == 0 || radLvlCurr / radLvlNext > 1 + NCConfig.radiation_spread_gradient) {
                    double radiationSpread = (radLvlCurr - radLvlNext) * NCConfig.radiation_spread_rate;
                    dRad[subchunk] -= radiationSpread; //current reduces from spreading
                    dRad[nextsubchunk] += radiationSpread * (1 - chunkRadSource.getSubchunkScrubbingFraction(nextsubchunk)); //nearby increases from spreading
                }
            }
        }

        //changing the actual values at the end so not to spread over multiple subchunks
        for(int subchunk = 0; subchunk < 16; subchunk++)
            chunkRadSource.setSubchunkRadiationLevel(subchunk, chunkRadSource.getSubchunkRadiationLevel(subchunk) + dRad[subchunk]);

        chunkRadSource.resetSubchunk();
    }
}
