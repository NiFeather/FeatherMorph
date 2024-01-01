package xiamomc.morph.backends.server.renderer.utilties;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;

public class ProtocolRegistryUtils
{
    public static WrappedDataWatcher.Serializer getSerializer(SingleValue<?> sv)
    {
        var s = sv.getSerializer();
        if (s != null)
            return s;

        return getSerializer(sv.defaultValue());
    }

    public static WrappedDataWatcher.Serializer getSerializer(Object instance)
    {
        var clazz = instance.getClass();

        if (instance instanceof Optional<?> optional)
        {
            if (optional.isEmpty())
                throw new IllegalArgumentException("An empty Optional is given");

            clazz = optional.get().getClass();
            instance = optional.get();
        }

        if (clazz == BlockPos.class)
            return WrappedDataWatcher.Registry.getBlockPositionSerializer(true);

        if (instance instanceof Component)
            return WrappedDataWatcher.Registry.getChatComponentSerializer(true);

        return WrappedDataWatcher.Registry.get(clazz);
    }
}
