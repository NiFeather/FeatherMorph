package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import it.unimi.dsi.fastutil.Pair;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.InteractionHand;
import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.NmsRecord;

import java.util.List;
import java.util.Optional;

public class LivingEntityWatcher extends EntityWatcher
{
    public LivingEntityWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.BASE_LIVING);
    }

    private final Pair<Player, EquipmentSlot> handPair = new ObjectObjectMutablePair<>(null, null);

    public void onPlayerStartUsingItem(PlayerInteractEvent e)
    {
        if (!this.isPlayerOnline()) return;
        if (!this.getBindingPlayer().equals(e.getPlayer())) return;

        handPair.left(e.getPlayer());
        handPair.right(e.getHand());
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var player = getBindingPlayer();
        var nmsPlayer = NmsRecord.ofPlayer(player);
        var values = ValueIndex.BASE_LIVING;

        write(values.HEALTH, (float)player.getHealth());

        var flagBit = 0x00;

        if (nmsPlayer.isUsingItem())
        {
            flagBit |= 0x01;

            var handInUse = handPair.right();

            if (handInUse == null)
            {
                var nmsHand = nmsPlayer.getUsedItemHand();
                handInUse = nmsHand == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
            }

            boolean isOffhand = handInUse == EquipmentSlot.OFF_HAND;
            if (isOffhand) flagBit |= 0x02;
        }

        if (player.isRiptiding())
            flagBit |= 0x04;

        write(values.LIVING_FLAGS, (byte)flagBit);

        List<Color> colors = new ObjectArrayList<>();
        boolean hasAmbient = false;
        for (PotionEffect effect : player.getActivePotionEffects())
        {
            if (effect.hasParticles())
                colors.add(effect.getType().getColor());

            hasAmbient = hasAmbient || effect.isAmbient();
        }

        var colorList = new ObjectArrayList<Particle<?>>();
        for (var color : colors)
            colorList.add(new Particle<>(ParticleTypes.ENTITY_EFFECT, new ParticleColorData(color.asRGB())));

        write(values.POTION_COLOR, colorList);
        write(values.POTION_ISAMBIENT, hasAmbient);

        write(values.STUCKED_ARROWS, player.getArrowsInBody());
        write(values.BEE_STINGERS, player.getBeeStingersInBody());

        Vector3i bedPos = null;
        if (player.isSleeping())
        {
            try
            {
                var bukkitPos = player.getBedLocation();
                bedPos = new Vector3i(bukkitPos.blockX(),bukkitPos.blockY(), bukkitPos.blockZ());
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while processing bed pos: " + t.getMessage());
            }
        }

        write(values.BED_POS, bedPos == null ? Optional.empty() : Optional.of(bedPos));

        super.doSync();
    }
}
