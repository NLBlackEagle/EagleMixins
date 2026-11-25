package eaglemixins.entity.ai;

import eaglemixins.handlers.RetaliationHandler;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * Wraps a mob's panic AI: it only runs away while it has nobody to fight (babies, tamed mobs, or when burning without a target).
 * Vanilla panic starts on every hit, which would otherwise keep retaliating animals running instead of attacking.
 */
public class EntityAIPanicUnlessFighting extends EntityAIBase {
    private final EntityCreature creature;
    public final EntityAIBase panic;

    public EntityAIPanicUnlessFighting(EntityCreature creature, EntityAIBase panic) {
        this.creature = creature;
        this.panic = panic;
        this.setMutexBits(panic.getMutexBits());
    }

    private boolean isFighting() {
        return this.creature.getAttackTarget() != null && this.creature.getAttackTarget().isEntityAlive() && RetaliationHandler.canFight(this.creature);
    }

    @Override
    public boolean shouldExecute() {
        return !isFighting() && this.panic.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !isFighting() && this.panic.shouldContinueExecuting();
    }

    @Override
    public boolean isInterruptible() {
        return this.panic.isInterruptible();
    }

    @Override
    public void startExecuting() {
        this.panic.startExecuting();
    }

    @Override
    public void resetTask() {
        this.panic.resetTask();
    }

    @Override
    public void updateTask() {
        this.panic.updateTask();
    }
}
