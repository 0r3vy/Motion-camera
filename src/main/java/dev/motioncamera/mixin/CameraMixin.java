package dev.motioncamera.mixin;

import dev.motioncamera.MotionCameraState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	protected abstract void setPosition(Vec3 pos);

	/**
	 * Chạy sau khi vanilla đã đặt xong camera (kể cả lùi ra third-person).
	 * Chỉ cộng thêm độ lệch làm mượt; khi không active thì không làm gì cả.
	 */
	@Inject(method = "setup", at = @At("TAIL"))
	private void motioncamera$applySmoothing(Level level, Entity entity, boolean detached, boolean mirrored,
											 float partialTick, CallbackInfo ci) {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (!MotionCameraState.isActive(mc, entity)) return;

			Vec3 cur = ((Camera) (Object) this).position();
			double[] d = MotionCameraState.offset(partialTick);
			if (Double.isNaN(d[0]) || Double.isNaN(d[1]) || Double.isNaN(d[2])) return;

			this.setPosition(new Vec3(cur.x + d[0], cur.y + d[1], cur.z + d[2]));
		} catch (Throwable t) {
			MotionCameraState.fail("camera", t);
		}
	}
}
