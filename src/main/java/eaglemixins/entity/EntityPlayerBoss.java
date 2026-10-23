package eaglemixins.entity;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.PlayerBossConfig;
import eaglemixins.entity.ai.EntityAIBossFlight;
import eaglemixins.util.ItemStackParser;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Boss that looks like a player (or a vanilla skeleton/zombie, see Model) and fights with the config's AI,
 * by default skeleton AI (bow or melee depending on the main hand), optionally flying.
 * Everything comes from its PlayerBossConfig entry, selected with the BossId NBT tag.
 * Render settings are synced to clients in RENDER so the server config decides how it looks.
 */
public class EntityPlayerBoss extends EntityMob implements IRangedAttackMob {

    private static final DataParameter<NBTTagCompound> RENDER = EntityDataManager.createKey(EntityPlayerBoss.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.createKey(EntityPlayerBoss.class, DataSerializers.BOOLEAN);

    private final BossInfoServer bossInfo = new BossInfoServer(getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
    private final MeleeAttack meleeAttack = new MeleeAttack(this);
    private final BowAttack bowAttack = new BowAttack(this);

    private String bossId = "";
    private boolean initialized = false; // equipment and full health applied
    private boolean needsConfig = true;
    @Nullable private PlayerBossConfig.BossAI currentAI;
    @Nullable private PlayerBossConfig.BossEntry entry;

    public EntityPlayerBoss(World world) {
        super(world);
        setSize(0.6F, 1.8F);
        experienceValue = 50;
        enablePersistence();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(RENDER, new NBTTagCompound());
        dataManager.register(SWINGING_ARMS, false);
    }

    @Override
    protected void initEntityAI() {
        // Tasks come from the config's AI in applyAI, this runs before the fields are set
    }

    /** Replaces all tasks with the ones of the given AI, nothing happens if that AI is already set. */
    private void applyAI(PlayerBossConfig.BossAI ai) {
        if (ai == currentAI) return;
        currentAI = ai;
        for (EntityAITasks.EntityAITaskEntry task : new ArrayList<>(tasks.taskEntries)) tasks.removeTask(task.action);
        for (EntityAITasks.EntityAITaskEntry task : new ArrayList<>(targetTasks.taskEntries)) targetTasks.removeTask(task.action);
        switch (ai) {
            case PLAYER_BOSS:
                tasks.addTask(0, new EntityAISwimming(this));
                tasks.addTask(1, new EntityAIBossFlight(this));
                tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
                tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
                tasks.addTask(6, new EntityAILookIdle(this));
                targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
                targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
                setCombatTask();
                break;
        }
    }

    @Nullable
    public PlayerBossConfig.BossEntry getEntry() {
        return entry;
    }

    // ---- Config ----

    /** Applies the config entry: stats and looks every time, equipment and full health only on the first spawn. */
    private void applyConfig() {
        entry = ForgeConfigHandler.playerbosses.bosses.get(bossId);
        if (entry == null) {
            EagleMixins.LOGGER.warn("Player boss at {} has unknown BossId \"{}\", see the Player Bosses config", getPosition(), bossId);
            applyAI(PlayerBossConfig.BossAI.PLAYER_BOSS);
            return;
        }
        applyAI(entry.ai);
        setAttribute(SharedMonsterAttributes.MAX_HEALTH, entry.maxHealth);
        setAttribute(SharedMonsterAttributes.ARMOR, entry.armor);
        setAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS, entry.armorToughness);
        setAttribute(SharedMonsterAttributes.ATTACK_DAMAGE, entry.attackDamage);
        setAttribute(SharedMonsterAttributes.MOVEMENT_SPEED, entry.movementSpeed);
        setAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, entry.knockbackResistance);
        setAttribute(SharedMonsterAttributes.FOLLOW_RANGE, entry.followRange);
        isImmuneToFire = entry.fireImmune;
        experienceValue = entry.experience;
        if (!entry.name.isEmpty()) setCustomNameTag(entry.name);

        if (!initialized) {
            equip(EntityEquipmentSlot.MAINHAND, entry.equipment.mainhand);
            equip(EntityEquipmentSlot.OFFHAND, entry.equipment.offhand);
            equip(EntityEquipmentSlot.HEAD, entry.equipment.head);
            equip(EntityEquipmentSlot.CHEST, entry.equipment.chest);
            equip(EntityEquipmentSlot.LEGS, entry.equipment.legs);
            equip(EntityEquipmentSlot.FEET, entry.equipment.feet);
            setHealth(getMaxHealth());
            initialized = true;
        } else if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        }
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) setDropChance(slot, entry.equipment.dropChance);
        nameWeapon();

        bossInfo.setVisible(entry.bossBar.enabled);
        bossInfo.setColor(entry.bossBar.color);
        bossInfo.setOverlay(entry.bossBar.overlay);
        bossInfo.setDarkenSky(entry.bossBar.darkenSky);

        NBTTagCompound render = new NBTTagCompound();
        render.setString("Model", entry.model.name());
        render.setString("Skin", entry.skin);
        render.setBoolean("Slim", entry.slimArms);
        render.setFloat("Scale", entry.scale);
        render.setBoolean("Wings", entry.wings.enabled);
        render.setInteger("WingPrimary", ItemStackParser.parseColor(entry.wings.primaryColor));
        render.setInteger("WingSecondary", ItemStackParser.parseColor(entry.wings.secondaryColor));
        render.setString("Horns", entry.wings.horns.name());
        render.setInteger("HornColor", ItemStackParser.parseColor(entry.wings.hornColor));
        render.setString("Subtitle", entry.subtitle);
        render.setString("BossBar", bossInfo.getUniqueId().toString()); // lets the client find this boss' bar to draw the subtitle
        dataManager.set(RENDER, render);
        updateSize(entry.scale);

        setCombatTask();
    }

    private void setAttribute(IAttribute attribute, double value) {
        getEntityAttribute(attribute).setBaseValue(value);
    }

    private void equip(EntityEquipmentSlot slot, String item) {
        ItemStack stack = ItemStackParser.parse(item, "player boss " + bossId);
        if (!stack.isEmpty()) setItemStackToSlot(slot, stack);
    }

    /** Weapon Name, or with Announce Weapon the item's own name: vanilla only names renamed weapons in death messages. */
    private void nameWeapon() {
        ItemStack weapon = getHeldItemMainhand();
        if (weapon.isEmpty() || entry == null) return;
        if (!entry.weaponName.isEmpty()) weapon.setStackDisplayName(entry.weaponName);
        else if (entry.announceWeapon && !weapon.hasDisplayName()) weapon.setStackDisplayName(weapon.getDisplayName());
    }

    private void updateSize(float scale) {
        PlayerBossConfig.BossModel model = getBossModel();
        setSize(model.width * scale, model.height * scale);
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && needsConfig) {
            needsConfig = false;
            applyConfig();
        }
        super.onLivingUpdate();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (RENDER.equals(key) && world.isRemote) updateSize(getRenderScale());
    }

    // ---- Client render data ----

    public NBTTagCompound getRenderData() {
        return dataManager.get(RENDER);
    }

    /** The config's Model, synced in the render data so both sides know it */
    public PlayerBossConfig.BossModel getBossModel() {
        try {
            return PlayerBossConfig.BossModel.valueOf(getRenderData().getString("Model"));
        } catch (IllegalArgumentException e) {
            return PlayerBossConfig.BossModel.PLAYER;
        }
    }

    public float getRenderScale() {
        NBTTagCompound render = getRenderData();
        return render.hasKey("Scale") ? render.getFloat("Scale") : 1.0F;
    }

    public boolean isSwingingArms() {
        return dataManager.get(SWINGING_ARMS);
    }

    // ---- Combat ----

    /** Bow AI with a bow in the main hand, melee otherwise, like skeletons. */
    public void setCombatTask() {
        if (world == null || world.isRemote || currentAI != PlayerBossConfig.BossAI.PLAYER_BOSS) return;
        tasks.removeTask(meleeAttack);
        tasks.removeTask(bowAttack);
        if (isHoldingBow()) {
            bowAttack.setAttackCooldown(entry != null ? entry.bowInterval : 20);
            tasks.addTask(4, bowAttack);
        } else {
            tasks.addTask(4, meleeAttack);
        }
    }

    public boolean isHoldingBow() {
        return getHeldItemMainhand().getItem() instanceof ItemBow;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slot, ItemStack stack) {
        super.setItemStackToSlot(slot, stack);
        if (slot == EntityEquipmentSlot.MAINHAND) setCombatTask();
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityTippedArrow arrow = new EntityTippedArrow(world, this);
        arrow.setEnchantmentEffectsFromEntity(this, distanceFactor);
        arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
        double dx = target.posX - posX;
        double dy = target.getEntityBoundingBox().minY + target.height / 3.0F - arrow.posY;
        double dz = target.posZ - posZ;
        double horizontal = MathHelper.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontal * 0.2D, dz, 1.6F, 14 - world.getDifficulty().getId() * 4);
        if (entry != null) arrow.setDamage(arrow.getDamage() * entry.arrowDamageMultiplier);
        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntity(arrow);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        dataManager.set(SWINGING_ARMS, swingingArms);
    }

    /** Squared melee reach to the target: the configured Attack Reach, or vanilla mob reach. */
    public double getMeleeReachSqr(EntityLivingBase target) {
        if (entry != null && entry.attackReach > 0) return entry.attackReach * entry.attackReach;
        return width * 2.0F * width * 2.0F + target.width;
    }

    private static class MeleeAttack extends EntityAIAttackMelee {
        private final EntityPlayerBoss boss;

        MeleeAttack(EntityPlayerBoss boss) {
            super(boss, 1.2D, false);
            this.boss = boss;
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase target) {
            return boss.getMeleeReachSqr(target);
        }

        /** Raises the arms like a melee skeleton, the player model ignores it without a bow */
        @Override
        public void startExecuting() {
            super.startExecuting();
            boss.setSwingingArms(true);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            boss.setSwingingArms(false);
        }
    }

    /** Vanilla's bow AI only accepts minecraft:bow, this accepts any bow (e.g. the SRP sentient bow). */
    private static class BowAttack extends EntityAIAttackRangedBow<EntityPlayerBoss> {
        private final EntityPlayerBoss boss;

        BowAttack(EntityPlayerBoss boss) {
            super(boss, 1.0D, 20, 15.0F);
            this.boss = boss;
        }

        @Override
        protected boolean isBowInMainhand() {
            return boss.isHoldingBow();
        }
    }

    // ---- Boss bar ----

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        bossInfo.setPercent(getHealth() / getMaxHealth());
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        bossInfo.removePlayer(player);
    }

    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        bossInfo.setName(getDisplayName());
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    // ---- Misc ----

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (entry == null || !entry.fallImmune) super.fall(distance, damageMultiplier);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    /** Spawners also check this. Monsters normally need darkness, bosses should spawn in any light. */
    @Override
    public boolean getCanSpawnHere() {
        return world.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    // ---- Sounds, from the mob the Model copies ----

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        switch (getBossModel()) {
            case SKELETON: return SoundEvents.ENTITY_SKELETON_AMBIENT;
            case WITHER_SKELETON: return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
            case STRAY: return SoundEvents.ENTITY_STRAY_AMBIENT;
            case ZOMBIE: return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
            case HUSK: return SoundEvents.ENTITY_HUSK_AMBIENT;
            default: return null;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        switch (getBossModel()) {
            case SKELETON: return SoundEvents.ENTITY_SKELETON_HURT;
            case WITHER_SKELETON: return SoundEvents.ENTITY_WITHER_SKELETON_HURT;
            case STRAY: return SoundEvents.ENTITY_STRAY_HURT;
            case ZOMBIE: return SoundEvents.ENTITY_ZOMBIE_HURT;
            case HUSK: return SoundEvents.ENTITY_HUSK_HURT;
            default: return SoundEvents.ENTITY_PLAYER_HURT;
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        switch (getBossModel()) {
            case SKELETON: return SoundEvents.ENTITY_SKELETON_DEATH;
            case WITHER_SKELETON: return SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
            case STRAY: return SoundEvents.ENTITY_STRAY_DEATH;
            case ZOMBIE: return SoundEvents.ENTITY_ZOMBIE_DEATH;
            case HUSK: return SoundEvents.ENTITY_HUSK_DEATH;
            default: return SoundEvents.ENTITY_PLAYER_DEATH;
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        SoundEvent step;
        switch (getBossModel()) {
            case SKELETON: step = SoundEvents.ENTITY_SKELETON_STEP; break;
            case WITHER_SKELETON: step = SoundEvents.ENTITY_WITHER_SKELETON_STEP; break;
            case STRAY: step = SoundEvents.ENTITY_STRAY_STEP; break;
            case ZOMBIE: step = SoundEvents.ENTITY_ZOMBIE_STEP; break;
            case HUSK: step = SoundEvents.ENTITY_HUSK_STEP; break;
            default: super.playStepSound(pos, block); return;
        }
        playSound(step, 0.15F, 1.0F);
    }

    // ---- Loot ----

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return entry == null || entry.lootTable.isEmpty() ? null : new ResourceLocation(entry.lootTable);
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        super.dropLoot(wasRecentlyHit, lootingModifier, source);
        if (entry == null) return;
        for (PlayerBossConfig.Drop drop : entry.drops) {
            if (rand.nextFloat() >= drop.chance + drop.lootingBonus * lootingModifier) continue;
            ItemStack stack = ItemStackParser.parse(drop.item, "player boss " + bossId + " drops");
            if (stack.isEmpty()) continue;
            int count = drop.max > drop.min ? drop.min + rand.nextInt(drop.max - drop.min + 1) : drop.min;
            if (count <= 0) continue;
            stack.setCount(count);
            entityDropItem(stack, 0.0F);
        }
    }

    // ---- NBT ----

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("BossId", bossId);
        compound.setBoolean("BossInitialized", initialized);
    }

    /**
     * A new boss (from a spawner or /summon) gets its config's Extra NBT here, before it joins the world,
     * so other mods (Champions, Infernal Mobs) see it like spawner NBT. The given NBT wins over the config.
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (!compound.getBoolean("BossInitialized")) {
            PlayerBossConfig.BossEntry config = ForgeConfigHandler.playerbosses.bosses.get(compound.getString("BossId"));
            if (config != null && !config.extraNbt.trim().isEmpty()) {
                try {
                    NBTTagCompound merged = JsonToNBT.getTagFromJson(config.extraNbt.trim());
                    merged.merge(compound);
                    compound = merged;
                } catch (NBTException e) {
                    EagleMixins.LOGGER.error("Invalid Extra NBT for player boss \"{}\": {}", compound.getString("BossId"), e.getMessage());
                }
            }
        }
        super.readFromNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        bossId = compound.getString("BossId");
        initialized = compound.getBoolean("BossInitialized");
        needsConfig = true;
        if (bossInfo != null) bossInfo.setName(getDisplayName());
    }
}
