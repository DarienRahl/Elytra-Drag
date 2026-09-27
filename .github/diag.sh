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
p net.minecraft.world.entity.Entity | grep -E 'public (boolean|double|float|int) [a-zA-Z]+;|void markHurt|Impulse|needsSync|hurt'
echo "### Entity.markHurt bytecode"; javap -c -p -cp "$CP" net.minecraft.world.entity.Entity 2>&1 | sed -n '/void markHurt()/,/^$/p'
echo "### LivingEntity knockback bytecode"; javap -c -p -cp "$CP" net.minecraft.world.entity.LivingEntity 2>&1 | sed -n '/public void knockback(double, double, double)/,/^$/p' | grep -E 'field|invoke'
echo "### LivingEntity markHurt"; javap -p -cp "$CP" net.minecraft.world.entity.LivingEntity 2>&1 | grep -E 'markHurt|hurt[A-Z]'
echo "### ServerEntity sendChanges"; javap -c -p -cp "$CP" net.minecraft.server.level.ServerEntity 2>&1 | sed -n '/public void sendChanges()/,/^$/p' | grep -E 'Field net/minecraft/world/entity/Entity|MotionPacket|Method net/minecraft/world/entity/Entity' | head -40
p net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket | grep -E 'ClientboundSetEntityMotionPacket\('
p net.minecraft.server.level.ServerPlayer | grep -E ' connection;'
p net.minecraft.server.network.ServerGamePacketListenerImpl | grep -E 'void send\('
echo "### AvatarRenderer.extractRenderState bytecode"; javap -c -p -cp "$CP" net.minecraft.client.renderer.entity.player.AvatarRenderer 2>&1 | sed -n '/public void extractRenderState(AvatarlikeEntity/,/^$/p' | grep -E 'invoke|putfield' | head -60
echo "### FireworkRocketEntity.tick tail"; javap -c -p -cp "$CP" net.minecraft.world.entity.projectile.FireworkRocketEntity 2>&1 | sed -n '/public void tick()/,/^$/p' | tail -25
exit 0
