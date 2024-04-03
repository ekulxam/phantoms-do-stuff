package survivalblock.phantomsdostuff;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class PhantomDamageTypes {
    public static final RegistryKey<DamageType> PHANTOM_ATTACK = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(PhantomsDoStuff.MOD_ID, "phantom_attack"));

    public static void init(){

    };
}
