package jaxvanyang.tpchatcmd;

import com.google.gson.JsonObject;
import java.lang.Math;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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
							final String commandTemplate = "/execute in %s run tp @s %.0f %.0f %.0f";
							final String textTemplate = "%s: %.0f %.0f %.0f";
							final FabricClientCommandSource source = context.getSource();
							final World world = source.getWorld();
							final String dimension = world.getRegistryKey().getValue().toString();
							final Vec3d pos = source.getPosition();
							double x = Math.round(pos.x);
							double y = Math.round(pos.y);
							double z = Math.round(pos.z);

							String dimensionName = dimension;
						switch (dimension) {
								case "minecraft:overworld":
								dimensionName = "Over World";
								break;
								case "minecraft:the_nether":
								dimensionName = "Nether";
								break;
								case "minecraft:the_end":
								dimensionName = "End";
								break;
							}

							final String command = String.format(
								commandTemplate, dimension, x, y, z
							);
							final JsonObject clickEvent = new JsonObject();
							// final JsonObject hoverEvent = new JsonObject();
							final JsonObject msgJson = new JsonObject();

							clickEvent.addProperty("action", "run_command");
							clickEvent.addProperty("value", command);
							// hoverEvent.addProperty("action", "show_text");
							// hoverEvent.addProperty("contents", "X Y Z");
							msgJson.add("clickEvent", clickEvent);
							// msgJson.add("hoverEvent", hoverEvent);
							msgJson.addProperty(
								"text",
								String.format(textTemplate, dimensionName, x, y, z)
							);
							msgJson.addProperty("color", "green");
							msgJson.addProperty("underlined", true);

							context.getSource().sendFeedback(
								Text.Serializer.fromJson(msgJson)
							);

							return 0;
						}
					)
				);
			}
		);
	}
}
