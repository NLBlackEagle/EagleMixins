package eaglemixins.entity.ai;

import eaglemixins.handlers.RetaliationHandler;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

/**
 * Melee attack for retaliating mobs that have none of their own (animals, villagers). Those mobs don't implement
 * attackEntityAsMob (it does nothing for them), so the hit deals a fixed amount of mob damage instead.
 */
public class EntityAIRetaliateMelee extends EntityAIAttackMelee {
    private final float damage;
    private final int cooldown;

    public EntityAIRetaliateMelee(EntityCreature creature, double speed, double damage, int cooldown) {
        super(creature, speed, true);
        this.damage = (float) damage;
        this.cooldown = cooldown;
    }

    @Override
    public boolean shouldExecute() {
        return RetaliationHandler.canFight(this.attacker) && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return RetaliationHandler.canFight(this.attacker) && super.shouldContinueExecuting();
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase target, double distanceSq) {
        if (distanceSq > this.getAttackReachSqr(target) || this.attackTick > 0) return;
        this.attackTick = this.cooldown;
        this.attacker.swingArm(EnumHand.MAIN_HAND);
        this.attacker.setLastAttackedEntity(target);
        if (this.damage > 0) target.attackEntityFrom(DamageSource.causeMobDamage(this.attacker), this.damage);
    }
}
