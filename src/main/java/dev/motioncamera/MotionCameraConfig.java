package dev.motioncamera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** config/motioncamera.json */
public final class MotionCameraConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("motioncamera.json");

	/** Độ mượt góc nhìn thứ 3: 0.02 (rất trễ) .. 1.0 (không mượt). */
	public double speed = 0.25;
	/** Độ mượt góc nhìn thứ 1. */
	public double firstPersonSpeed = 0.3;
	/** Cửa sổ làm mượt (ms), tính từ lúc pearl của bạn không còn bay nữa (để camera kịp trượt tới chỗ dịch chuyển). */
	public int exceptionDuration = 1500;

	private static MotionCameraConfig instance = new MotionCameraConfig();

	public static MotionCameraConfig get() {
		return instance;
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				MotionCameraConfig c = GSON.fromJson(Files.readString(FILE), MotionCameraConfig.class);
				if (c != null) instance = c;
			}
			instance.speed = Math.clamp(instance.speed, 0.02, 1.0);
			instance.firstPersonSpeed = Math.clamp(instance.firstPersonSpeed, 0.02, 1.0);
			instance.exceptionDuration = Math.clamp(instance.exceptionDuration, 100, 10000);
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (Exception e) {
			MotionCameraMod.LOGGER.warn("Motion Camera config error, using defaults", e);
		}
	}
}
