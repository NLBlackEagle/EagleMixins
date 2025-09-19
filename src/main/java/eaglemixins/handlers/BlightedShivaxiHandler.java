package eaglemixins.handlers;

import c4.champions.common.affix.AffixRegistry;
import c4.champions.common.affix.IAffix;
import c4.champions.common.capability.CapabilityChampionship;
import c4.champions.common.capability.IChampionship;
import c4.champions.common.rank.Rank;
import c4.champions.common.rank.RankManager;
import c4.champions.common.util.ChampionHelper;
import com.lothrazar.playerbosses.EntityPlayerBoss;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.handlers.util.AbyssalRiftSpawn;
import eaglemixins.util.Ref;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static eaglemixins.handlers.util.AbyssalRiftSpawn.isAbyssalRiftSpawn;

//By fonny, copied from RLMixins.DregoraScriptHandler
public class BlightedShivaxiHandler {
    private static final String symbol = "\u2622";
    private static final String blightedShivaxiName = TextFormatting.DARK_RED + symbol + " " + TextFormatting.DARK_GREEN + TextFormatting.BOLD + "Blighted Shivaxi" + TextFormatting.RESET + TextFormatting.DARK_RED + " " + symbol;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world.isRemote || entity.dimension != 0) return;
        if (!(entity instanceof EntityPlayerBoss)) return;
        if (!entity.getName().equals("Shivaxi")) return;
        if (!isAbyssalRiftSpawn(entity)) return;

        entity.world.createExplosion(entity, entity.posX, entity.posY, entity.posZ, 4, false);

        Entity regEnt = EntityList.createEntityByIDFromName(Ref.dragonBossReg, entity.world);
        if (!(regEnt instanceof EntityLiving)) return;
        EntityLiving toSpawn = (EntityLiving) regEnt;

        NBTTagCompound comp = toSpawn.writeToNBT(new NBTTagCompound());
        comp.setString("DeathLootTable", "dregora:entities/playerbosses/abyssal_tower_shivaxi");
        comp.setBoolean("parasitedespawn", false);
        toSpawn.readEntityFromNBT(comp);

        toSpawn.setPosition(entity.posX, entity.posY, entity.posZ);
        if (ChampionHelper.isValidChampion(toSpawn)) {
            IChampionship chp = CapabilityChampionship.getChampionship(toSpawn);
            if (chp != null && chp.getRank() == null) {
                Rank rank = RankManager.getRankForTier(1);
                chp.setRank(rank);
                Set<String> affixes = ChampionHelper.generateAffixes(rank, toSpawn);
                chp.setAffixes(affixes);
                chp.setName(blightedShivaxiName);
                chp.getRank().applyGrowth(toSpawn);

                for (String s : chp.getAffixes()) {
                    IAffix affix = AffixRegistry.getAffix(s);
                    if (affix != null) {
                        affix.onInitialSpawn(toSpawn, chp);
                    }
                }
            }
        }
        toSpawn.setCustomNameTag(blightedShivaxiName);
        toSpawn.enablePersistence();
        toSpawn.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("LoweredHealth", ForgeConfigHandler.server.blightedShivaxiHealthModifier, 1));
        toSpawn.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(new AttributeModifier("RaisedArmor", ForgeConfigHandler.server.blightedShivaxiArmorModifier, 1));
        toSpawn.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier("RaisedAttackDamage", ForgeConfigHandler.server.blightedShivaxiDamageModifier, 1));
        entity.world.spawnEntity(toSpawn);
    }

    private static final List<String> phase2Spawns = Arrays.asList(
            "srparasites:buglin",
            "srparasites:sim_adventurerhead",
            "srparasites:rupter",
            "srparasites:incompleteform_medium",
            "srparasites:pri_yelloweye"
    );
    private static final int[] phase2Weights = {6, 3, 3, 2, 1};
    private static final int phase2TotalWeights = 15;

    private static final List<String> phase3Spawns = Arrays.asList(
            "srparasites:sim_villager",
            "srparasites:sim_human",
            "srparasites:sim_bigspider",
            "srparasites:pri_arachnida",
            "srparasites:ada_arachnida"
    );
    private static final int[] phase3Weights = {5, 4, 3, 2, 1};
    private static final int phase3TotalWeights = 15;

    private static final String spawnCountKey = "MinionsSpawned";

    //summon playerbosses:player_boss ~ ~1 ~ {CustomName:"§4☢ §5§lBlighted Shivaxi§r §4☢"}
    //Function giving Abyssal Rift Shivaxi two phases at lower health where it spawns parasites
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(!Ref.entityIsInAbyssalRift(entity)) return;

        //Damage cap set at 100 per hit on abyssal rift bosses
        if(event.getAmount() > 100 && !entity.isNonBoss())
            event.setAmount(100);

        if(entity instanceof EntityPlayerBoss){
            float maxHealth = entity.getMaxHealth();
            float phase2 = maxHealth/2;
            float phase3 = maxHealth/4;
            float currHealth = entity.getHealth();

            if(!entity.getEntityData().hasKey(spawnCountKey))
                entity.getEntityData().setInteger(spawnCountKey,0);
            int spawnedCounter = entity.getEntityData().getInteger(spawnCountKey);

            boolean isPhase2 = currHealth < phase2;
            boolean isPhase3 = currHealth < phase3;

            if(isPhase2 || isPhase3) {
                if(isPhase3) event.setAmount(event.getAmount() * 0.25F);
                else event.setAmount(event.getAmount() * 0.75F);

                if(spawnedCounter < 50) {
                    int spawnCount = entity.getRNG().nextInt(6);
                    for (int i = 0; i < spawnCount; i++) {
                        Entity entityToSpawn = null;
                        //Random roll of entity ids
                        String entityToSpawnId = isPhase3 ?
                                getRandomEntity(entity.getRNG(), phase3Spawns, phase3Weights, phase3TotalWeights) :
                                getRandomEntity(entity.getRNG(), phase2Spawns, phase2Weights, phase2TotalWeights);
                        //find corresponding entity
                        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityToSpawnId));
                        if(entry != null)
                            entityToSpawn = entry.newInstance(entity.world);
                        if(entityToSpawn==null) continue;

                        //Spawning attempts
                        int spawnDist = MathHelper.getInt(entity.getRNG(), 5, 10);
                        for (int j = 0; j < 10; j++) {
                            float angle = entity.getRNG().nextFloat() * 6.14F;
                            BlockPos pos = entity.getPosition().add(spawnDist*Math.cos(angle),3,spawnDist*Math.sin(angle));
                            if(entity.world.isAirBlock(pos)){
                                entityToSpawn.setPosition(pos.getX(),pos.getY(),pos.getZ());
                                entity.world.spawnEntity(entityToSpawn);
                                spawnedCounter++;
                                break;
                            }
                        }
                    }
                    entity.getEntityData().setInteger(spawnCountKey,spawnedCounter);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        Entity entity = event.getEntity();
        if (entity instanceof AbyssalRiftSpawn) {
            ((AbyssalRiftSpawn) entity).updateSpawnPosition(event.getWorld(), event.getX(), event.getY(), event.getZ());
        }
    }

    private static String getRandomEntity(Random rand, List<String> phaseSpawns, int[] phaseWeights, int phaseTotalWeights) {
        //Weighted roll
        int randWeight = rand.nextInt(phaseTotalWeights);
        String selectedEntityName = phaseSpawns.get(0);
        for (int i = 0; i < phaseSpawns.size(); i++) {
            randWeight -= phaseWeights[i];
            if (randWeight < 0) {
                selectedEntityName = phaseSpawns.get(i);
                break;
            }
        }
        return selectedEntityName;
    }
}