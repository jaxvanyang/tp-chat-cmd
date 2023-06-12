package jaxvanyang.tpchatcmd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.Math;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		addTpChatCmd();
	}

	private void addTpChatCmd() {
		ClientCommandRegistrationCallback.EVENT.register(
			(dispatcher, registryAccess) -> {
				dispatcher.register(
					literal("tpchat").executes(
						context -> {
							final String chatCommandTemplate = "tellraw @a %s";
							final String prefixTemplate = "<%s> ";
							final String tpTemplate = "/execute in %s run tp @s %.0f %.0f %.0f";
							final String coordsTemplate = "%s (%.0f %.0f %.0f)";
							final FabricClientCommandSource source = context.getSource();
							final ClientPlayerEntity player = source.getPlayer();
							final World world = source.getWorld();
							final String dimension = world.getRegistryKey().getValue().toString();
							final Vec3d pos = source.getPosition();
							double x = Math.round(pos.x);
							double y = Math.round(pos.y);
							double z = Math.round(pos.z);

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

							final String command = String.format(
								tpTemplate, dimension, x, y, z
							);
							final JsonObject clickEvent = new JsonObject();
							// final JsonObject hoverEvent = new JsonObject();
							final JsonObject clickableMsgJson = new JsonObject();
							final JsonArray msgJson = new JsonArray();

							clickEvent.addProperty("action", "run_command");
							clickEvent.addProperty("value", command);
							// hoverEvent.addProperty("action", "show_text");
							// hoverEvent.addProperty("contents", "X Y Z");
							clickableMsgJson.add("clickEvent", clickEvent);
							// clickableMsgJson.add("hoverEvent", hoverEvent);
							clickableMsgJson.addProperty(
								"text",
								String.format(coordsTemplate, dimensionName, x, y, z)
							);
							clickableMsgJson.addProperty("color", "green");
							clickableMsgJson.addProperty("underlined", true);

							msgJson.add(
								String.format(
									prefixTemplate,
									player.getName().getString()
								)
							);
							msgJson.add(clickableMsgJson);

							player.networkHandler.sendChatCommand(
								String.format(chatCommandTemplate, msgJson.toString())
							);

							return 0;
						}
					)
				);
			}
		);
	}
}
