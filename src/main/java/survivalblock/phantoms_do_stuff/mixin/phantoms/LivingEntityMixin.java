package survivalblock.phantoms_do_stuff.mixin.phantoms;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuff;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@SuppressWarnings("UnreachableCode")
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void blockProjectilesIfBig(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        if ((LivingEntity) (Object) this instanceof PhantomEntity phantom && PhantomsDoStuffConfig.bigPhantomsBlockProjectiles && source.isIn(DamageTypeTags.IS_PROJECTILE) && PhantomsDoStuff.isABigPhantom(phantom)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D"))
    private double notAirShake(double original){
        if (!((LivingEntity) (Object) this instanceof PhantomEntity phantom)) {
            return original;
        }
        if (!PhantomsDoStuffConfig.bigPhantomsResistKnockback) {
            return original;
        }
        return Math.min(phantom.getPhantomSize() / 64d, 0.9d) + original;
    }
}
