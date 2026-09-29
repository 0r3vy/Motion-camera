package dev.motioncamera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Chỉ làm mượt camera trong "cửa sổ pearl": từ lúc bạn ném ender pearl,
 * suốt lúc nó bay, và thêm exceptionDuration ms sau đó.
 * Ngoài cửa sổ, camera là vanilla 100%.
 */
public final class MotionCameraState {
	private MotionCameraState() {}

	private static double x, y, z, px, py, pz;
	private static long lastPearlSeen = Long.MIN_VALUE / 2;
	private static boolean smoothing;

	public static void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		if (player == null || level == null) {
			smoothing = false;
			return;
		}

		long now = System.currentTimeMillis();
		for (Entity e : level.entitiesForRendering()) {
			if (e.getType() == EntityType.ENDER_PEARL && e instanceof Projectile pr && pr.getOwner() == player) {
				lastPearlSeen = now;
			}
		}
		smoothing = now - lastPearlSeen <= MotionCameraConfig.get().exceptionDuration;

		double ex = player.getX();
		double ey = player.getY() + player.getEyeHeight();
		double ez = player.getZ();

		px = x; py = y; pz = z;
		if (!smoothing) {
			// Bám sát vị trí thật để khi cửa sổ mở, camera bắt đầu đúng chỗ.
			x = ex; y = ey; z = ez;
		} else {
			boolean first = mc.options.getCameraType().isFirstPerson();
			double s = first ? MotionCameraConfig.get().firstPersonSpeed : MotionCameraConfig.get().speed;
			x += (ex - x) * s;
			y += (ey - y) * s;
			z += (ez - z) * s;
		}
	}

	public static boolean isActive(Minecraft mc) {
		return smoothing && mc.player != null && mc.getCameraEntity() == mc.player;
	}

	public static double[] position(float partial) {
		return new double[] {
				px + (x - px) * partial,
				py + (y - py) * partial,
				pz + (z - pz) * partial
		};
	}
}
