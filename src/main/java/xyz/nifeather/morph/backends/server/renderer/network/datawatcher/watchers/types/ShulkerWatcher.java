package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ShulkerPropertyCollection;

public class ShulkerWatcher extends LivingEntityWatcher
{
    public ShulkerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SHULKER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SHULKER);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        switch (property.id())
        {
            case PropertyNames.SHULKER_COLOR ->
            {
                var val = (DyeColor) value;
                this.writePersistent(ValueIndex.SHULKER.COLOR_ID, val.getWoolData());
            }

            case PropertyNames.SHULKER_SHELL_HEIGHT ->
            {
                var val = (Byte) value;
                this.writePersistent(ValueIndex.SHULKER.PEEK_ID, val);
            }
        }

        super.onPropertyWrite(property, value);
    }
}
