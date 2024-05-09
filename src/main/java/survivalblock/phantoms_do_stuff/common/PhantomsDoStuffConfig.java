package survivalblock.phantoms_do_stuff.common;

import eu.midnightdust.lib.config.MidnightConfig;

import java.util.ArrayList;
import java.util.List;

public class PhantomsDoStuffConfig extends MidnightConfig {
    @Entry
    public static boolean phantomsTargetEverything = true;
    @Entry
    public static boolean phantomsDropPassengers = true;
    @Entry
    public static boolean slightlyFasterPhantoms = true;
    @Entry
    public static boolean phantomHealthScalesWithSize = true;
    @Entry
    public static boolean phantomsTryStopDismount = true;
    @Entry
    public static boolean phantomsIgnoreArmor = true;
    @Entry
    public static boolean phantomsGrowAndHeal = true;
    @Entry
    public static boolean phantomsTransferFire = true;
    @Entry
    public static boolean phantomsPhaseThroughWalls = true;
    @Entry
    public static boolean bigPhantomsBlockProjectiles = true;
    @Entry
    public static boolean bigPhantomsResistKnockback = true;
    @Entry
    public static boolean noCancellingPhantomAttacks = false;
    @Entry
    public static boolean duplicatingPhantoms = true;
    @Entry
    public static boolean frogsEatEverything = false;
    @Entry
    public static boolean frogsInstakillEntities = false;
    @Entry
    public static boolean returnGiantAI = true;
    @Entry
    public static boolean giantsBurnInDaylight = false;
    @Entry
    public static boolean buffGiantAttributes = true;
    @Entry(min = 0.02, max = 1)
    public static double screamingGoatSpawnChance = 0.1;
    @Entry(min = 0.1)
    public static double goatRamKnockbackMultiplier = 2;

    public static List<Boolean> getConfigValues() {
        List<Boolean> configValues = new ArrayList<>();
        configValues.add(phantomsTargetEverything);
        return configValues;
    }
}