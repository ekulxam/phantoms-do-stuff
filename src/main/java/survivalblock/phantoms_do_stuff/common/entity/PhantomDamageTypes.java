package survivalblock.phantoms_do_stuff.common.entity;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuff;

public class PhantomDamageTypes {
    public static final RegistryKey<DamageType> PHANTOM_ATTACK = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, PhantomsDoStuff.id("phantom_attack"));

    public static void init(){

    };
}
