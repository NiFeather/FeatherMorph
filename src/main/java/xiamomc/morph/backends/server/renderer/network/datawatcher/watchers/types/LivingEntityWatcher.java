package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.misc.NmsRecord;

import java.util.List;
import java.util.Map;

public class LivingEntityWatcher extends EntityWatcher implements Listener
{
    public LivingEntityWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.BASE_LIVING);
    }

    private final Map<Player, EquipmentSlot> handMap = new Object2ObjectOpenHashMap<>();

    @EventHandler
    public void onPlayerStartUsingItem(PlayerInteractEvent e)
    {
        handMap.put(e.getPlayer(), e.getHand());
    }

    @Override
    protected void doSync()
    {
        var player = getBindingPlayer();
        var nmsPlayer = NmsRecord.ofPlayer(player);
        var values = ValueIndex.BASE_LIVING;

        write(values.HEALTH, (float)player.getHealth());

        var flagBit = 0x00;

        if (nmsPlayer.isUsingItem())
        {
            flagBit |= 0x01;

            var handInUse = handMap.remove(player);

            if (handInUse == null)
            {
                logger.warn("No hand in use but using item? Defaulting to HAND");
                handInUse = EquipmentSlot.HAND;
            }

            boolean isOffhand = handInUse == EquipmentSlot.OFF_HAND;
            if (isOffhand) flagBit |= 0x02;
        }

        if (player.isRiptiding())
            flagBit |= 0x04;

        write(values.LIVING_FLAGS, (byte)flagBit);

        int potionColor = 0;
        List<Color> colors = new ObjectArrayList<>();
        boolean hasAmbient = false;
        for (PotionEffect effect : player.getActivePotionEffects())
        {
            if (!effect.hasParticles())
                continue;

            colors.add(effect.getType().getColor());

            if (effect.isAmbient())
                hasAmbient = effect.isAmbient();
        }

        if (!colors.isEmpty())
        {
            var firstColor = colors.remove(0);
            var finalColor = firstColor.mixColors(colors.toArray(new Color[]{}));
            potionColor = finalColor.asRGB();
        }

        write(values.POTION_COLOR, potionColor);
        write(values.POTION_ISAMBIENT, hasAmbient);

        write(values.STUCKED_ARROWS, player.getArrowsInBody());
        write(values.BEE_STINGERS, player.getBeeStingersInBody());

        super.doSync();
    }
}
