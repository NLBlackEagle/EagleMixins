package eaglemixins.blocks;

/**
 * Keyslave (keyslave.bbmodel): has no key of its own, vanishes together with any lock it is connected to when that lock is opened.
 * The alt keyslave behaves the same, but turns into the lock type's alt replacement block instead of the keyslave one.
 */
public class BlockLockSlave extends BlockLockFrame {

    public final boolean alt;

    public BlockLockSlave(boolean alt) {
        this.alt = alt;
    }
}
