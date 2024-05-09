package survivalblock.phantoms_do_stuff.mixin.giants;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GiantEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantoms_do_stuff.access.BreakDoorsAccess;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@SuppressWarnings("UnreachableCode")
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyReturnValue(method = "getGroup", at = @At("RETURN"))
    private EntityGroup giantsAreUndeadToo(EntityGroup original){
        if (!((LivingEntity) (Object) this instanceof GiantEntity giant)) {
            return original;
        }
        if (!PhantomsDoStuffConfig.returnGiantAI) {
            return original;
        }
        return ((BreakDoorsAccess) giant).phantoms_do_stuff$getZombieDelegate().getGroup();
    }
}
