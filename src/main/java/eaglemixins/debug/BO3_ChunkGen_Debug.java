package eaglemixins.debug;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import eaglemixins.EagleMixins;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.*;

public class BO3_ChunkGen_Debug {
    public static Multimap<String, Integer> ticksPerStructSuccess = ArrayListMultimap.create();
    public static Multimap<String, Integer> ticksPerStructFail = ArrayListMultimap.create();

    @SubscribeEvent
    public static void onWorldClose(PlayerEvent.PlayerLoggedOutEvent event){
        if(event.player.world.isRemote) return;
        if(event.player.world.provider.getDimension() != 0) return;
        EagleMixins.LOGGER.info("Writing BO3 Generation Durations!");

        ticksPerStructSuccess.asMap().forEach((name, coll) -> EagleMixins.LOGGER.info("Success {} {} {} {}", name, coll.stream().mapToInt(Integer::intValue).average().orElse(0), getMedian(coll), coll.size()));
        ticksPerStructFail.asMap().forEach((name, coll) -> EagleMixins.LOGGER.info("Fail {} {} {} {}", name, coll.stream().mapToInt(Integer::intValue).average().orElse(0), getMedian(coll), coll.size()));
    }

    private static double getMedian(Collection<Integer> collection) {
        if(collection.isEmpty()) return 0;
        List<Integer> list = new ArrayList<>(collection);
        int middleEl = list.size()/2;
        if(list.size() % 2 == 1)
            return list.get(middleEl);
        return (list.get(middleEl - 1) + list.get(middleEl)) / 2.;
    }
}
