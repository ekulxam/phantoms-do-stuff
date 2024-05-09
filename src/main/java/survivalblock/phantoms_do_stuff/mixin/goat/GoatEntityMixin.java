package survivalblock.phantoms_do_stuff.mixin.goat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.passive.GoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@Mixin(GoatEntity.class)
public class GoatEntityMixin {

    @ModifyExpressionValue(method = {"createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/PassiveEntity;", "initialize"}, at = @At(value = "CONSTANT", args = "doubleValue=0.02"))
    private double increaseScreamingGoatSpawnChance(double original){
        return PhantomsDoStuffConfig.screamingGoatSpawnChance;
    }
}
