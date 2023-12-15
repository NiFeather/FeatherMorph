package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

import java.util.Arrays;
import java.util.Random;

public class RabbitWatcher extends LivingEntityWatcher
{
    public RabbitWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.RABBIT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.RABBIT);
    }

    public Rabbit.Type getType()
    {
        return Arrays.stream(Rabbit.Type.values()).toList().get(get(ValueIndex.RABBIT.RABBIT_TYPE));
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var availableTypes = Arrays.stream(Rabbit.Type.values()).toList();
        var random = new Random();

        var targetValue = availableTypes.get(random.nextInt(availableTypes.size()));
        write(ValueIndex.RABBIT.RABBIT_TYPE, targetValue.ordinal());
    }
}
