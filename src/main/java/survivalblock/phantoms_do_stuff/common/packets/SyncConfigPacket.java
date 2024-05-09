package survivalblock.phantoms_do_stuff.common.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuff;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

import java.util.ArrayList;
import java.util.List;

public class SyncConfigPacket {
	public static final Identifier ID = PhantomsDoStuff.id("sync_box_trot_config");

	public static void send(MinecraftServer server) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeCollection(PhantomsDoStuffConfig.getConfigValues(), PacketByteBuf::writeBoolean);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ServerPlayNetworking.send(player, ID, buf);
		}
	}

	public static void handler(ClientPlayNetworkHandler handler, PacketByteBuf buf) {
		List<Boolean> configValues = buf.readCollection(ArrayList::new, PacketByteBuf::readBoolean);
		if (!configValues.equals(PhantomsDoStuffConfig.getConfigValues())) {
			handler.onDisconnected(Text.translatable("phantoms_do_stuff.multiplayer.configdesync"));
		}
	}
}
