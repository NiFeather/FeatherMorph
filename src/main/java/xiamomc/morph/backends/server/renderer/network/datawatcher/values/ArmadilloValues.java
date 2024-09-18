package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AnimalValues;

public class ArmadilloValues extends AnimalValues
{
    public final SingleValue<Armadillo.ArmadilloState> STATE = createSingle("armadillo_state", Armadillo.ArmadilloState.IDLE);

    public ArmadilloValues()
    {
        STATE.setSerializer(WrappedDataWatcher.Registry.get(Armadillo.ArmadilloState.class));

        registerSingle(STATE);
    }
}
