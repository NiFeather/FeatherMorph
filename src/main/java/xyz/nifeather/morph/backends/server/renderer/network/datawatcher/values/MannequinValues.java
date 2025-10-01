package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.kyori.adventure.text.Component;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.List;
import java.util.Optional;

public class MannequinValues extends AvatarValues
{
    public final SingleValue<ItemProfile> SKIN_PROFILE = createSingle("mannequin_skin_profile", new ItemProfile("noset", Uuids.NIL_UUID, List.of()), EntityDataTypes.RESOLVABLE_PROFILE);
    public final SingleValue<Boolean> IMMOVABLE = createSingle("mannequin_immovable", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Optional<Component>> DESCRIPTION = createSingle("mannequin_description", Optional.of(Component.empty()), EntityDataTypes.OPTIONAL_ADV_COMPONENT);

    public MannequinValues()
    {
        super();

        registerSingle(DESCRIPTION, IMMOVABLE, SKIN_PROFILE);
    }
}
