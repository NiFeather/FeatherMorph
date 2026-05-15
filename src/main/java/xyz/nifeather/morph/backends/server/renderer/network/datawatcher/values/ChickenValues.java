package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenSoundVariant;
import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenSoundVariants;
import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariant;
import com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariants;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class ChickenValues extends AnimalValues
{
    public final SingleValue<ChickenVariant> CHICKEN_VARIANT;
    public final SingleValue<ChickenSoundVariant> CHICKEN_SOUND_VARIANT;

    public ChickenValues()
    {
        CHICKEN_VARIANT = createSingle("chicken_variant", ChickenVariants.TEMPERATE, EntityDataTypes.CHICKEN_VARIANT);
        CHICKEN_SOUND_VARIANT = createSingle("chicken_sound_variant", ChickenSoundVariants.CLASSIC, EntityDataTypes.CHICKEN_SOUND_VARIANT);

        registerSingle(CHICKEN_VARIANT, CHICKEN_SOUND_VARIANT);
    }
}
