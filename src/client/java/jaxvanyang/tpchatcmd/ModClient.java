package jaxvanyang.tpchatcmd;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class ModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(
			(dispatcher, registryAccess) -> TpChatCommand.register(dispatcher));
	}
}
