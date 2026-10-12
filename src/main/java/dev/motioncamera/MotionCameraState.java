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
 *   sau đó nhả dần để không bị giật. Ngoài cửa sổ, camera là vanilla 100%.
 *
 * maxOffset: camera không trễ quá N block so với vị trí thật (giữ tâm ngắm không lệch xa).
 * Kết thúc sớm khi pearl đã hạ cánh và camera đã bắt kịp, để không lệch tâm vô ích.
 * Thời gian tính bằng tick (không phụ thuộc lag spike / pause).
 */
public final class MotionCameraState {
	private MotionCameraState() {}

	private static final long NEVER = Long.MIN_VALUE / 4;
	private static final long FADE_TICKS = 8;
	private static final double SNAP_DIST_SQ = 256.0 * 256.0;
	private static final int MAX_ERRORS = 5;
	/** Kết thúc sớm: sau khi pearl hạ cánh ít nhất ngần này tick (chờ gói dịch chuyển) và camera đã bắt kịp. */
	private static final long EARLY_END_TICKS = 10;
	private static final double EARLY_END_LAG_SQ = 0.02 * 0.02;

	private static double x, y, z, px, py, pz;
	private static final double[] OUT = new double[3]; // tái sử dụng, tránh cấp phát mỗi frame

	private static long tickCounter;
	private static long lastPearlTick = NEVER;
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
			lastPearlTick = NEVER;
			return;
		}
		if (player != lastPlayer) { // world mới / respawn / đổi dimension
			lastPlayer = player;
			lastPearlTick = NEVER;
			needSnap = true;
		}

		tickCounter++;
		if (cfg.enabled) {
			for (Entity e : level.entitiesForRendering()) {
				if (e.getType() == EntityType.ENDER_PEARL && !e.isRemoved() && e instanceof Projectile) {
					if (((Projectile) e).getOwner() == player) {
						lastPearlTick = tickCounter;
						break;
					}
				}
			}
		}

		double ex = player.getX();
		double ey = player.getY() + player.getEyeHeight();
		double ez = player.getZ();

		double dx = ex - x, dy = ey - y, dz = ez - z;
		double lag2 = dx * dx + dy * dy + dz * dz;
		boolean farAway = lag2 > SNAP_DIST_SQ;

		// Kết thúc sớm: pearl đã hạ cánh, đã đợi gói dịch chuyển và camera đã bắt kịp vị trí thật
		// => trả về vanilla ngay, không giữ camera mượt (dễ lệch tâm) cho tới hết thời gian.
		if (lastPearlTick != NEVER && tickCounter - lastPearlTick >= EARLY_END_TICKS && lag2 < EARLY_END_LAG_SQ) {
			lastPearlTick = NEVER;
		}

		long durationTicks = (cfg.exceptionDuration + 49L) / 50L;
		long over = tickCounter - lastPearlTick - durationTicks; // <= 0: đang trong cửa sổ
		double fade = over <= 0 ? 0.0 : Math.min(1.0, (double) over / FADE_TICKS);
		smoothing = cfg.enabled && fade < 1.0;

		if (needSnap || farAway) {
			x = px = ex;
			y = py = ey;
			z = pz = ez;
			needSnap = false;
			return;
		}

		px = x; py = y; pz = z;

		if (!smoothing) {
			x = ex; y = ey; z = ez;
			return;
		}

		double s0 = mc.options.getCameraType().isFirstPerson() ? cfg.firstPersonSpeed : cfg.speed;
		double s = s0 + (1.0 - s0) * fade; // nhả dần về 1.0 khi hết cửa sổ
		x += (ex - x) * s;
		y += (ey - y) * s;
		z += (ez - z) * s;

		// Giới hạn độ trễ tối đa: tâm ngắm không lệch quá xa vị trí thật.
		double max = cfg.maxOffset;
		if (max > 0.0) {
			double ox = ex - x, oy = ey - y, oz = ez - z;
			double d2 = ox * ox + oy * oy + oz * oz;
			if (d2 > max * max) {
				double k = max / Math.sqrt(d2);
				x = ex - ox * k;
				y = ey - oy * k;
				z = ez - oz * k;
			}
		}
	}

	public static boolean isActive(Minecraft mc) {
		Entity cam = mc.getCameraEntity();
		return smoothing && !broken && cam != null && cam == mc.player && cam == lastPlayer;
	}

	/** Vị trí mắt đã làm mượt, nội suy theo partial tick. (Trả về mảng dùng chung, chỉ đọc ngay.) */
	public static double[] position(float partial) {
		OUT[0] = px + (x - px) * partial;
		OUT[1] = py + (y - py) * partial;
		OUT[2] = pz + (z - pz) * partial;
		return OUT;
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
