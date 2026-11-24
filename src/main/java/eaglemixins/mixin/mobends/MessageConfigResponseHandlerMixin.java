package eaglemixins.mixin.mobends;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.logging.Logger;

/**
 * Mo' Bends logs "Received Mo' Bends server configuration." with every shared setting each time the server sends it
 * (on every world join, sometimes twice). It goes through java.util.logging, so it shows up as stderr in the log.
 * This only drops that message, Mo' Bends' other logging is untouched.
 */
@Pseudo
@Mixin(targets = "goblinbob.mobends.core.network.msg.MessageConfigResponse$Handler", remap = false)
public abstract class MessageConfigResponseHandlerMixin {
    @WrapWithCondition(
            method = "onMessage(Lgoblinbob/mobends/core/network/msg/MessageConfigResponse;Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;)Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;",
            at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V")
    )
    private boolean eaglemixins$silenceConfigResponseLog(Logger logger, String message) {
        return false;
    }
}
