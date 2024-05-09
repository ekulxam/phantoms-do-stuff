package survivalblock.phantoms_do_stuff.mixin.frogs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FrogEntity;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@Debug(export = true)
@Mixin(FrogEntity.class)
public class FrogEntityMixin {

    @ModifyReturnValue(method = "isValidFrogFood", at = @At("RETURN"))
    private static boolean returnYes(boolean original, LivingEntity entity){
        return (PhantomsDoStuffConfig.frogsEatEverything || original) && !(entity instanceof FrogEntity);
    }
}
