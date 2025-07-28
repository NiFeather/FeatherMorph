package xyz.nifeather.morph.utilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DisguiseUtils
{
    public static int GHAST_EXECUTE_DELAY = 16;

    private static final String customDataTagName = "XIAMO_MORPH";

    public static String asString(DisguiseMeta info)
    {
        return info.getKey();
    }

    public static boolean validForHeadMorph(Material material)
    {
        return material == Material.DRAGON_HEAD
                || material == Material.PLAYER_HEAD
                || material == Material.ZOMBIE_HEAD
                || material == Material.SKELETON_SKULL
                || material == Material.WITHER_SKELETON_SKULL
                || material == Material.PIGLIN_HEAD;
    }

    /**
     * 获取和某一玩家附近多少格以内的所有玩家
     * @param player 目标玩家
     * @param distance 距离
     * @param includeSelf 是否包括自己
     * @return 玩家列表
     */
    public static List<Player> findNearbyPlayers(Player player, int distance, boolean includeSelf)
    {
        var value = new ObjectArrayList<Player>();

        var loc = player.getLocation();
        player.getWorld().getPlayers().forEach(p ->
        {
            if (p.getLocation().distance(loc) <= distance)
                value.add(p);
        });

        if (!includeSelf)
            value.remove(player);

        return value;
    }

    public static ItemStack[] chooseStack(ItemStack[] playerStack, ItemStack[] disguiseStack)
    {
        return Arrays.stream(disguiseStack).allMatch(s -> s == null || s.getType().isAir())
                ? playerStack
                : disguiseStack;
    }

    public static ItemStack[] getHandItems(Player player)
    {
        var equipment = player.getEquipment();
        return new ItemStack[]
                {
                        itemOrAir(equipment.getItemInMainHand()),
                        itemOrAir(equipment.getItemInOffHand())
                };
    }

    public static ItemStack itemOrAir(ItemStack stack)
    {
        return ItemUtils.itemOrAir(stack);
    }

    public static boolean gameModeMirrorable(Player player)
    {
        var nmsPlayerMode = NmsRecord.ofPlayer(player).gameMode;
        return nmsPlayerMode.isSurvival();
    }

    //region Ambient sound

    private static final Map<EntityType, EntityTypeUtils.SoundInfo> typeSoundMap = new Object2ObjectArrayMap<>();

    static
    {
        typeSoundMap.put(EntityType.BEE, new EntityTypeUtils.SoundInfo(SoundEvents.BEE_LOOP, SoundSource.NEUTRAL, 120, 1));
        typeSoundMap.put(EntityType.ENDER_DRAGON, new EntityTypeUtils.SoundInfo(SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.HOSTILE,100, 5));
        //typeSoundMap.put(EntityType.WOLF, new EntityTypeUtils.SoundInfo(SoundEvents.WOLF_AMBIENT, SoundSource.NEUTRAL, 80, 1));
    }

    @NotNull
    public static EntityTypeUtils.SoundInfo getAmbientSound(DisguiseState session, EntityType bukkitType, World tickingWorld, Location tickingLocation)
    {
        if (bukkitType == EntityType.UNKNOWN)
            return new EntityTypeUtils.SoundInfo(null, SoundSource.PLAYERS, Integer.MAX_VALUE, 1);

        var cache = typeSoundMap.getOrDefault(bukkitType, null);
        if (cache != null) return cache;

        var nmsType = EntityTypeUtils.getNmsType(bukkitType);
        if (nmsType == null)
            return new EntityTypeUtils.SoundInfo(null, SoundSource.PLAYERS, Integer.MAX_VALUE, 1);

        var entity = EntityTypeUtils.createEntityThenDispose(nmsType, tickingWorld, tickingLocation);

        //todo: Make DisguiseState records Wolves' SoundVariant
        if (entity instanceof Mob mob)
        {
            var source = mob.getSoundSource();
            var sound = mob.getAmbientSound0();
            var interval = mob.getAmbientSoundInterval();

            var rec = new EntityTypeUtils.SoundInfo(sound, source, interval, mob.getSoundVolume());
            typeSoundMap.put(bukkitType, rec);

            return rec;
        }

        return new EntityTypeUtils.SoundInfo(null, SoundSource.PLAYERS, Integer.MAX_VALUE, 1);
    }

    //endregion Ambient sound
}
