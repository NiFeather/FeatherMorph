package xiamomc.morph.updates;

import java.util.Arrays;

public enum Platforms
{
    SPIGOT("Spigot"),
    PAPER("Paper"),
    PURPUR("Purpur"),
    FOLIA("Folia");

    private final String implName;

    public String getImplName()
    {
        return implName;
    }

    Platforms(String implName)
    {
        this.implName = implName;
    }

    public static Platforms fromName(String name)
    {
        return Arrays.stream(Platforms.values())
                .filter(v -> v.implName.equalsIgnoreCase(name))
                .findFirst().orElse(PAPER);
    }
}
