package survivalblock.phantoms_do_stuff.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.phantoms_do_stuff.access.RidingTimeAccess;

import java.util.*;

@Debug(export = true)
@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {

	@Shadow public abstract int getPhantomSize();

	public PhantomEntityMixin(EntityType<? extends PhantomEntity> entityType, World world) {
		super(entityType, world); // tell the compiler to stop being annoying
	}

	/**
	 * Drops passengers sometimes and randomly.
	 * Phantoms are at least 3x likely (by likely I mean run a check 3x more frequently)
	 * 	to drop their passengers while over the world height limit
	 * Bigger phantoms are less likely to drop their passengers
	 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void dropPassenger(CallbackInfo ci){
		List<Entity> passengerList = new ArrayList<>(this.getPassengerList());
		for (Iterator<Entity> iterator = passengerList.iterator(); iterator.hasNext(); ) {
			Entity passenger = iterator.next();
			if (!passenger.hasVehicle() || !Objects.equals(passenger.getVehicle(), this)) {
				iterator.remove();
				continue;
			}
			World world = passenger.getWorld();
			if(world.isClient()){
				continue;
			}
			int ridingTime = ((RidingTimeAccess) passenger).phantoms_do_stuff$getRidingTime();
			if (ridingTime != 0 && ((!world.isInBuildLimit(passenger.getBlockPos()) && ridingTime % 25 == 0) || ridingTime % (100 + this.getPhantomSize() * 4) == 0)) {
				int shouldDrop = MathHelper.nextBetween(world.getRandom(), 0, 1);
				if(shouldDrop == 0){
					passenger.stopRiding();
					iterator.remove();
                }
            }
		}
	}

	/**
	 * Increases phantom health with size
	 */
	@Inject(method = "onSizeChanged()V", at = @At("TAIL"))
	private void updateMaxHealth(CallbackInfo ci){
		final UUID MAX_HEALTH_ID = UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC"); // vanilla health boost
		Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(MAX_HEALTH_ID, "phantom heal increase", this.getPhantomSize(), EntityAttributeModifier.Operation.ADDITION));
		float saveHealthDiff = (float) this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - this.getHealth();
		attributeModifiers = builder.build();
		this.getAttributes().addTemporaryModifiers(attributeModifiers);
		this.setHealth((float) (this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - saveHealthDiff));
	}

	@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$PhantomMoveControl")
	abstract static class PhantomMoveControlMixin extends MoveControl {

		public PhantomMoveControlMixin(MobEntity owner) {
			super(owner);
		}

		/**
		 * Attempts to slightly increase the phantom's speed, smaller = faster
		 * @param instance the phantom
		 * @param vec3d the original velocity being applied to the phantom
		 * @param original the original operation (setVelocity)
		 */
		@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
		private void iAmSpeed(PhantomEntity instance, Vec3d vec3d, Operation<Void> original){
			Entity entity = this.entity;
			if (entity instanceof PhantomEntity phantom) {
				int invertedSizeScale = 64 - phantom.getPhantomSize();
				float multiplier = 1f + (float) ((float) invertedSizeScale / 2048);
				original.call(instance, vec3d.multiply(multiplier));
				return;
			}
			original.call(instance, vec3d);
			return;
		}
	}

	@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$SwoopMovementGoal")
	abstract static class SwoopMovementGoalMixin {

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
		 * Spawns a small phantom if the phantom's size >= 64
		 * @param attacker the phantom
		 * @param target the target to become passenger
		 * @param original the original method call (did the attack succeed?)
		 * @return original
		 */
		@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;tryAttack(Lnet/minecraft/entity/Entity;)Z"))
        private boolean youRaiseMeUp(PhantomEntity attacker, Entity target, Operation<Boolean> original) {
			boolean result = original.call(attacker, target);
			if(result && !Objects.equals(target.getVehicle(), attacker)){
				((RidingTimeAccess) target).phantoms_do_stuff$resetRidingTime();
				target.startRiding(attacker, true); // become passenger
				World world = attacker.getWorld();
				if(world instanceof ServerWorld serverWorld && attacker.getPhantomSize() >= 64){
					PhantomEntity newPhantom = EntityType.PHANTOM.create(world);
					if (newPhantom == null) return result; // return true;
					// copy nbt here
					newPhantom.setPhantomSize(0);
					newPhantom.refreshPositionAndAngles(target.getBlockPos(), 0.0f, 0.0f);
					serverWorld.spawnEntityAndPassengers(newPhantom);
				}
			}
			return result;
		}
	}
}
