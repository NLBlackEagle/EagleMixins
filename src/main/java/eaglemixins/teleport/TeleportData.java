package eaglemixins.teleport;

import net.minecraft.util.math.BlockPos;

public class TeleportData {
    /* Persisted fields */
    public BlockPos sender;
    public BlockPos receiver;
    private boolean senderIsDiscovered = false;
    private boolean receiverIsDiscovered = false;

    public TeleportData(BlockPos sender, BlockPos receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public void setDiscoveredSenderPos(BlockPos sender) {
        this.sender = sender;
        this.senderIsDiscovered = true;
    }

    public void setDiscoveredReceiverPos(BlockPos receiver) {
        this.receiver = receiver;
        this.receiverIsDiscovered = true;
    }

    public BlockPos getBlockPos(boolean requestSender){
        return requestSender ? this.sender : this.receiver;
    }

    public boolean isDiscovered(boolean requestSender){
        return requestSender ? this.senderIsDiscovered : this.receiverIsDiscovered;
    }
}
