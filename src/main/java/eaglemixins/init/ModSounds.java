package eaglemixins.init;

import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "eaglemixins")
public final class ModSounds {

    public static final SoundEvent DEEPSLATE_BREAK = create("block.deepslate.break");
    public static final SoundEvent DEEPSLATE_STEP = create("block.deepslate.step");
    public static final SoundEvent DEEPSLATE_PLACE = create("block.deepslate.place");
    public static final SoundEvent DEEPSLATE_HIT = create("block.deepslate.hit");
    public static final SoundEvent DEEPSLATE_FALL = create("block.deepslate.fall");

    // Same as vanilla 1.17+ deepslate. Sound events are created here so blocks can reference them before the sound registry fires
    public static final SoundType DEEPSLATE = new SoundType(1.0F, 1.0F, DEEPSLATE_BREAK, DEEPSLATE_STEP, DEEPSLATE_PLACE, DEEPSLATE_HIT, DEEPSLATE_FALL);

    // Developer bosses, used through the player boss config's Sounds
    public static final SoundEvent DEVELOPER_IDLE = create("entity.developer.idle");
    public static final SoundEvent DEVELOPER_TARGET = create("entity.developer.target");
    public static final SoundEvent DEVELOPER_RESPOND = create("entity.developer.respond");
    public static final SoundEvent DEVELOPER_PAIN = create("entity.developer.pain");
    public static final SoundEvent DEVELOPER_DEATH = create("entity.developer.death");

    private static SoundEvent create(String name) {
        ResourceLocation location = new ResourceLocation("eaglemixins", name);
        return new SoundEvent(location).setRegistryName(location);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> e) {
        e.getRegistry().registerAll(DEEPSLATE_BREAK, DEEPSLATE_STEP, DEEPSLATE_PLACE, DEEPSLATE_HIT, DEEPSLATE_FALL,
                DEVELOPER_IDLE, DEVELOPER_TARGET, DEVELOPER_RESPOND, DEVELOPER_PAIN, DEVELOPER_DEATH);
    }
}
