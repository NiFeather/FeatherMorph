package xiamomc.morph.misc.skins;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import xiamomc.morph.misc.MorphGameProfile;

import java.util.UUID;

public class SingleSkin
{
    public String name = "unknown";

    public UUID uuid = Util.NIL_UUID;

    public String texture = "";

    public String signature = "";

    public int expiresAt = 0;

/*
    public static SingleSkin fromProfile(GameProfile profile)
    {
        var wrapped = new MorphGameProfile(profile);
        var instance = new SingleSkin();

        instance.name = profile.getName();
        instance.uuid = profile.getId();
    }

 */
}
