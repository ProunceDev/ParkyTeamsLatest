package org.ProunceDev.parkyTeamsLatest;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Random;

public class ColorUtil {
    private static final List<NamedTextColor> COLORS = List.of(
            NamedTextColor.RED, NamedTextColor.BLUE, NamedTextColor.GREEN,
            NamedTextColor.YELLOW, NamedTextColor.GOLD, NamedTextColor.AQUA,
            NamedTextColor.LIGHT_PURPLE, NamedTextColor.DARK_GRAY
    );

    public static NamedTextColor getRandomColor() {
        return COLORS.get(new Random().nextInt(COLORS.size()));
    }
}
