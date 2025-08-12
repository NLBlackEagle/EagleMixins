package eaglemixins.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "eaglemixins", value = Side.CLIENT)
public final class DeepslateSoundTuner {

    private static final ResourceLocation TARGET_BLOCK_ID = new ResourceLocation("eaglemixins", "deepslate");
    private static Block TARGET; // lazy init

    private static Block target() {
        if (TARGET == null) {
            TARGET = ForgeRegistries.BLOCKS.getValue(TARGET_BLOCK_ID);
        }
        return TARGET;
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        ISound s = event.getSound();
        if (s == null) return;
        if (s.getCategory() != SoundCategory.BLOCKS) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.world == null) return;

        BlockPos pos = new BlockPos(
                MathHelper.floor(s.getXPosF()),
                MathHelper.floor(s.getYPosF()),
                MathHelper.floor(s.getZPosF())
        );
        if (!mc.world.isBlockLoaded(pos)) return;

        Block at = mc.world.getBlockState(pos).getBlock();
        if (at != target()) {
            at = mc.world.getBlockState(pos.down()).getBlock();
            if (at != target()) return;
        }

        event.setResultSound(wrapWithPitch(s, 0.85f));
    }

    private static ISound wrapWithPitch(final ISound original, final float factor) {
        return new ISound() {
            @Override public ResourceLocation getSoundLocation() { return original.getSoundLocation(); }
            @Nullable @Override public SoundEventAccessor createAccessor(SoundHandler handler) { return original.createAccessor(handler); }
            @Override public Sound getSound() { return original.getSound(); }
            @Override public SoundCategory getCategory() { return original.getCategory(); }
            @Override public boolean canRepeat() { return original.canRepeat(); }
            @Override public int getRepeatDelay() { return original.getRepeatDelay(); }
            @Override public float getVolume() { return original.getVolume(); }
            @Override public float getPitch() {
                try {
                    float out = original.getPitch() * factor;
                    return MathHelper.clamp(out, 0.5f, 2.0f);
                } catch (Throwable t) {
                    return MathHelper.clamp(factor, 0.5f, 2.0f);
                }
            }
            @Override public float getXPosF() { return original.getXPosF(); }
            @Override public float getYPosF() { return original.getYPosF(); }
            @Override public float getZPosF() { return original.getZPosF(); }
            @Override public AttenuationType getAttenuationType() { return original.getAttenuationType(); }
        };
    }
}