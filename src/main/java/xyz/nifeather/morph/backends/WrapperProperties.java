package xyz.nifeather.morph.backends;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.OutputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Optional;

public class WrapperProperties
{
    public static final SingleProperty<String> DISGUISE_ID = SingleProperty.of("wrapper_disguiseIdentifier", "nil", InputHandles::immediateException, OutputHandles::immediateException);
    public static final SingleProperty<Optional<GameProfile>> PROFILE = SingleProperty.of("wrapper_profile", Optional.empty(), InputHandles::immediateException, OutputHandles::immediateException);
    public static final SingleProperty<String> DISGUISE_NAME = SingleProperty.of("wrapper_disguise_name", "", InputHandles::immediateException, OutputHandles::immediateException);
    public static final SingleProperty<Boolean> SADDLED = SingleProperty.of("wrapper_saddled", false, InputHandles::immediateException, OutputHandles::immediateException);
}
