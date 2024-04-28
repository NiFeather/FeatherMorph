package xiamomc.morph.misc.skins;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.utilities.NbtUtils;

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

    public static SingleSkin fromProfile(GameProfile profile)
    {
        var instance = new SingleSkin();

        instance.name = profile.getName();
        instance.snbt = NbtUtils.getCompoundString(NbtUtils.toCompoundTag(profile));
        instance.expiresAt = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000;
        //                   MS                           D    H    M    S    MS

        return instance;
    }

    @Nullable
    public GameProfile generateGameProfile()
    {
        if (this.snbt == null || this.snbt.equalsIgnoreCase("{}"))
            return null;

        var compound = NbtUtils.toCompoundTag(this.snbt);
        return compound == null
            ? null
            : NbtUtils.readGameProfile(compound);
    }
}
