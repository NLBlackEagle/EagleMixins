package eaglemixins.mixin.vanilla.mobequipment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.MobEquipmentConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityZombie.class)
public abstract class EntityZombieMixin extends EntityLivingBase {
    public EntityZombieMixin(World worldIn) {
        super(worldIn);
    }

    @ModifyArg(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/Item;)V")
    )
    private Item eaglemixins_vanillaEntityZombie_setEquipmentBasedOnDifficulty_changeItem(Item itemIn){
        return MobEquipmentConfig.getRandomItem(this.getRNG(), true);
    }

    @ModifyExpressionValue(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F")
    )
    private float eaglemixins_vanillaEntityZombie_setEquipmentBasedOnDifficulty_changeChance(float original){
        return original / ForgeConfigHandler.mobequipment.baseZombieChanceMulti; //easier to do rand/multi < somevalue than rand < somevalue x multi how its setup in this code
    }
}
