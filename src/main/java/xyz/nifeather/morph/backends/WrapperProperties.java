package xyz.nifeather.morph.backends;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.OutputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Optional;

public class WrapperProperties
{
    public static final SingleProperty<String> DISGUISE_ID = SingleProperty.builder("wrapper_disguiseIdentifier", "nil")
            .withInputHandle(InputHandles::immediateException)
            .build();

    public static final SingleProperty<String> DISGUISE_NAME = SingleProperty.builder("wrapper_disguise_name", "")
            .withInputHandle(InputHandles::immediateException)
            .build();

    public static final SingleProperty<Boolean> SADDLED = SingleProperty.builder("wrapper_saddled", false)
            .withInputHandle(InputHandles::immediateException)
            .build();
}
