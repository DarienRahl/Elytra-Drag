package net.bl4st.elytradrag.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.CameraType;

/**
 * The README's pictures: Steve flies with an elytra over a flat world in single player, first gliding normally,
 * then holding Sneak so the mod brakes and spreads the wings. The game's pictures are taken from behind and from the
 * front with the GUI hidden; .github/workflows/screenshots.yml turns them into docs/images/screenshots.
 */
@SuppressWarnings("UnstableApiUsage")
public class ElytraDragScreenshots implements FabricClientGameTest {
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	/** Chunks render slowly on CI's software renderer: up to three minutes, then the pictures are taken anyway. */
	private static final int CHUNK_TICKS = 20 * 60 * 3;
	/** The flight starts 40 blocks above the flat world's grass, facing south and a little down. */
	private static final int START_Y = -20;

	private static final String[] START = {
		"time set noon",
		"weather clear",
		"item replace entity @a armor.chest with minecraft:elytra",
		// an invisible platform to wait on while the chunks render
		"setblock 0 " + (START_Y - 1) + " 0 minecraft:barrier",
		"tp @a 0.5 " + START_Y + " 0.5 0 20",
	};

	/** Something to fly over: a pond and trees ahead (south) and behind (north). */
	private static final String[] SCENERY = {
		"fill -10 -61 30 4 -61 42 minecraft:water",
		"place feature minecraft:fancy_oak 9 -60 26",
		"place feature minecraft:oak -7 -60 20",
		"place feature minecraft:birch 6 -60 48",
		"place feature minecraft:oak -15 -60 46",
		"place feature minecraft:fancy_oak 17 -60 62",
		"place feature minecraft:birch -4 -60 68",
		"place feature minecraft:oak 12 -60 82",
		"place feature minecraft:fancy_oak -19 -60 88",
		"place feature minecraft:birch 24 -60 100",
		"place feature minecraft:oak -2 -60 104",
		"place feature minecraft:fancy_oak 6 -60 -24",
		"place feature minecraft:oak -9 -60 -34",
		"place feature minecraft:birch 14 -60 -48",
		"place feature minecraft:fancy_oak -16 -60 -60",
		"place feature minecraft:oak 3 -60 -72",
	};

	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(client -> {
			client.options.fov().set(70);
			// flying fast widens the view; the pictures should not depend on the speed
			client.options.fovEffectScale().set(0.0);
			client.options.renderDistance().set(6);
		});

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			TestServerContext server = singleplayer.getServer();
			for (String command : START) {
				server.runCommand(command);
			}
			waitForChunks(singleplayer, CHUNK_TICKS);
			for (String command : SCENERY) {
				server.runCommand(command);
			}
			waitForChunks(singleplayer, 20 * 60);

			// the offline account of the test's username (build.gradle) has Steve's default skin
			String skin = context.computeOnClient(client -> String.valueOf(client.player.getSkin()));
			if (!skin.contains("wide/steve")) {
				throw new AssertionError("The pictures should show Steve, but the player's skin is " + skin);
			}

			// F1: no hotbar or crosshair in the pictures
			context.getInput().pressKey(options -> options.keyToggleGui);
			camera(context, CameraType.THIRD_PERSON_BACK);

			// step off the platform and open the elytra with a jump in the air
			server.runCommand("setblock 0 " + (START_Y - 1) + " 0 minecraft:air");
			context.waitTicks(6);
			for (int attempt = 0; attempt < 5 && !isGliding(context); attempt++) {
				context.getInput().pressKey(options -> options.keyJump);
				context.waitTicks(4);
			}
			if (!isGliding(context)) {
				throw new AssertionError("The elytra did not open");
			}

			// gliding like in vanilla
			context.waitTicks(15);
			screenshot(context, "glide-back");
			camera(context, CameraType.THIRD_PERSON_FRONT);
			screenshot(context, "glide-front");

			// holding Sneak: the mod brakes and spreads the wings
			context.getInput().holdKey(options -> options.keyShift);
			camera(context, CameraType.THIRD_PERSON_BACK);
			context.waitTicks(30);
			screenshot(context, "drag-back");
			camera(context, CameraType.THIRD_PERSON_FRONT);
			screenshot(context, "drag-front");
			context.getInput().releaseKey(options -> options.keyShift);
		}
	}

	private static void waitForChunks(TestSingleplayerContext singleplayer, int timeout) {
		try {
			int ticks = singleplayer.getConnection().waitForChunksRender(timeout);
			System.out.println("elytra drag screenshots: chunks rendered after " + ticks + " ticks");
		} catch (AssertionError e) {
			// CI's software renderer can be very slow: take the pictures with the chunks that are there
			System.out.println("elytra drag screenshots: not every chunk rendered within " + timeout + " ticks, going on");
		}
	}

	private static boolean isGliding(ClientGameTestContext context) {
		return context.computeOnClient(client -> client.player.isFallFlying());
	}

	private static void camera(ClientGameTestContext context, CameraType type) {
		context.runOnClient(client -> client.options.setCameraType(type));
		context.waitTick();
	}

	private static void screenshot(ClientGameTestContext context, String name) {
		context.takeScreenshot(TestScreenshotOptions.of(name).withSize(WIDTH, HEIGHT).disableCounterPrefix());
	}
}
