package dev.motioncamera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** .minecraft/config/motioncamera.json */
public final class MotionCameraConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("motioncamera.json");

	/** Bật/tắt toàn bộ mod. */
	public boolean enabled = true;
	/** Độ mượt góc nhìn thứ 3: 0.02 (rất trễ) .. 1.0 (không mượt). */
	public double speed = 0.25;
	/** Độ mượt góc nhìn thứ 1. */
	public double firstPersonSpeed = 0.3;
	/** Giữ hiệu ứng mượt bao lâu (ms) sau khi pearl của bạn không còn bay. */
	public int exceptionDuration = 1500;

	private static MotionCameraConfig instance = new MotionCameraConfig();

	public static MotionCameraConfig get() {
		return instance;
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				try {
					MotionCameraConfig c = GSON.fromJson(Files.readString(FILE), MotionCameraConfig.class);
					if (c != null) instance = c;
				} catch (Exception parseError) {
					// File hỏng: sao lưu rồi dùng mặc định thay vì crash.
					Files.move(FILE, FILE.resolveSibling("motioncamera.json.bad"), StandardCopyOption.REPLACE_EXISTING);
					instance = new MotionCameraConfig();
					MotionCameraMod.LOGGER.warn("Config lỗi, đã sao lưu thành motioncamera.json.bad và dùng mặc định");
				}
			}
			sanitize();
			save();
		} catch (Exception e) {
			MotionCameraMod.LOGGER.warn("Không xử lý được config, dùng mặc định", e);
			instance = new MotionCameraConfig();
		}
	}

	public static void save() {
		try {
			sanitize();
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (Exception e) {
			MotionCameraMod.LOGGER.warn("Không ghi được config", e);
		}
	}

	private static void sanitize() {
		MotionCameraConfig c = instance;
		c.speed = clean(c.speed, 0.25);
		c.firstPersonSpeed = clean(c.firstPersonSpeed, 0.3);
		c.exceptionDuration = Math.clamp(c.exceptionDuration, 100, 10000);
	}

	private static double clean(double v, double fallback) {
		if (Double.isNaN(v) || Double.isInfinite(v)) return fallback;
		return Math.clamp(v, 0.02, 1.0);
	}
}
