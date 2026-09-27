package net.bl4st.elytradrag;

import net.bl4st.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraDrag implements ModInitializer {

	public static final String MOD_ID = "elytradrag";

	@Override
	public void onInitialize() {
		ModConfig.LoadConfig();
		ServerTickEvents.END_SERVER_TICK.register(this::ElytraDragTick);
	}

	/**
	 * True while the entity is flying with elytras and holding the sneak key
	 */
	public static boolean IsDragging(LivingEntity entity)
	{
		return entity.isFallFlying() && entity.isShiftKeyDown();
	}

	private void ElytraDragTick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers())
		{
			if (!IsDragging(player))
				continue;

			Vec3 playerVelocity = player.getDeltaMovement();
			double playerSpeed = playerVelocity.length() * 20.0;
			if (playerSpeed > ModConfig.MINIMUM_SPEED)
			{
				player.setDeltaMovement(playerVelocity.scale(1.0 - 0.05 * ModConfig.ELYTRA_DRAG));
				// Sends the new velocity to the client, player movement is client authoritative
				player.syncVelocity = true;
			}
			LimitFallDistance(player);
		}
	}

	/**
	 * Caps the FallDistance when drag is applied and
	 * makes the vertical speed requirement for
	 * fallDistance reset easier to reach
	 */
	private void LimitFallDistance(ServerPlayer player)
	{
		if (player.getDeltaMovement().y > -1.0 && player.fallDistance > 1.0)
			player.fallDistance = 1.0;
		else if (player.fallDistance > ModConfig.MAXIMUM_FALLDISTANCE)
			player.fallDistance = ModConfig.MAXIMUM_FALLDISTANCE;
	}
}
