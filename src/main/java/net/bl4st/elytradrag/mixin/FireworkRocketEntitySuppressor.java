package net.bl4st.elytradrag.mixin;

import net.bl4st.elytradrag.ElytraDrag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntitySuppressor {

    @Shadow
    private int lifetime;

    @Shadow
    private LivingEntity attachedToEntity;

    /**
     * Ends the rocket propulsion as soon as the
     * player boosted by it starts slowing down
     */
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void elytradrag$suppressPropulsion(CallbackInfo ci)
    {
        if (attachedToEntity != null && ElytraDrag.IsDragging(attachedToEntity))
            lifetime = 0;
    }
}
