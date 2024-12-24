package eaglemixins.handlers;


import eaglemixins.EagleMixins;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;


public class BerianHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {


        if (event.isCanceled() || !event.getEntity().getName().equals("Villager")) return;
        if (event.getEntity().getEntityData().getTag("SussyBerianNaming") == null) {

            int Random = (int)(Math.random()*100);
            //give entity a tag to make sure this script only iterates once per entity.
            event.getEntity().getEntityData().setString("SussyBerianNaming", String.valueOf(1));

           // NBTTagCompound ProfessionName = (NBTTagCompound)event.getEntity().getEntityData().getTag("ProfessionName");
           // NBTTagCompound Profession = (NBTTagCompound)event.getEntity().getEntityData().getTag("Profession");

           // EagleMixins.LOGGER.log(Level.INFO, "EagleMixins ProfessionName " + ProfessionName);
           // EagleMixins.LOGGER.log(Level.INFO, "EagleMixins Profession " + Profession);

            NBTTagCompound c = event.getEntity().getEntityData();

            if((c.hasKey("ProfessionName")) || (c.hasKey("Profession"))){

                EagleMixins.LOGGER.log(Level.INFO, "EagleMixins HasTag ");

            }

            if (Random < 5) {

                event.getEntity().setCustomNameTag("Sussyberian");

            } else if (Random < 10) {

                event.getEntity().setCustomNameTag("Mentalberian");

            }
        }
    }

}
