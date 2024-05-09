package survivalblock.phantoms_do_stuff.mixin.phantoms;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.phantoms_do_stuff.access.RidingTimeAccess;

@Debug(export = true)
@Mixin(Entity.class)
public abstract class EntityMixin implements RidingTimeAccess {

    @Unique
    private int ridingTime;
    @Override
    public void phantoms_do_stuff$setRidingTime(int newRidingTime) {
        this.ridingTime = newRidingTime;
    }

    @Override
    public void phantoms_do_stuff$resetRidingTime() {
        phantoms_do_stuff$setRidingTime(0);
    }

    @Override
    public int phantoms_do_stuff$getRidingTime() {
        return this.ridingTime;
    }

    @Shadow public abstract float getHeight();

    @WrapOperation(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity$PositionUpdater;accept(Lnet/minecraft/entity/Entity;DDD)V"))
    private void yeahPhantomsCarryYouNow(Entity.PositionUpdater positionUpdater, Entity passenger, double x, double d, double z, Operation<Void> original){
        if(passenger.getVehicle() instanceof PhantomEntity && !(passenger instanceof PhantomEntity)){
            double offset = d - this.getHeight() / 2 - passenger.getMountedHeightOffset();
            positionUpdater.accept(passenger, x, offset, z);
        } else {
            original.call(positionUpdater, passenger, x, d, z);
        }
    }

    @Inject(method = "tickRiding", at = @At("TAIL"))
    private void updateRidingTime(CallbackInfo ci){
        phantoms_do_stuff$setRidingTime(phantoms_do_stuff$getRidingTime() + 1);
    }

    @Inject(method = "tickRiding", at = @At(value = "RETURN", ordinal = 0))
    private void resetRidingTime(CallbackInfo ci){
        phantoms_do_stuff$setRidingTime(0);
    }
}
