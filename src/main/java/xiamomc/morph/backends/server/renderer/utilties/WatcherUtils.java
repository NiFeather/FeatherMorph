package xiamomc.morph.backends.server.renderer.utilties;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma.AbstractSlimeWatcher;

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
/*
            case HORSE ->
            {
                var color = ((HorseWatcher) watcher).getColor().ordinal();
                var style = ((HorseWatcher) watcher).getStyle().ordinal();
                compoundTag.putInt("Variant", color | style << 8);
            }

            case PARROT ->
            {
                var variant = ((ParrotWatcher) watcher).getVariant().ordinal();
                compoundTag.putInt("Variant", variant);
            }

            case CAT ->
            {
                var variant = ((CatWatcher) watcher).getType().getKey().asString();
                compoundTag.putString("variant", variant);
            }

            case TROPICAL_FISH ->
            {
                var variant = ((TropicalFishWatcher) watcher).getVariant();

                compoundTag.putInt("Variant", variant);
            }

            case RABBIT ->
            {
                var type = ((RabbitWatcher) watcher).getType().getTypeId();
                compoundTag.putInt("RabbitType", type);
            }
 */

            case FOX ->
            {
                var foxType = watcher.get(ValueIndex.FOX.VARIANT) == 0 ? "red" : "snow";
                tag.putString("Type", foxType);
            }
/*
            case FROG ->
            {
                var variant = ((FrogWatcher) watcher).getVariant().getKey().asString();
                compoundTag.putString("variant", variant);
            }

            case GOAT ->
            {
                var goatWatcher = ((GoatWatcher) watcher);

                var hasLeftHorn = goatWatcher.hasLeftHorn();
                var hasRightHorn = goatWatcher.hasRightHorn();
                var isScreaming = goatWatcher.isScreaming();

                compoundTag.putBoolean("HasLeftHorn", hasLeftHorn);
                compoundTag.putBoolean("HasRightHorn", hasRightHorn);
                compoundTag.putBoolean("IsScreamingGoat", isScreaming);
            }

            case PANDA ->
            {
                var pandaWatcher = ((PandaWatcher) watcher);
                var mainGene = pandaWatcher.getMainGene();
                var hiddenGene = pandaWatcher.getHiddenGene();

                compoundTag.putString("MainGene", mainGene.toString().toLowerCase());
                compoundTag.putString("HiddenGene", hiddenGene.toString().toLowerCase());
            }

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
