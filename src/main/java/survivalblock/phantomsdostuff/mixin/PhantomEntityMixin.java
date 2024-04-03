package survivalblock.phantomsdostuff.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.phantomsdostuff.access.RidingTimeAccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Debug(export = true)
@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {

	@Shadow public abstract int getPhantomSize();

	public PhantomEntityMixin(EntityType<? extends PhantomEntity> entityType, World world) {
		super(entityType, world); // tell the compiler to stop being annoying
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void dropPassenger(CallbackInfo ci){
		List<Entity> passengerList = new ArrayList<>(this.getPassengerList());
		for (Iterator<Entity> iterator = passengerList.iterator(); iterator.hasNext(); ) {
			Entity passenger = iterator.next();
			World world = passenger.getWorld();
			int ridingTime = ((RidingTimeAccess) passenger).phantoms_do_stuff$getRidingTime();
			if (ridingTime != 0 && ((!world.isInBuildLimit(passenger.getBlockPos()) && ridingTime % 25 == 0) || ridingTime  % (50 + this.getPhantomSize()) == 0)) {
				int shouldDrop = MathHelper.nextBetween(world.getRandom(), 0, 1);
				if(shouldDrop == 0){
					passenger.stopRiding();
					iterator.remove();
				}
			}
		}
	}

	@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$SwoopMovementGoal")
	public static class SwoopMovementGoalMixin {

		@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;tryAttack(Lnet/minecraft/entity/Entity;)Z"))
		private boolean attackCorrectly(PhantomEntity instance, Entity entity){
			List<Entity> passengerList = new ArrayList<>(instance.getPassengerList());
			for (Iterator<Entity> iterator = passengerList.iterator(); iterator.hasNext(); ) {
				Entity passenger = iterator.next();
				if(Objects.equals(passenger, entity)){
					iterator.remove();
					return false;
				}
			}
			return true;
		}

		/**
		 * Makes the phantom's target its passenger if the phantom's attack was successful
		 * @param attacker the phantom
		 * @param target the target to become passenger
		 * @param original the original method call (did the attack succeed?)
		 * @return original
		 */
		@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;tryAttack(Lnet/minecraft/entity/Entity;)Z"))
        private boolean youRaiseMeUp(PhantomEntity attacker, Entity target, Operation<Boolean> original) {
			boolean result = original.call(attacker, target);
			if(result && !Objects.equals(target.getVehicle(), attacker)){
				target.startRiding(attacker, true); // become passenger
			}
			return result;
		}
	}
}
