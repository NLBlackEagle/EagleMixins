package eaglemixins.entity.ai;

import eaglemixins.handlers.RetaliationHandler;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIHurtByTarget;

/**
 * Targets whoever hurt the mob and calls nearby mobs of the same kind for help.
 * Tamed mobs don't turn on their owner (or anyone else) through this.
 */
public class EntityAIRetaliateTarget extends EntityAIHurtByTarget {

    public EntityAIRetaliateTarget(EntityCreature creature) {
        super(creature, true);
    }

    @Override
    public boolean shouldExecute() {
        return !RetaliationHandler.isTamed(this.taskOwner) && super.shouldExecute();
    }
}
