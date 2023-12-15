package xiamomc.morph.backends.server.renderer.utilties;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.CatWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.FrogWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PandaWatcher;

public class WatcherUtils
{
    public static CompoundTag buildCompoundFromWatcher(SingleWatcher watcher)
    {
        var tag = new CompoundTag();

        switch (watcher.getEntityType())
        {
            case SLIME, MAGMA_CUBE ->
            {
                var size = watcher.get(ValueIndex.SLIME_MAGMA.SIZE);
                tag.putInt("Size", size);
            }

            case HORSE ->
            {
                tag.putInt("Variant", watcher.get(ValueIndex.HORSE.HORSE_VARIANT));
            }

            case PARROT ->
            {
                var variant = watcher.get(ValueIndex.PARROT.PARROT_VARIANT);
                tag.putInt("Variant", variant);
            }

            case CAT ->
            {
                var variant = ((CatWatcher)watcher).getCatType().getKey().asString();
                tag.putString("variant", variant);
            }

            case TROPICAL_FISH ->
            {
                var variant = watcher.get(ValueIndex.TROPICAL.FISH_VARIANT);

                tag.putInt("Variant", variant);
            }

            case RABBIT ->
            {
                var type = watcher.get(ValueIndex.RABBIT.RABBIT_TYPE);
                tag.putInt("RabbitType", type);
            }

            case FOX ->
            {
                var foxType = watcher.get(ValueIndex.FOX.FOX_VARIANT) == 0 ? "red" : "snow";
                tag.putString("Type", foxType);
            }

            case FROG ->
            {
                var variant = ((FrogWatcher)watcher).getFrogVariant().getKey().asString();
                tag.putString("variant", variant);
            }

            case GOAT ->
            {
                tag.putBoolean("HasLeftHorn", watcher.get(ValueIndex.GOAT.HAS_LEFT_HORN));
                tag.putBoolean("HasRightHorn", watcher.get(ValueIndex.GOAT.HAS_RIGHT_HORN));
                tag.putBoolean("IsScreamingGoat", watcher.get(ValueIndex.GOAT.IS_SCREAMING));
            }

            case PANDA ->
            {
                var pandaWatcher = (PandaWatcher)watcher;
                var mainGene = pandaWatcher.getMainGene();
                var hiddenGene = pandaWatcher.getHiddenGene();

                tag.putString("MainGene", mainGene.toString().toLowerCase());
                tag.putString("HiddenGene", hiddenGene.toString().toLowerCase());
            }

/*
            case VILLAGER ->
            {
                if (!compoundTag.contains("VillagerData"))
                {
                    var villagerData = ((VillagerWatcher) watcher).getVillagerData();
                    var profession = villagerData.getProfession();
                    var type = villagerData.getType();
                    var level = villagerData.getLevel();

                    var compound = new CompoundTag();
                    compound.putInt("level", level);
                    compound.putString("profession", profession.getKey().asString());
                    compound.putString("type", type.getKey().asString());

                    compoundTag.put("VillagerData", compound);
                }
            }
 */
        }

        return tag;
    }
}
