package eaglemixins.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityWolf;

/**
 * Wraps one of a wolf's own targeting AIs (vanilla: hunt sheep/rabbits, attack skeletons) so it only runs while the wolf
 * has no pack alpha. Pack members don't pick targets themselves, they copy their alpha.
 */
public class EntityAIPackGate extends EntityAIBase {
    private final EntityWolf wolf;
    private final EntityAIBase delegate;

    public EntityAIPackGate(EntityWolf wolf, EntityAIBase delegate) {
        this.wolf = wolf;
        this.delegate = delegate;
        this.setMutexBits(delegate.getMutexBits());
    }

    private boolean allowed() {
        EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
        return pack == null || pack.getPackAlpha() == null;
    }

    @Override
    public boolean shouldExecute() {
        return allowed() && delegate.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return allowed() && delegate.shouldContinueExecuting();
    }

    @Override
    public boolean isInterruptible() {
        return delegate.isInterruptible();
    }

    @Override
    public void startExecuting() {
        delegate.startExecuting();
    }

    @Override
    public void resetTask() {
        delegate.resetTask();
    }

    @Override
    public void updateTask() {
        delegate.updateTask();
    }
}
