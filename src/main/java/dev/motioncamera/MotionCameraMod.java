package dev.motioncamera;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class MotionCameraMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("motioncamera");

	@Override
	public void onInitializeClient() {
		MotionCameraConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(MotionCameraState::tick);

		// /motioncamera toggle | status | reload | speed <v> | fpspeed <v> | duration <ms>
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("motioncamera")
						.then(ClientCommandManager.literal("toggle").executes(ctx -> {
							MotionCameraConfig c = MotionCameraConfig.get();
							c.enabled = !c.enabled;
							MotionCameraConfig.save();
							ctx.getSource().sendFeedback(Component.literal("Motion Camera: " + (c.enabled ? "ON" : "OFF")));
							return Command.SINGLE_SUCCESS;
						}))
						.then(ClientCommandManager.literal("status").executes(ctx -> {
							MotionCameraConfig c = MotionCameraConfig.get();
							ctx.getSource().sendFeedback(Component.literal("Motion Camera: " + (c.enabled ? "ON" : "OFF")
									+ " | speed=" + c.speed + " | fpspeed=" + c.firstPersonSpeed
									+ " | duration=" + c.exceptionDuration + "ms"));
							return Command.SINGLE_SUCCESS;
						}))
						.then(ClientCommandManager.literal("reload").executes(ctx -> {
							MotionCameraConfig.load();
							ctx.getSource().sendFeedback(Component.literal("Motion Camera: đã tải lại config"));
							return Command.SINGLE_SUCCESS;
						}))
						.then(doubleCommand("speed", v -> MotionCameraConfig.get().speed = v))
						.then(doubleCommand("fpspeed", v -> MotionCameraConfig.get().firstPersonSpeed = v))
						.then(intCommand("duration", v -> MotionCameraConfig.get().exceptionDuration = v))
				));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> doubleCommand(String name, DoubleConsumer setter) {
		return ClientCommandManager.literal(name).then(
				ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.02, 1.0)).executes(ctx -> {
					double v = DoubleArgumentType.getDouble(ctx, "value");
					setter.accept(v);
					MotionCameraConfig.save();
					ctx.getSource().sendFeedback(Component.literal(name + " = " + v));
					return Command.SINGLE_SUCCESS;
				}));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> intCommand(String name, IntConsumer setter) {
		return ClientCommandManager.literal(name).then(
				ClientCommandManager.argument("value", IntegerArgumentType.integer(100, 10000)).executes(ctx -> {
					int v = IntegerArgumentType.getInteger(ctx, "value");
					setter.accept(v);
					MotionCameraConfig.save();
					ctx.getSource().sendFeedback(Component.literal(name + " = " + v));
					return Command.SINGLE_SUCCESS;
				}));
	}
}
