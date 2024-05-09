package survivalblock.phantoms_do_stuff.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import survivalblock.phantoms_do_stuff.common.packets.SyncConfigPacket;

public class PhantomsDoStuffClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncConfigPacket.ID, (client, handler, buf, sender) -> SyncConfigPacket.handler(handler, buf));
	}
}