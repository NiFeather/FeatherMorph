package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.NmsRecord;

import java.util.Arrays;
import java.util.List;

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
}
