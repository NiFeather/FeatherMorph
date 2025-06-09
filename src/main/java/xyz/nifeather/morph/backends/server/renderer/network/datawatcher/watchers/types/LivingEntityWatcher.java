package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.utilities.NmsUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    protected WrapperPlayServerUpdateAttributes buildAttributePacket()
    {
        var player = getBindingPlayer();
        List<WrapperPlayServerUpdateAttributes.Property> attributeProperties = new ObjectArrayList<>();

        var nmsPlayer = NmsRecord.ofPlayer(player);

        List<AttributeInstance> attributes = getEntityType() == EntityType.PLAYER
                ? new ObjectArrayList<>(nmsPlayer.getAttributes().getSyncableAttributes())
                : NmsUtils.getValidAttributes(getEntityType(), nmsPlayer.getAttributes());

        attributes.forEach(instance ->
        {
            // Still NMS :(
            var id = BuiltInRegistries.ATTRIBUTE.getKey(instance.getAttribute().value()).toString();

            var packetAttribute = Attributes.getByName(id);
            if (packetAttribute == null)
            {
                logger.warn("Unknown attribute for packet: " + id);
                return;
            }

            List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ObjectArrayList<>();
            for (AttributeModifier modifier : instance.getModifiers())
            {
                var packetModifier = new WrapperPlayServerUpdateAttributes.PropertyModifier(
                        new ResourceLocation(modifier.id().toString()),
                        UUID.randomUUID(),
                        modifier.amount(),
                        fromNMSAttributeOperation(modifier.operation())
                );

                modifiers.add(packetModifier);
            }

            var property = new WrapperPlayServerUpdateAttributes.Property(packetAttribute, instance.getBaseValue(), modifiers);
            attributeProperties.add(property);
        });

        return new WrapperPlayServerUpdateAttributes(player.getEntityId(), attributeProperties);
    }

    protected WrapperPlayServerUpdateAttributes.PropertyModifier.Operation fromNMSAttributeOperation(AttributeModifier.Operation nmsOperation)
    {
        return switch (nmsOperation)
        {
            case ADD_VALUE -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION;
            case ADD_MULTIPLIED_BASE -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE;
            case ADD_MULTIPLIED_TOTAL -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL;
            default -> throw new RuntimeException("Unknown operation: " + nmsOperation);
        };
    }

    @Override
    public List<PacketWrapper<?>> buildSpawnPackets() throws BuildFailedException
    {
        var packets = new ObjectArrayList<PacketWrapper<?>>();
        var entityPackets = super.buildSpawnPackets();

        packets.addAll(entityPackets);
        packets.add(buildAttributePacket());

        return packets;
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var player = getBindingPlayer();
        var nmsPlayer = NmsRecord.ofPlayer(player);
        var values = ValueIndex.BASE_LIVING;

        writeTemp(values.HEALTH, (float)player.getHealth());

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

        writeTemp(values.LIVING_FLAGS, (byte)flagBit);

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
            colorList.add(new Particle<>(com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes.ENTITY_EFFECT, new ParticleColorData(color.asRGB())));

        writeTemp(values.POTION_COLOR, colorList);
        writeTemp(values.POTION_ISAMBIENT, hasAmbient);

        writeTemp(values.STUCKED_ARROWS, player.getArrowsInBody());
        writeTemp(values.BEE_STINGERS, player.getBeeStingersInBody());

        Optional<Vector3i> bedPos = Optional.empty();
        if (player.isSleeping())
        {
            var bukkitPos = player.getBedLocation();
            bedPos = Optional.of(
                    new Vector3i(bukkitPos.blockX(), bukkitPos.blockY(), bukkitPos.blockZ())
            );
        }

        writeTemp(values.BED_POS, bedPos);
    }
}
