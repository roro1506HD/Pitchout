package ovh.roro.pitchout.util.firework;

import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;

/**
 * @author roro1506_HD
 */
public class FireworkUtil {

    private static final Random RANDOM = new Random();

    public static FireworkEffect getRandomFirework() {
        FireworkEffect.Builder effectBuilder = FireworkEffect.builder()
                .flicker(RANDOM.nextBoolean())
                .trail(RANDOM.nextBoolean())
                .with(FireworkEffect.Type.values()[RANDOM.nextInt(FireworkEffect.Type.values().length)]);

        for (int i = 0; i < RANDOM.nextInt(3) + 1; i++)
            effectBuilder.withColor(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)));

        for (int i = 0; i < RANDOM.nextInt(3) + 1; i++)
            effectBuilder.withFade(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)));

        return effectBuilder.build();
    }

    public static FireworkEffect getFireworkEffect(Color color, Type type, boolean flicker, boolean trail) {
        return FireworkEffect.builder()
                .with(type)
                .withColor(color)
                .flicker(flicker)
                .trail(trail)
                .build();
    }

    public static Color getRandomColor() {
        return Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }
}