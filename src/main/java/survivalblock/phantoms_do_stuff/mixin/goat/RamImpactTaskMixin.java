package survivalblock.phantoms_do_stuff.mixin.goat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.ai.brain.task.RamImpactTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@Mixin(RamImpactTask.class)
public class RamImpactTaskMixin {

    @ModifyExpressionValue(method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/GoatEntity;J)V", at = @At(value = "INVOKE", target = "Ljava/util/function/ToDoubleFunction;applyAsDouble(Ljava/lang/Object;)D"))
    private double increaseRamKnockback(double original){
        if (Math.abs(PhantomsDoStuffConfig.goatRamKnockbackMultiplier - 1) <= 0.001) {
            return original;
        }
        return original * PhantomsDoStuffConfig.goatRamKnockbackMultiplier;
    }
}
