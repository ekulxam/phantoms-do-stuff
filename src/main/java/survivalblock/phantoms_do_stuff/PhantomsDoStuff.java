package survivalblock.phantoms_do_stuff;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomsDoStuff implements ModInitializer {
	public static final String MOD_ID = "phantoms_do_stuff";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		if(FabricLoader.getInstance().isDevelopmentEnvironment()){
			LOGGER.debug("Phantoms do stuff now");
		}
		LOGGER.info("Act I: The Phantom Menace");
		PhantomDamageTypes.init();
	}
}