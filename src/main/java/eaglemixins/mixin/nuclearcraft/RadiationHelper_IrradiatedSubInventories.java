package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.EagleMixins;
import nc.capability.radiation.entity.IEntityRads;
import nc.capability.radiation.source.IRadiationSource;
import nc.config.NCConfig;
import nc.radiation.RadiationHelper;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.charm.crafting.feature.Crate;
import svenhjol.charm.crafting.tile.TileCrate;

import java.util.function.Function;

@Mixin(RadiationHelper.class)
public abstract class RadiationHelper_IrradiatedSubInventories {
    @Shadow(remap = false) private static double transferRadsFromStackToPlayer(ItemStack stack, IEntityRads playerRads, EntityPlayer player, int updateRate) {EagleMixins.LOGGER.warn("EagleMixins shadowing RadiationHelper.transferRadsFromStackToPlayer failed!");return 0;}

    @Unique private static Item eaglemixins$crateItem = null; //charm crates are only defined as blocks, not items

    @Unique
    private static double eagleMixins$applyOperationOnIInventory(IInventory inv, Function<ItemStack, Double> radsFunction, boolean alsoCheckSubInventories){
        double addedRads = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack innerStack = inv.getStackInSlot(i);
            if (!innerStack.isEmpty()) {
                //transfer radiation for each stack in the inventory
                addedRads += radsFunction.apply(innerStack);
                //Shulkers or crates in the inventory
                if(alsoCheckSubInventories)
                    addedRads += eagleMixins$applyOperationOnSubInventory(innerStack, radsFunction);
            }
        }
        return addedRads;
    }

    @Unique
    private static double eagleMixins$applyOperationOnSubInventory(ItemStack stack, Function<ItemStack, Double> radsFunction) {
        if (eaglemixins$crateItem == null) eaglemixins$crateItem = ItemShulkerBox.getItemFromBlock(Crate.crate);

        //Only Shulkers and Charm Crates
        Item item = stack.getItem();
        boolean isShulker = item instanceof ItemShulkerBox;
        if (!isShulker && !(item == eaglemixins$crateItem)) return 0;

        //Sub inventory is saved as tile entity nbt
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null || !tags.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) return 0;
        NBTTagCompound inventoryTags = tags.getCompoundTag("BlockEntityTag");
        //Needed for TileEntity.create to accept it
        if (!inventoryTags.hasKey("id", Constants.NBT.TAG_STRING)) {
            inventoryTags = inventoryTags.copy();
            inventoryTags.setString("id", isShulker ? "minecraft:shulker_box" : "charm:crate");
        }

        //Make tile entity from it
        TileEntity containedTile;
        try {
            //shouldn't crash with world=null but who knows what ppl do with shulker boxes
            containedTile = TileEntity.create(null, inventoryTags);
            if (containedTile == null) return 0;
        } catch (Exception e) {
            return 0;
        }

        //Iterate over the inventory
        double addedRads = 0;
        if (containedTile instanceof IInventory) {
            addedRads = eagleMixins$applyOperationOnIInventory((IInventory) containedTile, radsFunction, false);
        } else if (containedTile instanceof TileCrate) {
            for (int i = 0; i < ((TileCrate) containedTile).getInventorySize(); i++) {
                ItemStack innerStack = ((TileCrate) containedTile).getInventory().getStackInSlot(i);
                if (!innerStack.isEmpty()) {
                    //transfer radiation for each stack in the crate/shulker
                    addedRads += radsFunction.apply(innerStack);
                    //Not checking subinventories as we assume you cant stack shulkers in shulkers etc
                }
            }
        }
        return addedRads;
    }

    @Unique
    private static double eagleMixins$applyOperationOnBackpack(EntityPlayer player, Function<ItemStack, Double> radsFunction) {
        IBackpack cap = BackpackHelper.getBackpack(player);
        if (cap == null) return 0;
        IBackpackData data = cap.getData();
        if (!(data instanceof BackpackDataItems)) return 0;
        ItemStackHandler handler = ((BackpackDataItems) data).getItems(); //this would generate loot in the worn backpack if it has an unopened loot table. technically with an Accessor one could check if its null but i doubt it matters
        if (handler == null) return 0;

        double addedRads = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                //Normal irradiated items in backpack
                addedRads += radsFunction.apply(stack);
                //Shulkers or crates in the backpack
                addedRads += eagleMixins$applyOperationOnSubInventory(stack, radsFunction);
            }
        }
        return addedRads;
    }

    @Unique
    private static double eagleMixins$applyOperationOnEnderChest(EntityPlayer player, Function<ItemStack, Double> radsFunction) {
        if (!(player.openContainer instanceof ContainerChest)) return 0;
        ContainerChest chest = (ContainerChest) player.openContainer;
        if (!(chest.getLowerChestInventory() instanceof InventoryEnderChest)) return 0;
        InventoryEnderChest enderInventory = (InventoryEnderChest) chest.getLowerChestInventory();

        return eagleMixins$applyOperationOnIInventory(enderInventory, radsFunction, true);
    }

    @Unique
    private static double eagleMixins$applyOperationOnCraftingInventory(EntityPlayer player, Function<ItemStack, Double> radsFunction) {
        if (!(player.openContainer instanceof ContainerPlayer)) return 0;
        InventoryCrafting invCrafting = ((ContainerPlayer) player.openContainer).craftMatrix;
        return eagleMixins$applyOperationOnIInventory(invCrafting, radsFunction, true); //not craftResult cause that stack kinda doesn't exist (yet)
    }

    //From Inventory to Chunk (if hardcore containers is enabled)
    @WrapOperation(
            method = "transferRadsFromInventoryToChunkBuffer",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;transferRadiationFromProviderToChunkBuffer(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;Lnet/minecraft/util/EnumFacing;Lnc/capability/radiation/source/IRadiationSource;)V"),
            remap = false
    )
    private static void eagleMixins_useRadiationInShulkers_toChunk(ICapabilityProvider provider, EnumFacing side, IRadiationSource chunkSource, Operation<Void> original, InventoryPlayer inventory) {
        original.call(provider, side, chunkSource); //default behavior
        eagleMixins$applyOperationOnSubInventory((ItemStack) provider, subStack -> {
            original.call(subStack, side, chunkSource);
            return 0.;
        });
    }

    //From Backpack, Held Item, Ender Chest and Inventory Crafting  to Chunk (if hardcore containers is enabled)
    @Inject(method = "transferRadsFromInventoryToChunkBuffer", at = @At("TAIL"), remap = false)
    private static void eagleMixins_useRadiationInBackpack_toChunk(InventoryPlayer inventory, IRadiationSource chunkSource, CallbackInfo ci) {
        if (!NCConfig.radiation_hardcore_stacks) return;
        Function<ItemStack, Double> transferRadsToChunkFunction = subStack -> {
            RadiationHelper.transferRadiationFromProviderToChunkBuffer(subStack, null, chunkSource);
            return 0.;
        };
        eagleMixins$applyOperationOnBackpack(inventory.player, transferRadsToChunkFunction);

        //If player has inventory gui open and has clicked on an item so it's held by the mouse pointer
        if (!inventory.getItemStack().isEmpty())
            eagleMixins$applyOperationOnSubInventory(inventory.getItemStack(), transferRadsToChunkFunction);

        eagleMixins$applyOperationOnEnderChest(inventory.player, transferRadsToChunkFunction);
        eagleMixins$applyOperationOnCraftingInventory(inventory.player, transferRadsToChunkFunction);
    }

    //From Item Entity to Chunk (if hardcore containers is enabled)
    @WrapOperation(
            method = "transferRadiationFromStackToChunkBuffer",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;addToSourceBuffer(Lnc/capability/radiation/source/IRadiationSource;D)V"),
            remap = false
    )
    private static void eagleMixins_useRadiationInShulkers_fromEntityItemToChunk(IRadiationSource chunkSource, double addedRadiation, Operation<Void> original, ItemStack stack, IRadiationSource unused, double multiplier) {
        original.call(chunkSource, addedRadiation);
        eagleMixins$applyOperationOnSubInventory(stack, subStack -> {
            original.call(chunkSource, RadiationHelper.getRadiationFromStack(subStack, multiplier));
            return 0.;
        });
    }

    //From Inventory to Player
    @WrapOperation(
            method = "transferRadsFromInventoryToPlayer",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;transferRadsFromStackToPlayer(Lnet/minecraft/item/ItemStack;Lnc/capability/radiation/entity/IEntityRads;Lnet/minecraft/entity/player/EntityPlayer;I)D"),
            remap = false
    )
    private static double eagleMixins_useRadiationInShulkers_toPlayer(ItemStack checkedStack, IEntityRads playerRads, EntityPlayer player, int updateRate, Operation<Double> original) {
        double originalRads = original.call(checkedStack, playerRads, player, updateRate); //default behavior
        double addedRads = eagleMixins$applyOperationOnSubInventory(checkedStack, subStack -> original.call(subStack, playerRads, player, updateRate));

        return originalRads + addedRads;
    }

    //From Backpack, Held Item, Ender Chest and Inventory Crafting to Player
    @ModifyReturnValue(method = "transferRadsFromInventoryToPlayer", at = @At("TAIL"), remap = false)
    private static double eagleMixins_useRadiationInBackpack_toPlayer(double original, IEntityRads playerRads, EntityPlayer player, int updateRate) {
        Function<ItemStack, Double> transferRadsToPlayerFunction = subStack -> transferRadsFromStackToPlayer(subStack, playerRads, player, updateRate);

        double backPackRads = eagleMixins$applyOperationOnBackpack(player, transferRadsToPlayerFunction);

        double clickedItemRads = 0;
        if (!player.inventory.getItemStack().isEmpty()) {
            clickedItemRads += transferRadsFromStackToPlayer(player.inventory.getItemStack(), playerRads, player, updateRate);
            clickedItemRads += eagleMixins$applyOperationOnSubInventory(player.inventory.getItemStack(), transferRadsToPlayerFunction);
        }

        double enderChestRads = eagleMixins$applyOperationOnEnderChest(player, transferRadsToPlayerFunction);
        double inventoryCraftingRads = eagleMixins$applyOperationOnCraftingInventory(player, transferRadsToPlayerFunction);

        return original + backPackRads + clickedItemRads + enderChestRads + inventoryCraftingRads;
    }
}