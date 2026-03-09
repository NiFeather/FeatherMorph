package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class TextDisplayWatcher extends DisplayEntityWatcher
{
    public TextDisplayWatcher(Player bindTarget)
    {
        super(bindTarget, EntityType.TEXT_DISPLAY);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();
        register(ValueIndex.TEXT_DISPLAY);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        switch (property.id())
        {
            case PropertyNames.TEXT_DISPLAY_TEXT ->
            {
                var component = (Component) value;
                this.writePersistent(ValueIndex.TEXT_DISPLAY.TEXT, component);
            }
        }

        super.onPropertyWrite(property, value);
    }
}
