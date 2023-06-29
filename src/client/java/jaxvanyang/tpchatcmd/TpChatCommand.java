package jaxvanyang.tpchatcmd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public final class TpChatCommand {
	// TODO: find a constant in official API to replace this
	public final static int CHAT_LENTH_LIMIT = 256;

	public final static String MINECRAFT_PREFIX = "minecraft:";
	public final static int MINECRAFT_PREFIX_LEN = MINECRAFT_PREFIX.length();

	public final static String CHAT_COMMAND_TEMPLATE = "tellraw @a %s";
	public final static String PREFIX_TEMPLATE = "<%s> ";
	public final static String POSTFIX_TEMPLATE = ": %s";
	public final static String TP_TEMPLATE = "/execute in %s run tp %.0f %.0f %.0f";
	public final static String COORDS_TEMPLATE = "%s (%.0f %.0f %.0f)";

	private final static String LOCATION_ARGNAME = "location";
	private final static String DESC_ARGNAME = "description";

	// Workaround for BlockPosArgumentType.getBlockPos() not support
	// FabricClientCommandSource
	private static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
		final ServerCommandSource dummyServerCommandSource = new ServerCommandSource(null,
			context.getSource().getPosition(),
			null, null, 0, null, null, null, null);

		return context.getArgument(name, PosArgument.class).toAbsoluteBlockPos(dummyServerCommandSource);
	}

	public static void register(
		CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("tpchat")
			.executes(ctx -> tpchat(ctx.getSource(), null, null))
			.then(argument(LOCATION_ARGNAME, BlockPosArgumentType.blockPos())
				.executes(
					ctx -> tpchat(ctx.getSource(), getBlockPos(ctx, LOCATION_ARGNAME), null))
				.then(argument(DESC_ARGNAME, StringArgumentType.greedyString())
					.executes(
						ctx -> tpchat(ctx.getSource(), getBlockPos(ctx, LOCATION_ARGNAME),
							getString(ctx, DESC_ARGNAME))))));
	}

	public static int tpchat(FabricClientCommandSource source, BlockPos location, String desc) {
		// Context constants
		final Boolean isDedicated = source.getClient().getServer() == null;
		final ClientPlayerEntity player = source.getPlayer();
		final World world = source.getWorld();
		final Vec3d pos = source.getPosition();
		String dimension = world.getRegistryKey().getValue().toString();
		final String playerName = player.getName().getString();

		double x = pos.x, y = pos.y, z = pos.z;

		if (location != null) {
			x = location.getX();
			y = location.getY();
			z = location.getZ();
		}

		if (dimension.startsWith(MINECRAFT_PREFIX)) {
			dimension = dimension.substring(MINECRAFT_PREFIX_LEN);
		}
		String dimensionName = dimension;
		switch (dimension) {
			case "overworld":
			dimensionName = "Overworld";
			break;

			case "the_nether":
			dimensionName = "Nether";
			break;

			case "the_end":
			dimensionName = "End";
			break;
		}

		final String tpCommand = String.format(
			TP_TEMPLATE, dimension, x, y, z);
		final String clickableTextString = String.format(
			COORDS_TEMPLATE, dimensionName, x, y, z);

		final JsonArray chatCommandJson = new JsonArray();
		final JsonObject clickableText = new JsonObject();
		final JsonObject clickEvent = new JsonObject();

		chatCommandJson.add(String.format(PREFIX_TEMPLATE, playerName));
		chatCommandJson.add(clickableText);
		if (desc != null) {
			chatCommandJson.add(String.format(POSTFIX_TEMPLATE, desc));
		}

		clickableText.addProperty("text", clickableTextString);
		clickableText.addProperty("color", "green");
		clickableText.addProperty("underlined", true);
		clickableText.add("clickEvent", clickEvent);

		clickEvent.addProperty("action", "run_command");
		clickEvent.addProperty("value", tpCommand);

		final String chatCommand = String.format(
			CHAT_COMMAND_TEMPLATE, chatCommandJson.toString());
		final int chatCommandLength = chatCommand.length();

		// Refer to https://github.com/jaxvanyang/tp-chat-cmd/issues/5
		if (isDedicated && chatCommandLength > CHAT_LENTH_LIMIT) {
			source.sendError(
				Text.literal(
					String.format(
						"""
						[ERROR] /tpchat failed: Chat message too long.
						Technically, The raw Json text of your message exceeds
						the chat length limit. Yours is %d characters long,
						while the limit is %d. Try to reduce your description.
						""",
						chatCommandLength,
						CHAT_LENTH_LIMIT)));

			return -1;
		}

		player.networkHandler.sendChatCommand(
			String.format(CHAT_COMMAND_TEMPLATE, chatCommandJson.toString()));

		// This commented line is from the Fabric Wiki, but symbol not found.
		// return Command.SINGLE_SUCCESS;
		return 1;
	}
}
