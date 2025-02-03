package eaglemixins.mixin.loadingscreens;

import com.bloodnbonesgaming.loadingscreens.client.LoadingScreenHandler;
import com.bloodnbonesgaming.loadingscreens.client.gui.GuiElementBase;
import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(LoadingScreenHandler.class)
public class LoadingScreenHandlerMixin {
    @Shadow(remap = false) private boolean setup;
    @Shadow(remap = false) private List<GuiElementBase> elements;

    @Shadow(remap = false)
    public void setup(final Minecraft minecraft) {
        EagleMixins.LOGGER.info("EagleMixins LoadingScreens Mixin: Failed to apply shadow setup()");
    }

    @Unique long lastTime = 0;
    @Unique long currentTime = 0;
    @Unique int currentScreenId = 0;
    @Unique List<Integer> screenIdOrder = null;

    /**
     * @author Nischhelm
     * @reason gotta fully change behavior
     */
    @Overwrite(remap = false)
    public void renderScreen(Minecraft minecraft, int width, int height) {
        if (!this.setup) {
            this.setup(minecraft);
            this.setup = true;
        }

        //Use sys time to slightly alleviate thread lag changing cycle speed
        long sysTime = System.currentTimeMillis();

        //Setup (random) order on first frame and after 2 minutes (assumption: new login without restart -> re-randomise)
        if (screenIdOrder == null || sysTime-lastTime > 120000) {
            screenIdOrder = new ArrayList<>();
            for (int i = 0; i < this.elements.size(); i++)
                screenIdOrder.add(i);
            //Randomise
            if (ForgeConfigHandler.client.randomOrder)
                Collections.shuffle(screenIdOrder);
        }

        //Cycling
        currentTime = sysTime - lastTime;
        lastTime = sysTime;
        if (currentTime > ForgeConfigHandler.client.frequency * 1000L) {
            currentTime -= ForgeConfigHandler.client.frequency * 1000L; //instead of setting to 0, try to keep a steady pace with lag
            currentScreenId++;
        }
        if (currentScreenId >= this.elements.size())
            currentScreenId = 0;

        //Random screen
        int randomisedIndex = ForgeConfigHandler.client.disableCycling ? screenIdOrder.get(0) : screenIdOrder.get(currentScreenId);

        //Cycle through available screens
        final GuiElementBase element = this.elements.get(randomisedIndex);
        GlStateManager.enableAlpha();
        element.render(Minecraft.getMinecraft(), width, height);
        GlStateManager.disableAlpha();
    }
}