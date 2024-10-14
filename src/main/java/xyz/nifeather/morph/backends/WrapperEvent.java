package xyz.nifeather.morph.backends;

import com.mojang.authlib.GameProfile;

public class WrapperEvent<T>
{
    public static final WrapperEvent<GameProfile> SKIN_SET = new WrapperEvent<>();
}
