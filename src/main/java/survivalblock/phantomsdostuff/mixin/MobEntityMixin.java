package survivalblock.phantomsdostuff.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.phantomsdostuff.PhantomDamageTypes;

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
            return target.damage(new DamageSource(ENTRY, phantom), f);
        }
        return original;
    }
}
