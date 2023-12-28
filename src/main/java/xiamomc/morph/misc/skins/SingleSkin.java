package xiamomc.morph.misc.skins;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.NbtUtils;
import xiamomc.morph.utilities.NmsUtils;

import java.util.UUID;

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
        //                                                D    H    M    S    MS

        return instance;
    }

    @Nullable
    public GameProfile toGameProfile()
    {
        if (this.snbt == null || this.snbt.equalsIgnoreCase("{}"))
            return null;

        var compound = NbtUtils.toCompoundTag(this.snbt);
        return compound == null
            ? null
            : net.minecraft.nbt.NbtUtils.readGameProfile(compound);
    }
}
