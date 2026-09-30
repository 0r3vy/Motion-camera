package dev.motioncamera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Camera chỉ được làm mượt trong "cửa sổ pearl":
 *   từ lúc bạn ném ender pearl -> suốt lúc nó bay -> thêm exceptionDuration ms,
 *   sau đó nhả dần trong FADE_MS để không bị giật khi kết thúc.
 * Ngoài cửa sổ, camera là vanilla 100%.
 */
public final class MotionCameraState {
	private MotionCameraState() {}

	private static final long NEVER = Long.MIN_VALUE / 4;
	private static final long FADE_MS = 500;
	/** Chỉ snap khi lệch quá xa (đổi dimension/respawn). Pearl dịch chuyển vẫn được lướt mượt. */
	private static final double SNAP_DIST_SQ = 256.0 * 256.0;
	private static final int MAX_ERRORS = 5;

	// camera đã làm mượt (tick hiện tại / trước)
	private static double x, y, z, px, py, pz;

	private static long lastPearlSeen = NEVER;
	private static boolean smoothing;
	private static boolean needSnap = true;
	private static LocalPlayer lastPlayer;
	private static int errors;
	private static boolean broken;

	public static void tick(Minecraft mc) {
		if (broken) return;
		try {
			doTick(mc);
		} catch (Throwable t) {
			fail("tick", t);
		}
	}

	private static void doTick(Minecraft mc) {
		MotionCameraConfig cfg = MotionCameraConfig.get();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;

		if (player == null || level == null) {
			smoothing = false;
			needSnap = true;
			lastPlayer = null;
			lastPearlSeen = NEVER;
			return;
		}
		if (player != lastPlayer) { // vào world mới / respawn / đổi dimension
			lastPlayer = player;
			lastPearlSeen = NEVER;
			needSnap = true;
		}

		long now = System.currentTimeMillis();
		if (cfg.enabled) {
			for (Entity e : level.entitiesForRendering()) {
				if (e.getType() == EntityType.ENDER_PEARL && !e.isRemoved() && e instanceof Projectile) {
					if (((Projectile) e).getOwner() == player) {
						lastPearlSeen = now;
					}
				}
			}
		}

		long over = now - lastPearlSeen - cfg.exceptionDuration; // <= 0: đang trong cửa sổ
		double fade = over <= 0 ? 0.0 : Math.min(1.0, (double) over / FADE_MS);
		smoothing = cfg.enabled && fade < 1.0;

		double ex = player.getX();
		double ey = player.getY() + player.getEyeHeight();
		double ez = player.getZ();

		double dx = ex - x, dy = ey - y, dz = ez - z;
		boolean farAway = dx * dx + dy * dy + dz * dz > SNAP_DIST_SQ;

		if (needSnap || farAway) {
			x = px = ex;
			y = py = ey;
			z = pz = ez;
			needSnap = false;
			return;
		}

		px = x; py = y; pz = z;

		if (!smoothing) {
			// Bám sát vị trí thật để khi cửa sổ mở, camera bắt đầu đúng chỗ.
			x = ex; y = ey; z = ez;
		} else {
			double s0 = mc.options.getCameraType().isFirstPerson() ? cfg.firstPersonSpeed : cfg.speed;
			double s = s0 + (1.0 - s0) * fade; // nhả dần về 1.0 khi hết cửa sổ
			x += (ex - x) * s;
			y += (ey - y) * s;
			z += (ez - z) * s;
		}
	}

	public static boolean isActive(Minecraft mc) {
		Entity cam = mc.getCameraEntity();
		return smoothing && !broken && cam != null && cam == mc.player && cam == lastPlayer;
	}

	/** Vị trí mắt đã làm mượt, nội suy theo partial tick. */
	public static double[] position(float partial) {
		return new double[] {
				px + (x - px) * partial,
				py + (y - py) * partial,
				pz + (z - pz) * partial
		};
	}

	public static void fail(String where, Throwable t) {
		errors++;
		MotionCameraMod.LOGGER.error("Motion Camera lỗi ở {} ({}/{})", where, errors, MAX_ERRORS, t);
		if (errors >= MAX_ERRORS) {
			broken = true;
			smoothing = false;
			MotionCameraMod.LOGGER.error("Motion Camera tự tắt để tránh crash game. Gửi log cho dev.");
		}
	}
}
