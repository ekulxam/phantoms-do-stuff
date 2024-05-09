package survivalblock.phantoms_do_stuff.mixin.frogs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FrogEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@SuppressWarnings("UnreachableCode")
@Mixin(MobEntity.class)
public class MobEntityMixin {

    @ModifyExpressionValue(method = "tryAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", ordinal = 0))
    private double superFrog(double original){
        if (!((MobEntity) (Object) this instanceof FrogEntity)) {
            return original;
        }
        return PhantomsDoStuffConfig.frogsInstakillEntities ? Float.MAX_VALUE : original;
    }

    @ModifyReturnValue(method = "tryAttack", at = @At("RETURN"))
    private boolean removeTargetIfNotDead(boolean original, Entity target){
        if (!((MobEntity) (Object) this instanceof FrogEntity)) {
            return original;
        }
        if (PhantomsDoStuffConfig.frogsInstakillEntities && target.isAlive()) {
            target.remove(Entity.RemovalReason.KILLED);
            return true;
        }
        return original;
    }
}
