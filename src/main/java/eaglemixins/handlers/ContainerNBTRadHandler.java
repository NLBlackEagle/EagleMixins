package eaglemixins.handlers;

import nc.capability.radiation.resistance.IRadiationResistance;
import nc.config.NCConfig;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** One-stop RadShield flow (legacy apply + shulker-safe persistence). */
public final class ContainerNBTRadHandler {

    private static final String BET = "BlockEntityTag";
    private static final String FD  = "ForgeData";
    private static final String KEY = "RadShield";
    private static final int NBT_COMPOUND = 10;
    private static final int NBT_DOUBLE   = 6;

    // Carry value from BreakEvent to either HarvestDropsEvent OR EntityJoinWorldEvent
    private static final ConcurrentMap<DimPos, Pending> STASH = new ConcurrentHashMap<>();
    private static final long STASH_TTL_MS = 5000L; // safety expiry for pending entries

    /* -----------------------------------------------------------
     * 0) Smart-cancel ITEM path only when we're handling legacy.
     *    If modern caps exist, let NuclearCraft handle the item.
     * ----------------------------------------------------------- */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItemSmartCancel(PlayerInteractEvent.RightClickItem e) {
        if (e.getHand() != EnumHand.MAIN_HAND) return;

        ItemStack held = e.getItemStack();
        if (held.isEmpty()) return;
        ResourceLocation id = held.getItem().getRegistryName();
        if (id == null || !"nuclearcraft".equals(id.getNamespace()) || !"rad_shielding".equals(id.getPath())) return;

        if (!e.getEntityPlayer().isSneaking()) return;
        if (!NCConfig.radiation_tile_shielding) return;
        if (NCConfig.radiation_hardcore_containers <= 0.0D) return;

        // Raytrace the target block
        RayTraceResult hit = e.getEntityPlayer().rayTrace(5.0D, 1.0F);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return;

        LegacyProbe p = probeLegacy(e.getWorld(), hit.getBlockPos(), held);
        // Cancel item only for legacy (no modern caps) that we will handle in the block event
        if (p.applicable && !p.modern) {
            e.setCanceled(true);
            e.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    /* -----------------------------------------------------------
     * 1) Apply for legacy containers (MAIN_HAND, both sides).
     *    Client: play anvil sound when it would succeed.
     *    Server: actually install + chat + shrink.
     * ----------------------------------------------------------- */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlockApply(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != EnumHand.MAIN_HAND) return; // main-hand only

        World world = e.getWorld();
        EntityPlayer player = e.getEntityPlayer();
        ItemStack held = e.getItemStack();
        if (held.isEmpty()) return;

        ResourceLocation id = held.getItem().getRegistryName();
        if (id == null || !"nuclearcraft".equals(id.getNamespace()) || !"rad_shielding".equals(id.getPath())) return;

        if (!NCConfig.radiation_tile_shielding) return;
        if (!player.isSneaking()) return;
        if (NCConfig.radiation_hardcore_containers <= 0.0D) return;

        LegacyProbe p = probeLegacy(world, e.getPos(), held);
        // If TE has modern caps, let NC item handler handle it (we didn't cancel item in that case)
        if (p.modern || !p.applicable) return;

        boolean wouldSucceed = p.newVal > p.cur;
        if (world.isRemote) {
            if (wouldSucceed) {
                player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 0.5F, 1.0F);
            }
            return; // client does not mutate
        }

        if (!wouldSucceed) {
            player.sendMessage(
                    new TextComponentTranslation("item.nuclearcraft.rad_shielding.install_fail")
                            .appendText(" " + RadiationHelper.resistanceSigFigs(p.cur))
            );
            return;
        }

        // Server: apply + mirror + shrink + chat
        if (p.rad == null) return; // safety; server requires capability
        p.rad.setRadiationResistance(p.newVal);
        p.te.getTileData().setDouble(KEY, p.newVal);
        p.te.markDirty();
        held.shrink(1);

        player.sendMessage(
                new TextComponentTranslation("item.nuclearcraft.rad_shielding.install_success")
                        .appendText(" " + RadiationHelper.resistanceSigFigs(p.newVal))
        );
    }

    /* -------------------- 2) Break: snapshot value -------------------- */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent e) {
        World world = e.getWorld();
        if (world.isRemote) return;

        BlockPos pos = e.getPos();
        TileEntity te = world.getTileEntity(pos);
        if (te == null) return;

        double val = 0.0D;
        IRadiationResistance cap = te.getCapability(IRadiationResistance.CAPABILITY_RADIATION_RESISTANCE, null);
        if (cap != null) val = Math.max(val, cap.getRadiationResistance());
        if (te.getTileData().hasKey(KEY, NBT_DOUBLE)) val = Math.max(val, te.getTileData().getDouble(KEY));
        if (val <= 0.0D) return;

        Item blockItem = Item.getItemFromBlock(e.getState().getBlock()); // expected drop item
        STASH.put(new DimPos(world.provider.getDimension(), pos),
                new Pending(val, blockItem, System.currentTimeMillis()));
    }

    /* -------------------- 3a) Normal containers: modify drops list -------------------- */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent e) {
        World world = e.getWorld();
        if (world.isRemote) return;

        DimPos key = new DimPos(world.provider.getDimension(), e.getPos());
        Pending p = STASH.get(key);
        if (p == null || p.isExpired()) { STASH.remove(key); return; }
        if (p.value <= 0.0D) { STASH.remove(key); return; }

        Item blockItem = Item.getItemFromBlock(e.getState().getBlock());
        if (blockItem == null) return;

        for (ItemStack drop : e.getDrops()) {
            if (drop.isEmpty() || drop.getItem() != blockItem) continue;
            writeShieldToItem(drop, p.value);
            STASH.remove(key);
            return;
        }
        // If list empty (e.g., shulker), EntityJoinWorldEvent path will handle it
    }

    /* -------------------- 3b) Shulkers & manual spawns: stamp the EntityItem -------------------- */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent e) {
        if (e.getWorld().isRemote) return;
        if (!(e.getEntity() instanceof EntityItem)) return;

        EntityItem entityItem = (EntityItem) e.getEntity();
        ItemStack stack = entityItem.getItem();
        if (stack.isEmpty()) return;

        int dim = e.getWorld().provider.getDimension();
        BlockPos spawnPos = new BlockPos(entityItem.posX, entityItem.posY, entityItem.posZ);

        long now = System.currentTimeMillis();
        STASH.entrySet().removeIf(en -> en.getValue().isExpired(now));

        for (Map.Entry<DimPos, Pending> en : STASH.entrySet()) {
            DimPos dp = en.getKey();
            Pending p = en.getValue();
            if (dp.dim != dim) continue;
            if (spawnPos.distanceSq(dp.pos) > 2.25D) continue; // 1.5^2
            if (p.expectedItem != null && stack.getItem() != p.expectedItem) continue;

            writeShieldToItem(stack, p.value);
            STASH.remove(dp);
            return;
        }
    }

    /* -------------------- 4) Place: restore from BET (flat or ForgeData) -------------------- */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.PlaceEvent e) {
        World world = e.getWorld();
        if (world.isRemote) return;

        ItemStack stack = e.getItemInHand();
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) return;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(BET, NBT_COMPOUND)) return;
        NBTTagCompound bet = tag.getCompoundTag(BET);

        Double val = null;
        if (bet.hasKey(KEY, NBT_DOUBLE)) val = bet.getDouble(KEY);
        else if (bet.hasKey(FD, NBT_COMPOUND)) {
            NBTTagCompound fd = bet.getCompoundTag(FD);
            if (fd.hasKey(KEY, NBT_DOUBLE)) val = fd.getDouble(KEY);
        }
        if (val == null || val <= 0.0D) return;

        TileEntity te = world.getTileEntity(e.getPos());
        if (te == null) return;

        IRadiationResistance cap = te.getCapability(IRadiationResistance.CAPABILITY_RADIATION_RESISTANCE, null);
        if (cap != null && val > cap.getRadiationResistance()) cap.setRadiationResistance(val);

        double cur = te.getTileData().hasKey(KEY, NBT_DOUBLE) ? te.getTileData().getDouble(KEY) : 0.0D;
        if (val > cur) { te.getTileData().setDouble(KEY, val); te.markDirty(); }
    }

    /* -------------------- 5) Tooltip (client) -------------------- */
    @SideOnly(Side.CLIENT)
    public static final class Tooltip {
        @SubscribeEvent
        public static void onTooltip(ItemTooltipEvent e) {
            ItemStack stack = e.getItemStack();
            if (stack.isEmpty() || !stack.hasTagCompound()) return;
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null || !tag.hasKey(BET, NBT_COMPOUND)) return;
            NBTTagCompound bet = tag.getCompoundTag(BET);

            Double v = null;
            if (bet.hasKey(KEY, NBT_DOUBLE)) v = bet.getDouble(KEY);
            else if (bet.hasKey(FD, NBT_COMPOUND)) {
                NBTTagCompound fd = bet.getCompoundTag(FD);
                if (fd.hasKey(KEY, NBT_DOUBLE)) v = fd.getDouble(KEY);
            }
            if (v == null || v <= 0.0D) return;

            e.getToolTip().add(TextFormatting.AQUA + "Rad Resistance: " + RadiationHelper.resistanceSigFigs(v));
        }
    }

    /* -------------------- helpers -------------------- */

    private static void writeShieldToItem(ItemStack drop, double val) {
        if (drop == null || drop.isEmpty() || val <= 0.0D) return;

        NBTTagCompound tag = drop.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();

        NBTTagCompound bet = tag.hasKey(BET, NBT_COMPOUND) ? tag.getCompoundTag(BET) : new NBTTagCompound();
        bet.setDouble(KEY, val);

        NBTTagCompound fd = bet.hasKey(FD, NBT_COMPOUND) ? bet.getCompoundTag(FD) : new NBTTagCompound();
        fd.setDouble(KEY, val);
        bet.setTag(FD, fd);

        tag.setTag(BET, bet);
        drop.setTagCompound(tag);
    }

    private static double valueFromShieldStack(ItemStack held) {
        double[] levels = NCConfig.radiation_shielding_level;
        int idx = Math.max(0, Math.min(held.getMetadata(), levels.length - 1));
        return levels[idx];
    }

    private static boolean hasAnyModernCap(TileEntity te) {
        if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) return true;
        for (EnumFacing f : EnumFacing.values()) {
            if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f) ||
                    te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f)) return true;
        }
        return false;
    }

    /** Bundle of info for legacy handling decisions. */
    private static final class LegacyProbe {
        final boolean modern;         // has item/fluid caps anywhere
        final boolean applicable;     // legacy inventory + has IRadiationResistance
        final double cur, newVal;     // current & desired values
        final TileEntity te;          // TE (for server)
        final IRadiationResistance rad; // cap (for server)

        LegacyProbe(boolean modern, boolean applicable, double cur, double newVal, TileEntity te, IRadiationResistance rad) {
            this.modern = modern; this.applicable = applicable; this.cur = cur; this.newVal = newVal; this.te = te; this.rad = rad;
        }
    }

    private static LegacyProbe probeLegacy(World world, BlockPos pos, ItemStack held) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null) return new LegacyProbe(false, false, 0.0D, 0.0D, null, null);

        boolean modern = hasAnyModernCap(te);
        boolean legacy = (te instanceof IInventory || te instanceof ISidedInventory);
        if (!legacy) return new LegacyProbe(modern, false, 0.0D, 0.0D, te, null);

        IRadiationResistance rad = te.getCapability(IRadiationResistance.CAPABILITY_RADIATION_RESISTANCE, null);
        if (rad == null) return new LegacyProbe(modern, false, 0.0D, 0.0D, te, null);

        double cur = rad.getRadiationResistance();
        // also consider any mirrored ForgeData as current baseline
        if (te.getTileData().hasKey(KEY, NBT_DOUBLE)) cur = Math.max(cur, te.getTileData().getDouble(KEY));

        double newVal = valueFromShieldStack(held);
        return new LegacyProbe(modern, true, cur, newVal, te, rad);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e) {
        int dim = e.getWorld().provider.getDimension();
        STASH.keySet().removeIf(k -> k.dim == dim);
    }

    private static final class DimPos {
        final int dim; final BlockPos pos;
        DimPos(int d, BlockPos p) { dim = d; pos = p.toImmutable(); }
        @Override public boolean equals(Object o){ return o instanceof DimPos && dim==((DimPos)o).dim && pos.equals(((DimPos)o).pos); }
        @Override public int hashCode(){ return Objects.hash(dim,pos); }
    }

    private static final class Pending {
        final double value; final Item expectedItem; final long ts;
        Pending(double v, Item i, long t){ value=v; expectedItem=i; ts=t; }
        boolean isExpired(){ return isExpired(System.currentTimeMillis()); }
        boolean isExpired(long now){ return now - ts > STASH_TTL_MS; }
    }
}
