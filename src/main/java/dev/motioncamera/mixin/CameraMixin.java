package dev.motioncamera.mixin;

import dev.motioncamera.MotionCameraState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

	/**
	 * Thay vị trí "mắt" mà vanilla đặt cho camera bằng vị trí đã làm mượt.
	 * Vanilla vẫn tự lùi camera ra cho third-person từ vị trí này.
	 */
	@ModifyArgs(
			method = "setup",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
	)
	private void motioncamera$smoothPosition(Args args) {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (!MotionCameraState.isActive(mc)) return;

			float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
			double[] p = MotionCameraState.position(partial);
			if (Double.isNaN(p[0]) || Double.isNaN(p[1]) || Double.isNaN(p[2])) return;

			args.set(0, p[0]);
			args.set(1, p[1]);
			args.set(2, p[2]);
		} catch (Throwable t) {
			MotionCameraState.fail("camera", t);
		}
	}
}
