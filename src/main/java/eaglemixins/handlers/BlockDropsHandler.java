package eaglemixins.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class BlockDropsHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (event.getHarvester() == null) return;
        Random harvesterRNG = event.getHarvester().getRNG();

        IBlockState state = event.getState();
        Block harvestedBlock = state.getBlock();
        if (harvestedBlock.getRegistryName() == null) return;
        String blockId = harvestedBlock.getRegistryName().toString();
        event.getHarvester().sendMessage(new TextComponentString(blockId));

        // Aquaculture seaweed dropped from kelp, can be baked to become kelp and kelp can be used as a fuel source to smelt 2 items. (Same-ish as 1.16.5)
        if (blockId.equals("biomesoplenty:seaweed")) {
            Item item = Item.getByNameOrId("aquaculture:food");
            if (item != null) {
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(item,1,0));
            }
            return;
        }

        if (harvestedBlock == Blocks.ICE || harvestedBlock == Blocks.PACKED_ICE ||
                blockId.equals("iceandfire:dragon_ice") ||
                blockId.equals("biomesoplenty:hard_ice")
        ) {
            event.getDrops().clear();
            addDrop(event.getDrops(),harvesterRNG,"simpledifficulty:ice_chunk",1.0F);
            addDrop(event.getDrops(),harvesterRNG,"simpledifficulty:ice_chunk",0.5F);
            return;
        }

        //Theta and Eta Barriers
        if(blockId.equals("dimstack:bedrock")) {
            event.getDrops().clear();
            int blockState = harvestedBlock.getMetaFromState(state);
            event.getHarvester().sendMessage(new TextComponentString(""+blockState));
            // On Theta Barrier Destroyed
            if(blockState == 7) {
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/stone", 1F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/stone", 0.5F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/stone", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/basalt", 0.5F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/basalt", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:flint_shard", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:flint_shard", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"simpledifficulty:magma_chunk", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"simpledifficulty:magma_chunk", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"rustic:dust_tiny_iron", 0.1F);
                addDrop(event.getDrops(),harvesterRNG,"rustic:dust_tiny_iron", 0.1F);
            }
            // On Eta Barrier Destroyed
            if(blockState == 6) {
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/stone", 1F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/stone", 0.5F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/stone", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/basalt", 0.5F);
                addDrop(event.getDrops(),harvesterRNG,"notreepunching:rock/basalt", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"biomesoplenty:crystal_shard", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"biomesoplenty:crystal_shard", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"defiledlands:defilement_powder", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"defiledlands:defilement_powder", 0.3F);
                addDrop(event.getDrops(),harvesterRNG,"contenttweaker:steel_nugget", 0.1F);
                addDrop(event.getDrops(),harvesterRNG,"contenttweaker:steel_nugget", 0.1F);
            }
            return;
        }

        //Replace the Biome essence with randomized nbt data for data-less biome essence
        if(blockId.equals("biomesoplenty:biome_block")){
            event.getDrops().clear();
            addDrop(event.getDrops(),harvesterRNG,"biomesoplenty:biome_essence", 1F);
        }
    }

    private static void addDrop(List<ItemStack> drops, Random rng, String location, float chance){
        if(chance>=1.0F || rng.nextFloat()<chance) {
            Item item = Item.getByNameOrId(location);
            if (item != null)
                drops.add(new ItemStack(item));
        }
    }
}