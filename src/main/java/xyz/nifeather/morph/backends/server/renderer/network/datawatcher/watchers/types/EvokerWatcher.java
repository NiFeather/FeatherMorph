package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.EvokerValues;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class EvokerWatcher extends LivingEntityWatcher
{
    public EvokerWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.EVOKER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.EVOKER);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.IS_AGGRESSIVE))
        {
            var aggressive = (boolean) newVal;
            var spellStatus = aggressive ? EvokerValues.SpellStatus.ATTACK : EvokerValues.SpellStatus.NONE;
            writePersistent(ValueIndex.EVOKER.SPELL_STATUS, spellStatus.val);
        }
    }
}
