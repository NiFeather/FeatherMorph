package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class MannequinValues extends AvatarValues
{
    public final SingleValue<Optional<Component>> DESCRIPTION = createSingle("mannequin_description", Optional.of(Component.empty()), EntityDataTypes.OPTIONAL_ADV_COMPONENT);
    public final SingleValue<Boolean> IMMOVABLE = createSingle("mannequin_immovable", false, EntityDataTypes.BOOLEAN);

    // todo: Wait for PacketEvents
    //public final SingleValue<UserProfile> SKIN_PROFILE = createSingle("mannequin_skin_profile", new UserProfile(Uuids.NIL_UUID, "noset"), )

    public MannequinValues()
    {
        super();

        registerSingle(/* SKIN_PROFILE, */IMMOVABLE, DESCRIPTION);
    }
}
