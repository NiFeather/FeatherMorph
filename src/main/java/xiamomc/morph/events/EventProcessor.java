package xiamomc.morph.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.server.TabCompleteEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;

public class EventProcessor extends MorphPluginObject implements Listener
{
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e)
    {
        var entity = e.getEntity();
        var killer = entity.getKiller();

        var logger = Plugin.getSLF4JLogger();

        //logger.warn(entity + "died by:" + killer);

        //盔甲架需要额外的一些东西
        if (entity.getType() == EntityType.ARMOR_STAND)
        {
            //logger.warn("IS armor stand");
            var lastCause = entity.getLastDamageCause();

            //logger.warn("cause: " + String.valueOf(lastCause));
            if (lastCause instanceof  EntityDamageByEntityEvent damageEvent)
            {
                var cause = damageEvent.getDamager();

                //logger.warn("cause entity: " + cause);
                if (cause instanceof Player) killer = (Player) cause;
            }
        }

        if (killer != null)
            this.onPlayerKillEntity(killer, e.getEntity());
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e)
    {
        if (e.isCancelled()) return;

        //从buffer获取指令名
        var buffers = e.getBuffer().split(" ");
        var commandBaseName = "";

        //检查是不是带命名空间的指令
        var split = buffers[0].split(":");
        if (split.length >= 2)
        {
            commandBaseName = split[1];

            //检查命名空间
            if (!split[0].equals(MorphPlugin.getMorphNameSpace())) return;
        }
        else commandBaseName = buffers[0];

        //移除斜杠
        commandBaseName = commandBaseName.replace("/", "");

        switch (commandBaseName) {
            case "morph", "morphplayer" ->
            {
                var sender = e.getSender();
                if (sender instanceof Player player) {
                    //Logger.warn("BUFFERS: " + Arrays.toString(buffers));

                    var arg = "";
                    if (buffers.length >= 2)
                        arg = buffers[1];

                    var isPlayerComplete = buffers[0].contains("morphplayer");

                    var infos = morphs.getAvaliableDisguisesFor(player)
                            .stream().filter(c -> c.isPlayerDisguise == isPlayerComplete).toList();

                    var list = new ArrayList<String>();

                    for (var di : infos) {
                        var name = isPlayerComplete ? di.playerDisguiseTargetName : di.type.getKey().asString();
                        //Logger.warn("INF: " + name + " :: BUF :" + arg + " :: START :" + name.startsWith(arg));
                        if (!name.contains(arg)) continue;

                        list.add(name);
                    }

                    e.setCompletions(list);
                }
            }

            case "sendrequest", "acceptrequest", "denyrequest", "unmorph" ->
            {
                e.setCompletions(new ArrayList<>());
            }
        }
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        if (e.getRightClicked() instanceof Player targetPlayer)
        {
            var sourcePlayer = e.getPlayer();

            if (sourcePlayer.isSneaking())
            {
                //to be
            }
        }
    }

    @Resolved
    private MorphManager morphs;

    private void onPlayerKillEntity(Player player, Entity entity)
    {
        //Plugin.getSLF4JLogger().warn(entity.getType().toString());
        switch (entity.getType())
        {
            case DROPPED_ITEM:
            case SPLASH_POTION:
            case WITHER_SKULL:
            case SMALL_FIREBALL:
            case FIREBALL:
            case ARROW:
            case FISHING_HOOK:
            case ITEM_FRAME:
            case PAINTING:
                return;

            case PLAYER:
                var targetPlayer = (Player) entity;
                morphs.addNewPlayerMorphToPlayer(player, targetPlayer);
                break;

            default:
                morphs.addNewMorphToPlayer(player, entity);
                break;
        }
    }
}
