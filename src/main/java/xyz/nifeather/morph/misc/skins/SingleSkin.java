package xyz.nifeather.morph.misc.skins;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.utilities.NbtUtils;

public class SingleSkin
{
    @Expose
    @SerializedName("name")
    public String name = "unknown";

    @Expose
    @SerializedName("profile")
    public String snbt = "{}";

    @Expose
    @SerializedName("expires_at")
    public long expiresAt;

    @Nullable
    public GameProfile cachedProfile;

    public static SingleSkin fromProfile(GameProfile profile)
    {
        var instance = new SingleSkin();

        instance.name = profile.name();
        instance.snbt = NbtUtils.getCompoundString(NbtUtils.toCompoundTag(profile));
        instance.cachedProfile = profile;
        instance.expiresAt = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000;
        //                   MS                           D    H    M    S    MS

        return instance;
    }

    @Nullable
    public GameProfile generateGameProfile()
    {
        if (cachedProfile != null) return cachedProfile;

        if (this.snbt == null || this.snbt.equalsIgnoreCase("{}"))
            return null;

        cachedProfile = NbtUtils.readGameProfile(this.snbt);

        return cachedProfile;
    }
}
