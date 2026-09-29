package dev.motioncamera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotionCameraMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("motioncamera");

	@Override
	public void onInitializeClient() {
		MotionCameraConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(MotionCameraState::tick);
	}
}
