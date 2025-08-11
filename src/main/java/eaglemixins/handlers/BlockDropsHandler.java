package eaglemixins.handlers;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class BlockDropsHandler {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {

        if (event.getWorld().isRemote) return;
        Random harvesterRNG = event.getHarvester().getRNG();

        IBlockState state = event.getState();
        Block harvestedBlock = state.getBlock();
        if (harvestedBlock.getRegistryName() == null) return;
        String blockId = harvestedBlock.getRegistryName().toString();

        // Aquaculture seaweed dropped from kelp, can be baked to become kelp and kelp can be used as a fuel source to smelt 2 items. (Same-ish as 1.16.5)
        if (harvestedBlock.equals(BOPBlocks.seaweed)) {

            if (event.getHarvester() == null) return;

            if(event.isSilkTouching()) return;
            Item item = Item.getByNameOrId("aquaculture:food");
            if (item != null) {
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(item, 1, 0));
            }
            return;
        }

        if (blockId.equals("contentcreator:iron_slab_reinforced")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:iron_slab_reinforced", 1.0F);
            return;
        }

        if (blockId.equals("contentcreator:concrete_slab_reinforced")) {
            if(event.isSilkTouching()) {
                event.getDrops().clear();
                addDrop(event.getDrops(), harvesterRNG, "contentcreator:concrete_slab_reinforced", 1.0F);
                return;
            } else {
                event.getDrops().clear();
                addDrop(event.getDrops(), harvesterRNG, "minecraft:concrete_powder:8", 1.0F);
                addDrop(event.getDrops(), harvesterRNG, "minecraft:iron_nugget", 1.0F, 1, 3);
                return;
            }
        }

        if (blockId.equals("contentcreator:concrete_slab_reinforced_double")) {
            if(event.isSilkTouching()) {
                event.getDrops().clear();
                addDrop(event.getDrops(), harvesterRNG, "contentcreator:reinforced_concrete", 1.0F);
                return;
            } else {
                event.getDrops().clear();
                addDrop(event.getDrops(), harvesterRNG, "minecraft:concrete_powder:8", 1.0F);
                addDrop(event.getDrops(), harvesterRNG, "minecraft:iron_nugget", 1.0F, 1, 6);
                return;
            }
        }

        if (blockId.equals("contentcreator:concrete_stairs_reinforced")) {
            if(event.isSilkTouching()) {
                event.getDrops().clear();
                addDrop(event.getDrops(), harvesterRNG, "contentcreator:concrete_stairs_reinforced", 1.0F);
                return;
            } else {
                event.getDrops().clear();
                addDrop(event.getDrops(), harvesterRNG, "minecraft:concrete_powder:8", 1.0F);
                addDrop(event.getDrops(), harvesterRNG, "minecraft:iron_nugget", 1.0F, 1, 5);
                return;
            }
        }

        if (blockId.equals("contentcreator:iron_plate_slab_reinforced_double")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:iron_plate_slab_reinforced", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:iron_plate_slab_reinforced", 1.0F);
            return;
        }

        if (blockId.equals("contentcreator:deepslate_tile_slab_double")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:deepslate_tile_slab", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:deepslate_tile_slab", 1.0F);
            return;
        }

        if (blockId.equals("contentcreator:polished_deepslate_slab_double")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:polished_deepslate_slab", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:polished_deepslate_slab", 1.0F);
            return;
        }

        if (blockId.equals("contentcreator:deepslate_brick_slab_double")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:deepslate_brick_slab", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:deepslate_brick_slab", 1.0F);
            return;
        }

        if (blockId.equals("contentcreator:deepslate_slab_double")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:deepslate_slab", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:deepslate_slab", 1.0F);
            return;
        }

        if (blockId.equals("contentcreator:cobbled_deepslate_slab_double")) {
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:cobbled_deepslate_slab", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "contentcreator:cobbled_deepslate_slab", 1.0F);
            return;
        }

        if (harvestedBlock == Blocks.ICE || harvestedBlock == Blocks.PACKED_ICE ||
                blockId.equals("iceandfire:dragon_ice") ||
                harvestedBlock.equals(BOPBlocks.hard_ice)
        ) {
            if(event.isSilkTouching()) return;
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, "simpledifficulty:ice_chunk", 1.0F);
            addDrop(event.getDrops(), harvesterRNG, "simpledifficulty:ice_chunk", 0.5F);
            return;
        }

        //Theta and Eta Barriers
        if (blockId.equals("dimstack:bedrock")) {
            if (event.getHarvester() == null) return;
            event.getDrops().clear();
            int blockState = harvestedBlock.getMetaFromState(state);
            // On Theta Barrier Destroyed
            if (blockState == 7) {
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/stone", 1F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/stone", 0.5F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/stone", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/basalt", 0.5F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/basalt", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:flint_shard", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:flint_shard", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "simpledifficulty:magma_chunk", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "simpledifficulty:magma_chunk", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "rustic:dust_tiny_iron", 0.1F);
                addDrop(event.getDrops(), harvesterRNG, "rustic:dust_tiny_iron", 0.1F);
            }
            // On Eta Barrier Destroyed
            if (blockState == 6) {
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/stone", 1F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/stone", 0.5F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/stone", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/basalt", 0.5F);
                addDrop(event.getDrops(), harvesterRNG, "notreepunching:rock/basalt", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, BOPItems.crystal_shard.getDefaultInstance(), 0.3F);
                addDrop(event.getDrops(), harvesterRNG, BOPItems.crystal_shard.getDefaultInstance(), 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "defiledlands:defilement_powder", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "defiledlands:defilement_powder", 0.3F);
                addDrop(event.getDrops(), harvesterRNG, "contenttweaker:steel_nugget", 0.1F);
                addDrop(event.getDrops(), harvesterRNG, "contenttweaker:steel_nugget", 0.1F);
            }
            return;
        }

        //Resource Crate Loottable
        if (blockId.equals("contenttweaker:resource_crate")) {
            event.getDrops().clear();  // Clear default drops

            World world = event.getWorld();
            LootTableManager lootManager = world.getLootTableManager();
            ResourceLocation lootTable = new ResourceLocation("eaglemixins", "blocks/resource_crate");

            LootContext.Builder builder = new LootContext.Builder((WorldServer) world)
                    .withLuck(event.getFortuneLevel());

            List<ItemStack> loot = lootManager.getLootTableFromLocation(lootTable)
                    .generateLootForPools(world.rand, builder.build());

            event.getDrops().addAll(loot);

            return;
        }

        //Replace the Biome essence with randomized nbt data for data-less biome essence
        if (harvestedBlock.equals(BOPBlocks.biome_block)) {
            if(event.isSilkTouching()) return;
            event.getDrops().clear();
            addDrop(event.getDrops(), harvesterRNG, BOPItems.biome_essence.getDefaultInstance(), 1F);
        }

        //Deepslate Blocks
        if (DEEPSLATE_MAP.containsKey(blockId)) {

            BlockSpec src = DEEPSLATE_MAP.get(blockId);
            Block srcBlock = ForgeRegistries.BLOCKS.getValue(src.id);
            if (srcBlock == null) return;

            IBlockState srcState = (src.meta >= 0)
                    ? srcBlock.getStateFromMeta(src.meta)
                    : srcBlock.getDefaultState();

            event.getDrops().clear();

            if (event.isSilkTouching()) {
                // mimic silk-touch: drop the source block itself (with meta if set)
                Item item = Item.getItemFromBlock(srcBlock);
                if (item != null) {
                    event.getDrops().add(new ItemStack(item, 1, Math.max(0, src.meta)));
                }
                return;
            }

            // normal/fortune drops of the source block
            List<ItemStack> newDrops = srcBlock.getDrops(
                    event.getWorld(),
                    event.getPos(),
                    srcState,
                    event.getFortuneLevel()
            );
            event.getDrops().addAll(newDrops);
        }
    }

    private static final Map<String, BlockSpec> DEEPSLATE_MAP = new HashMap<>();
    static {
        DEEPSLATE_MAP.put("contenttweaker:deepslate_copper_ore",  parseBlockSpec("iceandfire:copper_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_silver_ore",  parseBlockSpec("iceandfire:silver_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_diamond_ore", parseBlockSpec("minecraft:diamond_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_emerald_ore", parseBlockSpec("minecraft:emerald_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_lapis_ore",   parseBlockSpec("minecraft:lapis_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_redstone_ore",parseBlockSpec("minecraft:redstone_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_gold_ore",    parseBlockSpec("minecraft:gold_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_iron_ore",    parseBlockSpec("minecraft:iron_ore"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_coal_ore",    parseBlockSpec("minecraft:coal_ore"));
        // NuclearCraft meta variants:
        DEEPSLATE_MAP.put("contenttweaker:deepslate_lead_ore",    parseBlockSpec("nuclearcraft:ore:2"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_thorium_ore", parseBlockSpec("nuclearcraft:ore:3"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_uranium_ore", parseBlockSpec("nuclearcraft:ore:4"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_boron_ore",   parseBlockSpec("nuclearcraft:ore:5"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_lithium_ore", parseBlockSpec("nuclearcraft:ore:6"));
        DEEPSLATE_MAP.put("contenttweaker:deepslate_crystal_ore", parseBlockSpec("scalinghealth:crystalore"));
    }

    private static final class BlockSpec {
        final ResourceLocation id; final int meta; // meta < 0 => ignore
        BlockSpec(ResourceLocation id, int meta) { this.id = id; this.meta = meta; }
    }

    private static BlockSpec parseBlockSpec(String spec) {
        // "mod:block[:meta]"
        String[] parts = spec.split(":");
        String mod = parts[0];
        String path = parts[1];
        int meta = -1;
        if (parts.length >= 3) {
            try { meta = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
        }
        return new BlockSpec(new ResourceLocation(mod, path), meta);
    }

    private static final Map<String, ItemStack> itemMap = new HashMap<>();

    // Old signature — fixed 1 item
    private static void addDrop(List<ItemStack> drops, Random rng, String location, float chance) {
        addDrop(drops, rng, location, chance, 1, 1);
    }

    // New signature — min/max range
    private static void addDrop(List<ItemStack> drops, Random rng, String location, float chance, int min, int max) {
        if (!itemMap.containsKey(location)) {
            // Parse string: modid:item[:meta]
            String[] parts = location.split(":");
            if (parts.length < 2) return; // invalid
            String modid = parts[0];
            String name = parts[1];
            int meta = 0;
            if (parts.length >= 3) {
                try {
                    meta = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {}
            }
            Item item = Item.getByNameOrId(modid + ":" + name);
            if (item != null) {
                itemMap.put(location, new ItemStack(item, 1, meta));
            }
        }
        addDrop(drops, rng, itemMap.get(location), chance, min, max);
    }

    // Fixed amount version for ItemStack
    private static void addDrop(List<ItemStack> drops, Random rng, ItemStack newDrop, float chance) {
        addDrop(drops, rng, newDrop, chance, 1, 1);
    }

    // Range version for ItemStack
    private static void addDrop(List<ItemStack> drops, Random rng, ItemStack newDrop, float chance, int min, int max) {
        if (chance >= 1.0F || rng.nextFloat() < chance) {
            if (newDrop != null) {
                int amount = min + rng.nextInt(max - min + 1); // inclusive
                ItemStack stack = newDrop.copy();
                stack.setCount(amount);
                drops.add(stack);
            }
        }
    }
}