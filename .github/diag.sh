#!/usr/bin/env bash
# Temporary: dumps Minecraft 26.3 signatures used by the mod.
set +e
JARS=$(find ~/.gradle/caches .gradle -name '*.jar' 2>/dev/null | grep -iE 'minecraft-(merged|common|clientonly|client|server)[^/]*\.jar$' | grep -v sources | sort -u)
echo "### JARS"; echo "$JARS"
CP=$(echo "$JARS" | tr '\n' ':')
echo "### CLASSES"
for j in $JARS; do unzip -Z1 "$j"; done | grep -iE '(Elytra|Wings|FireworkRocketEntity|HumanoidRenderState|AvatarRenderer|AvatarRenderState|ElytraAnimationState)[^/]*\.class$' | sort -u
EM=$(for j in $JARS; do unzip -Z1 "$j"; done | grep -E '/ElytraModel\.class$' | head -1 | sed 's/\.class$//; s#/#.#g')
echo "### ElytraModel=$EM"
p() { echo "### javap $*"; javap -p -cp "$CP" "$@" 2>&1; }
p net.minecraft.world.entity.projectile.FireworkRocketEntity
p net.minecraft.client.renderer.entity.state.HumanoidRenderState
p net.minecraft.client.renderer.entity.state.EntityRenderState | grep -E 'ageInTicks|class'
p net.minecraft.client.renderer.entity.player.AvatarRenderer | grep -E 'extractRenderState|class'
p net.minecraft.world.entity.Avatar | grep -E 'class'
p net.minecraft.world.entity.Entity | grep -E 'hurtMarked|fallDistance|isShiftKeyDown|DeltaMovement|markHurt|class '
p net.minecraft.world.entity.LivingEntity | grep -E 'isFallFlying|markHurt|class '
p net.minecraft.world.phys.Vec3 | grep -E ' scale\(| length\(| y;'
p net.minecraft.server.players.PlayerList | grep -E 'getPlayers'
p net.minecraft.server.level.ServerPlayer | grep -E 'class |hurtMarked|KnownMovement'
echo "### ElytraModel bytecode"; javap -c -p -cp "$CP" "$EM" 2>&1 | head -120
echo "### FireworkRocketEntity.tick bytecode"; javap -c -p -cp "$CP" net.minecraft.world.entity.projectile.FireworkRocketEntity 2>&1 | sed -n '/public void tick()/,/^$/p' | grep -E 'getfield|putfield|invoke|tick' | head -80
echo "### AvatarRenderer.extractRenderState bytecode"; javap -c -p -cp "$CP" net.minecraft.client.renderer.entity.player.AvatarRenderer 2>&1 | sed -n '/public void extractRenderState(net.minecraft.world.entity.Avatar/,/^$/p' | grep -E 'invoke|putfield' | head -60
echo "### HumanoidMobRenderer elytra"; javap -c -p -cp "$CP" net.minecraft.client.renderer.entity.HumanoidMobRenderer 2>&1 | grep -nE 'elytra|Elytra' | head -20
echo "### WingsLayer usage of elytraRot"; for c in $(for j in $JARS; do unzip -Z1 "$j"; done | grep -E '/WingsLayer\.class$' | sed 's/\.class$//; s#/#.#g'); do javap -c -p -cp "$CP" "$c" 2>&1 | grep -nE 'setupAnim|elytra' | head; done
exit 0
