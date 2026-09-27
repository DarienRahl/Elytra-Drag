package net.bl4st.elytradrag.client.mixin;

import net.bl4st.elytradrag.ElytraDrag;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class ElytraDragAnimation {
    /**
     * Wing flaps per second
     */
    @Unique
    private static final float FLAPPING_SPEED = 3.0F;

    /**
     * Scuffed way to handle some kind of animation,
     * it just works™
     * The elytra model uses these angles for the left wing
     * and mirrors them onto the right wing
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("RETURN"))
    private void elytradrag$dragAnimation(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (!ElytraDrag.IsDragging(entity))
            return;

        float progressCycle = state.ageInTicks * FLAPPING_SPEED / 20.0F;
        float progress = ((float) Math.sin(progressCycle * Math.PI * 2.0D) + 1.0F) / 2.0F;
        float playerSpeed = (float) entity.getDeltaMovement().length() * 20.0F;
        float animSpeedCoef = GetSpeedAnimationCoef(playerSpeed);

        state.elytraRotX = progress * (1.0F - animSpeedCoef);
        state.elytraRotY = 1.5F * animSpeedCoef;
        state.elytraRotZ = -1.0F;
    }

    /**
     * 0 at 2 blocks/s or less, 1 at 45 blocks/s or more
     */
    @Unique
    private static float GetSpeedAnimationCoef(float value) {
        value = Math.max(2.0F, Math.min(45.0F, value));
        return (float) Math.pow((value - 2.0F) / 43.0F, 0.25D);
    }
}
