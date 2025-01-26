package eaglemixins.handlers;

import eaglemixins.util.Ref;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import svenhjol.charm.world.entity.EntityChargedEmerald;

import java.util.ArrayList;
import java.util.Arrays;

public class SentientWeaponEvolutionHandler {
    private static final ArrayList<String> livingWeaponsNoTag = new ArrayList<>(Arrays.asList("weapon_bow","armor_boots","armor_pants","armor_chest","armor_helm"));
    private static final ArrayList<String> livingWeaponsWithTag = new ArrayList<>(Arrays.asList("weapon_scythe","weapon_axe","weapon_sword","weapon_cleaver","weapon_maul","weapon_lance"));

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        EntityLivingBase victim = event.getEntityLiving();
        if(victim == null || victim.world.isRemote) return;
        ResourceLocation location = EntityList.getKey(victim);
        if(location == null) return;
        if(!location.getNamespace().equals(Ref.SRPMODID)) return;

        if(!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if(player==null) return;

        int srpItemsEquipped = 0;
        for(ItemStack stack : player.getEquipmentAndArmor()){
            ResourceLocation resourceLocation = stack.getItem().getRegistryName();
            if(resourceLocation != null && resourceLocation.getNamespace().equals(Ref.SRPMODID))
                srpItemsEquipped++;
        }
        int dividedHealth = (int) (Math.floor(victim.getMaxHealth()) / srpItemsEquipped);

        for(ItemStack stack : player.getEquipmentAndArmor()) {
            ResourceLocation resourceLocation = stack.getItem().getRegistryName();
            if (resourceLocation != null && resourceLocation.getNamespace().equals(Ref.SRPMODID)) {
                String itemId = resourceLocation.getPath();
                boolean hasNoTag = livingWeaponsNoTag.contains(itemId);
                boolean hasTag = livingWeaponsWithTag.contains(itemId);
                if (!hasTag && !hasNoTag) continue;  //Not a living item

                //Setup NBT tags if living item is fresh
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                if(!stack.getTagCompound().hasKey("srpkills"))
                    stack.getTagCompound().setInteger("srpkills", 0);

                //Set srpkills tag
                int currentKills = stack.getTagCompound().getInteger("srpkills") + dividedHealth;
                stack.getTagCompound().setInteger("srpkills", currentKills);

                //Set Lore tags
                if (hasNoTag) {
                    String srpkillsToolTip = "" + TextFormatting.RESET + TextFormatting.BLUE + "---> " + currentKills;
                    String itemToWrite = itemId.equals("weapon_bow") ? "bow" : "armor";
                    String loreTag = "" + TextFormatting.RESET + TextFormatting.DARK_PURPLE + TextFormatting.ITALIC + " Your " + itemToWrite + " tasted blood, now it longs for Parasites...";
                    setLore(stack, srpkillsToolTip + loreTag);
                } else {
                    String loreTag = "" + TextFormatting.RESET + TextFormatting.DARK_PURPLE + TextFormatting.ITALIC + " Your weapon tasted blood, now it longs for Parasites...";
                    setLore(stack, loreTag);
                }

                //Evolve
                if (currentKills > 50000) {
                    EntityChargedEmerald lightningBolt = new EntityChargedEmerald(player.world, player);
                    lightningBolt.setPosition(player.posX, player.posY, player.posZ);
                    player.world.spawnEntity(lightningBolt);

                    NBTTagCompound savedTags = stack.getTagCompound();

                    Item newItem = Item.getByNameOrId("srparasites:" + itemId + "_sentient");
                    if (newItem == null) continue;
                    ItemStack newStack = new ItemStack(newItem);
                    newStack.setTagCompound(savedTags);
                    if(hasNoTag)
                        newStack.getTagCompound().getCompoundTag("display").removeTag("Lore");
                    player.entityDropItem(newStack, 0.5F);

                    stack.shrink(1);
                }
            }
        }
    }

    private static void setLore(ItemStack stack, String srpkillsToolTip) {
        NBTTagString toolTip = new NBTTagString(srpkillsToolTip);
        NBTTagList lore = new NBTTagList();
        lore.appendTag(toolTip);
        if(stack.getTagCompound().hasKey("display")) {
            NBTTagCompound displayCompound = stack.getTagCompound().getCompoundTag("display");
            if(displayCompound.hasKey("Lore"))
                ((NBTTagList) displayCompound.getTag("Lore")).set(0,toolTip);
            else
                displayCompound.setTag("Lore", lore);
        } else {
            NBTTagCompound displayCompound = new NBTTagCompound();
            displayCompound.setTag("Lore",lore);
            stack.getTagCompound().setTag("display", displayCompound);
        }
    }
}