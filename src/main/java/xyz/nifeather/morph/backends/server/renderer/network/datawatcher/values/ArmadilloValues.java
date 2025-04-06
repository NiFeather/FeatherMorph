package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class ArmadilloValues extends AnimalValues
{
    public final SingleValue<ArmadilloState> STATE = createSingle("armadillo_state", ArmadilloState.IDLE, EntityDataTypes.ARMADILLO_STATE);

    public ArmadilloValues()
    {
        registerSingle(STATE);
    }
}
