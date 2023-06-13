package jaxvanyang.tpchatcmd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public final class TpChatCommand {
	// TODO: find a constant in official API to replace this
	public final static int CHAT_LENTH_LIMIT = 256;

	public final static String CHAT_COMMAND_TEMPLATE = "tellraw @a %s";
	public final static String PREFIX_TEMPLATE = "<%s> ";
	public final static String TP_TEMPLATE = "/execute in %s run tp @s %.0f %.0f %.0f";
	public final static String COORDS_TEMPLATE = "%s (%.0f %.0f %.0f)";

	public static void register(
		CommandDispatcher<FabricClientCommandSource> dispatcher
	) {
		dispatcher.register(literal("tpchat")
			.executes(ctx -> tpchat(ctx.getSource())));
	}

	public static int tpchat(FabricClientCommandSource source) {
		// Context constants
		final Boolean isDedicated = source.getClient().getServer() == null;
		final ClientPlayerEntity player = source.getPlayer();
		final World world = source.getWorld();
		final Vec3d pos = source.getPosition();
		final String dimension = world.getRegistryKey().getValue().toString();
		final String playerName = player.getName().getString();

		double x = pos.x, y = pos.y, z = pos.z;

		String dimensionName = dimension;
		switch (dimension) {
			case "minecraft:overworld":
			dimensionName = "Overworld";
			break;
			case "minecraft:the_nether":
			dimensionName = "Nether";
			break;
			case "minecraft:the_end":
			dimensionName = "End";
			break;
		}

		final String tpCommand = String.format(
			TP_TEMPLATE, dimension, x, y, z
		);
		final String clickableTextString = String.format(
			COORDS_TEMPLATE, dimensionName, x, y, z
		);

		final JsonArray chatCommandJson = new JsonArray();
		final JsonObject clickableText = new JsonObject();
		final JsonObject clickEvent = new JsonObject();
		final JsonObject hoverEvent = new JsonObject();

		chatCommandJson.add(String.format(PREFIX_TEMPLATE, playerName));
		chatCommandJson.add(clickableText);

		clickableText.addProperty( "text", clickableTextString);
		clickableText.addProperty("color", "green");
		clickableText.addProperty("underlined", true);
		clickableText.add("clickEvent", clickEvent);

		clickEvent.addProperty("action", "run_command");
		clickEvent.addProperty("value", tpCommand);

		final String chatCommand = String.format(
			CHAT_COMMAND_TEMPLATE, chatCommandJson.toString()
		);
		final int chatCommandLength = chatCommand.length();

		// Refer to https://github.com/jaxvanyang/tp-chat-cmd/issues/5
		if (isDedicated && chatCommandLength > CHAT_LENTH_LIMIT) {
			source.sendFeedback(
				Text.literal(
					String.format(
						"The raw JSON text of your message exceeds the chat length limit." +
						"Yours is %d characters long, while the limit is %d.",
						chatCommandLength,
						CHAT_LENTH_LIMIT
					)
				)
			);

			return -1;
		}

		player.networkHandler.sendChatCommand(
			String.format(CHAT_COMMAND_TEMPLATE, chatCommandJson.toString())
		);

		// This commented line is from the Fabric Wiki, but symbol not found.
		// return Command.SINGLE_SUCCESS;
		return 1;
	}
}
