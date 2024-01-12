package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

import java.util.Random;

public class GoatWatcher extends LivingEntityWatcher
{
    public GoatWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.GOAT);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.GOAT);
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var random = new Random();

        if (random.nextInt(0, 100) <= 15)
            write(ValueIndex.GOAT.HAS_LEFT_HORN, false);

        if (random.nextInt(0, 100) <= 15)
            write(ValueIndex.GOAT.HAS_RIGHT_HORN, false);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("HasLeftHorn"))
            write(ValueIndex.GOAT.HAS_LEFT_HORN, nbt.getBoolean("HasLeftHorn"));

        if (nbt.contains("HasRightHorn"))
            write(ValueIndex.GOAT.HAS_RIGHT_HORN, nbt.getBoolean("HasRightHorn"));

        if (nbt.contains("IsScreamingGoat"))
            write(ValueIndex.GOAT.IS_SCREAMING, nbt.getBoolean("IsScreamingGoat"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putBoolean("HasLeftHorn", get(ValueIndex.GOAT.HAS_LEFT_HORN));
        nbt.putBoolean("HasRightHorn", get(ValueIndex.GOAT.HAS_RIGHT_HORN));
        nbt.putBoolean("IsScreamingGoat", get(ValueIndex.GOAT.IS_SCREAMING));
    }
}
