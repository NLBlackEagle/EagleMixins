package eaglemixins.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolArmorBase;
import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolMeleeBase;
import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolRangeBase;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import svenhjol.charm.world.entity.EntityChargedEmerald;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SentientWeaponEvolutionHandler {
    private static boolean isSRPLivingGear(Item item){
        if(!(item instanceof WeaponToolMeleeBase || item instanceof WeaponToolArmorBase || item instanceof WeaponToolRangeBase))
            return false;
        ResourceLocation itemReg = item.getRegistryName();
        return itemReg != null && !itemReg.getPath().contains("sentient");
    }
    private static final String srpkillsKey = "srpkills";
    private static final Enchantment smeCoP =  Enchantment.getEnchantmentByLocation("somanyenchantments:curseofpossession");
    private static final Random rand = new Random();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        EntityLivingBase victim = event.getEntityLiving();
        if(victim == null || victim.world.isRemote) return;
        if(!(victim instanceof EntityParasiteBase)) return;

        if(!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if(player==null) return;

        //Count equipped srpGear
        int srpGearEquipped = 0;
        for(ItemStack stack : player.getEquipmentAndArmor()){
            ResourceLocation resourceLocation = stack.getItem().getRegistryName();
            if(resourceLocation != null && isSRPLivingGear(stack.getItem()))
                srpGearEquipped++;
        }
        int dividedHealth = (int) (Math.floor(victim.getMaxHealth()) / srpGearEquipped);

        //Increase srpkills tag and evolve to sentient
        for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (isSRPLivingGear(stack.getItem())) {
                boolean isMeleeWeapon = stack.getItem() instanceof WeaponToolMeleeBase;    //Weapon true, bow and armor false

                //Setup NBT tags if living item is fresh
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                if(!stack.getTagCompound().hasKey(srpkillsKey))
                    stack.getTagCompound().setInteger(srpkillsKey, 0);

                //Set srpkills tag
                int currentKills = stack.getTagCompound().getInteger(srpkillsKey) + dividedHealth;
                stack.getTagCompound().setInteger(srpkillsKey, currentKills);

                //Set Lore tags
                if (!isMeleeWeapon) {
                    List<String> loreTags = new ArrayList<>();
                    loreTags.add("" + TextFormatting.RESET + TextFormatting.BLUE + "---> " + currentKills);
                    loreTags.add(stack.getItem() instanceof WeaponToolRangeBase ? "eaglemixins.srptooltip.bow" : "eaglemixins.srptooltip.armor");
                    setLocLore(stack, loreTags);
                } else {
                    String loreTag = "eaglemixins.srptooltip.weapon";
                    setLocLore(stack, Collections.singletonList(loreTag));
                }

                //Evolve
                if (currentKills > 50000) {
                    boolean isArmor = stack.getItem() instanceof ItemArmor;

                    for(int i = 0; i < (isArmor ? 10 : 1); i++) {
                        EntityChargedEmerald lightningBolt = new EntityChargedEmerald(player.world, player);
                        BlockPos randPos = player.getPosition();
                        if(isArmor)
                            randPos = randPos.add(rand.nextGaussian() * 2, 0 , rand.nextGaussian() * 2);
                        lightningBolt.setPosition(randPos.getX(), randPos.getY(), randPos.getZ());
                        player.world.spawnEntity(lightningBolt);
                    }

                    NBTTagCompound savedTags = stack.getTagCompound();

                    String itemId = stack.getItem().getRegistryName().getPath(); //!=null already checked in isSRPLivingGear()

                    Item newItem = Item.getByNameOrId("srparasites:" + itemId + "_sentient");
                    if (newItem == null) continue;
                    ItemStack newStack = new ItemStack(newItem);
                    newStack.setTagCompound(savedTags);
                    if (!isMeleeWeapon)
                        newStack.getTagCompound().getCompoundTag("display").removeTag("LocLore");

                    boolean hasCurseOfPossession = EnchantmentHelper.getEnchantments(newStack).get(smeCoP) != null;
                    if (hasCurseOfPossession || isArmor) {
                        //replace item in slot if item has curse of possession or is armor
                        stack.shrink(1);    //This shouldn't be necessary but we do it anyway
                        player.setItemStackToSlot(slot, newStack);
                    } else {
                        //otherwise throw it
                        stack.shrink(1);
                        player.entityDropItem(newStack, 0.5F);
                    }
                }
            }
        }
    }

    private static void setLocLore(ItemStack stack, List<String> srpkillsToolTip) {
        NBTTagList lore = new NBTTagList();
        for(String s : srpkillsToolTip)
            lore.appendTag(new NBTTagString(s));

        if(stack.getTagCompound().hasKey("display")) {
            NBTTagCompound displayCompound = stack.getTagCompound().getCompoundTag("display");
            //TODO: this overwrites any other LocLore
            displayCompound.setTag("LocLore", lore);
        } else {
            NBTTagCompound displayCompound = new NBTTagCompound();
            displayCompound.setTag("LocLore",lore);
            stack.getTagCompound().setTag("display", displayCompound);
        }
    }
}