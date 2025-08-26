package xyz.nifeather.morph.platform;

import xyz.nifeather.morph.platform.impl.paper.PaperPlatform;
import xyz.nifeather.morph.platform.impl.paper.PaperPlatformProvider;

public class CurrentPlatform
{
    private static final PaperPlatformProvider platformProvider;

    static
    {
        platformProvider = new PaperPlatformProvider();
    }

    public static PaperPlatform instance()
    {
        return platformProvider.platform();
    }
}
