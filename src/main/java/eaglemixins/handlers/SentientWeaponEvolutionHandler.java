package eaglemixins.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolArmorBase;
import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolMeleeBase;
import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolRangeBase;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfig;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import srpmixins.capability.adaptation.CapabilityAdaptationHandler;
import srpmixins.capability.adaptation.ICapabilityAdaptation;
import srpmixins.config.SRPMixinsConfigHandler;
import svenhjol.charm.world.entity.EntityChargedEmerald;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SentientWeaponEvolutionHandler {

    private static final String srpkillsKey = "srpkills";
    private static final Enchantment smeCoP = Enchantment.getEnchantmentByLocation("somanyenchantments:curseofpossession");
    private static final Random rand = new Random();

    private static boolean isSRPLivingGear(Item item){
        if(!(item instanceof WeaponToolMeleeBase || item instanceof WeaponToolArmorBase || item instanceof WeaponToolRangeBase))
            return false;
        ResourceLocation itemReg = item.getRegistryName();
        return itemReg != null && !itemReg.getPath().contains("sentient");
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        EntityLivingBase victim = event.getEntityLiving();
        if(victim == null || victim.world.isRemote) return;
        if(!(victim instanceof EntityParasiteBase)) return;

        if(!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if(player == null) return;

        //Count equipped srpGear
        int srpGearEquipped = 0;
        for(ItemStack stack : player.getEquipmentAndArmor())
            if(isSRPLivingGear(stack.getItem()))
                srpGearEquipped++;

        if (srpGearEquipped == 0) return;
        int dividedHealth = (int) (Math.floor(victim.getMaxHealth()) / srpGearEquipped);

        //Increase srpkills tag and evolve to sentient
        for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (isSRPLivingGear(stack.getItem())) {

                //Setup NBT if missing
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());

                //Set srpkills
                assert stack.getTagCompound() != null;
                int currentKills = stack.getTagCompound().getInteger(srpkillsKey) + dividedHealth;
                stack.getTagCompound().setInteger(srpkillsKey, currentKills);
                player.inventoryContainer.detectAndSendChanges();

                //Evolve
                if (currentKills > SRPConfig.weapon_livingSentient_HP_needed) {
                    boolean isArmor = stack.getItem() instanceof ItemArmor;

                    //Lightning strike(s)
                    for(int i = 0; i < (isArmor ? 10 : 1); i++) {
                        EntityChargedEmerald lightningBolt = new EntityChargedEmerald(player.world, player);
                        BlockPos randPos = player.getPosition();
                        if(isArmor)
                            randPos = randPos.add(rand.nextGaussian() * 2, 0 , rand.nextGaussian() * 2);
                        lightningBolt.setPosition(randPos.getX(), randPos.getY(), randPos.getZ());
                        player.world.spawnEntity(lightningBolt);
                    }

                    //Get respective sentient gear
                    String itemId = Objects.requireNonNull(stack.getItem().getRegistryName()).getPath();
                    Item newItem = Item.getByNameOrId("srparasites:" + itemId + "_sentient");
                    if (newItem == null) continue;
                    ItemStack newStack = new ItemStack(newItem);

                    //Keep NBT (enchants, repair cost, etc)
                    NBTTagCompound savedTags = stack.getTagCompound();
                    newStack.setTagCompound(savedTags);

                    //Keep adaptation in SRPMixins overhauled system
                    if(isArmor && SRPMixinsConfigHandler.adaptation.overhaulAdaptation) {
                        ICapabilityAdaptation adaCap = stack.getCapability(CapabilityAdaptationHandler.CAP_ADAPTATION, null);
                        ICapabilityAdaptation adaCapNew = newStack.getCapability(CapabilityAdaptationHandler.CAP_ADAPTATION, null);
                        if (adaCap != null && adaCapNew != null) adaCapNew.copyAdaptationsFrom(adaCap);
                    }

                    boolean hasCurseOfPossession = EnchantmentHelper.getEnchantments(newStack).get(smeCoP) != null;
                    if (hasCurseOfPossession || isArmor) {
                        stack.shrink(1);
                        player.setItemStackToSlot(slot, newStack);
                    } else {
                        stack.shrink(1);
                        player.entityDropItem(newStack, 0.5F);
                    }
                }
            }
        }
    }

    /**
     * Forge tooltip event dynamically injects LocLore and kill counter.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) return;
        if (!isSRPLivingGear(stack.getItem())) return;

        List<String> tooltip = event.getToolTip();
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;

        int kills = tag.getInteger(srpkillsKey);

        // Add kill counter on the first line if missing
        boolean hasCounter = tooltip.stream().anyMatch(line -> line.contains("--->"));
        if (!hasCounter) {
            tooltip.add(0, TextFormatting.RESET + "" + TextFormatting.BLUE + "---> " + kills);
        } else {
            // If it exists somewhere else, move it to the first line
            tooltip.removeIf(line -> line.contains("--->"));
            tooltip.add(0, TextFormatting.RESET + "" + TextFormatting.BLUE + "---> " + kills);
        }

        // Add type line below it with light purple + italic formatting
        String typeKey = null;
        if (stack.getItem() instanceof WeaponToolMeleeBase) {
            typeKey = "eaglemixins.srptooltip.weapon";
        } else if (stack.getItem() instanceof WeaponToolRangeBase) {
            typeKey = "eaglemixins.srptooltip.bow";
        } else if (stack.getItem() instanceof WeaponToolArmorBase) {
            typeKey = "eaglemixins.srptooltip.armor";
        }

        if (typeKey != null) {
            String localized = I18n.format(typeKey);
            String formatted = TextFormatting.BLUE + "" + localized;

            // Remove any previous instances of this type line
            tooltip.removeIf(line -> line.contains(localized));
            // Insert on line 1 (below kill counter)
            tooltip.add(1, formatted);
        }

        // Add LocLore lines dynamically after the first two lines
        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("LocLore", 9)) {
                NBTTagList locLore = display.getTagList("LocLore", 8);
                for (int i = 0; i < locLore.tagCount(); i++) {
                    String line = locLore.getStringTagAt(i);
                    tooltip.add(line); // append after kill counter + type line
                }
            }
        }
    }
}
