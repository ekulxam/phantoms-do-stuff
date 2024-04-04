package survivalblock.phantoms_do_stuff.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantoms_do_stuff.PhantomDamageTypes;

@Debug(export = true)
@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    protected MobEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "tryAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean phantomsIgnoreArmor(boolean original, Entity target, @Local(ordinal = 0) float f){
        if (((Object) this) instanceof PhantomEntity phantom) {
            RegistryEntry<DamageType> ENTRY = phantom.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(PhantomDamageTypes.PHANTOM_ATTACK);
            World world = phantom.getWorld();
            int shouldHeal = MathHelper.nextBetween(world.getRandom(), 0, 3);
            if(shouldHeal >= 2 && target instanceof PlayerEntity playerTarget){
                float hunger = playerTarget.getHungerManager().getFoodLevel();
                int hungerStolen = Math.round(hunger / shouldHeal);
                playerTarget.getHungerManager().setFoodLevel(hungerStolen);
                phantom.heal(hungerStolen);
                if (hungerStolen > 10){
                    phantom.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200));
                }
                phantom.setPhantomSize(phantom.getPhantomSize() + shouldHeal-1);
            }
            return target.damage(new DamageSource(ENTRY, phantom), f);
        }
        return original;
    }
}
