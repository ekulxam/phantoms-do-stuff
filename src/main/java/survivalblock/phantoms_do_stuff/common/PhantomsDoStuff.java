package survivalblock.phantoms_do_stuff.common;

import eu.midnightdust.lib.config.MidnightConfig;
import fuzs.puzzleslib.api.event.v1.server.ServerTickEvents;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import survivalblock.phantoms_do_stuff.common.entity.PhantomDamageTypes;
import survivalblock.phantoms_do_stuff.common.packets.SyncConfigPacket;

public class PhantomsDoStuff implements ModInitializer {
	public static final String MOD_ID = "phantoms_do_stuff";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final int PHANTOM_DISMOUNT_DELAY = 10;
	int timer = 600;

    public static Identifier id(String id) {
		return new Identifier(MOD_ID, id);
    }

    @Override
	public void onInitialize() {

		MidnightConfig.init(MOD_ID, PhantomsDoStuffConfig.class);
		if(FabricLoader.getInstance().isDevelopmentEnvironment()){
			LOGGER.debug("Phantoms do stuff now");
		}
		LOGGER.info("Act I: The Phantom Menace");
		PhantomDamageTypes.init();
		ServerTickEvents.END.register((server -> {
			timer--;
			if (timer <= 0) {
				timer = 600;
				SyncConfigPacket.send(server);
			}
		}));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SyncConfigPacket.send(server);
		});
	}

	public static boolean isABigPhantom(PhantomEntity phantom) {
		return phantom.getPhantomSize() > 48;
	}
}