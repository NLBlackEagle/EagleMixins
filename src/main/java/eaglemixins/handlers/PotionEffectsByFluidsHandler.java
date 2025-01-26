package eaglemixins.handlers;

import eaglemixins.util.Ref;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PotionEffectsByFluidsHandler {
    //Listener for player in SRP deadblood / BOP blood / BOP Hot Spring Water
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase != TickEvent.Phase.START) return;
        EntityPlayer player = event.player;
        World world = player.world;
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;

        List<ResourceLocation> blockColumn = new ArrayList<>();
        for(int i=-1; i<2; i++)
            blockColumn.add(world.getBlockState(player.getPosition().add(0,i,0)).getBlock().getRegistryName());
        blockColumn.add(world.getBlockState(player.getPosition().add(0,1.5,0)).getBlock().getRegistryName()); //idk why but alr then

        for(ResourceLocation location : blockColumn){
            if(location == null) continue;
            if(Ref.deadBloodReg.equals(location)) {
                for(int i=0; i<4; i++)
                    player.addPotionEffect(getEffect(i));
                return;
            } else if(location.toString().equals("biomesoplenty:blood")) {
                for(int i=4; i<6; i++)
                    player.addPotionEffect(getEffect(i));
                return;
            } else if(location.toString().equals("biomesoplenty:hot_spring_water")) {
                for(int i=6; i<11; i++)
                    player.addPotionEffect(getEffect(i));
                return;
            }
        }
    }

    private static final List<String> effectStrings = Arrays.asList(
            //Dead Blood
            "potioncore:dispel",
            "potioncore:weight",
            "minecraft:slowness",
            "srparasites:corrosive",
            //BOP Blood
            "elenaidodge:sluggish",
            "lycanitesmobs:aphagia",
            //Hot Spring Water
            "potioncore:potion_sickness",
            "potioncore:explode",
            "potioncore:launch",
            "minecraft:weakness",
            "simpledifficulty:hyperthermia"
    );
    private static final int[] durations = {100,100,100,100,100,100,200,1,1,100,100};
    private static final int[] amplifiers = {0,0,0,0,0,0,0,0,0,1,2};
    private static List<PotionEffect> effects = null;
    private static PotionEffect getEffect(int id) {
        if (effects == null) {
            effects = new ArrayList<>();
            for (int i = 0; i < effectStrings.size(); i++) {
                String effectString = effectStrings.get(i);
                Potion potion = Potion.getPotionFromResourceLocation(effectString);
                if (potion != null)
                    effects.add(new PotionEffect(potion, durations[i], amplifiers[i]));
                else
                    effects.add(new PotionEffect(MobEffects.INSTANT_DAMAGE,0,0));
            }
        }
        if (id >= 0 && id < effects.size())
            return new PotionEffect(effects.get(id));
        else
            return new PotionEffect(effects.get(0));
    }
}
