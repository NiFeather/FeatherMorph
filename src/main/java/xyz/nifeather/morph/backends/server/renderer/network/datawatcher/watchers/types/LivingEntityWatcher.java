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
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LivingEntityWatcher extends EntityWatcher
{
    public LivingEntityWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.BASE_LIVING);
    }

    private final Pair<Player, EquipmentSlot> itemConsumingPair = new ObjectObjectMutablePair<>(null, null);

    public void onPlayerStartUsingItem(PlayerInteractEvent e)
    {
        if (!this.isActive()) return;
        if (!bindTarget.equals(e.getPlayer())) return;

        itemConsumingPair.left(e.getPlayer());
        itemConsumingPair.right(e.getHand());
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);
        if (property.equals(properties.CUSTOM_NAME))
        {
            Component component = value instanceof Component component1 ? component1 : Component.empty();
            this.writePersistent(ValueIndex.BASE_LIVING.CUSTOM_NAME, component.equals(Component.empty()) ? Optional.empty() : Optional.of(component));
        }
        else if (property.equals(properties.CUSTOM_NAME_VISIBLE))
        {
            Boolean bool = value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString());
            this.writePersistent(ValueIndex.BASE_LIVING.CUSTOM_NAME_VISIBLE, bool);
        }
        else if (property.equals(properties.STUCKED_ARROWS))
        {
            int count = (Integer) value;
            this.writePersistent(ValueIndex.BASE_LIVING.STUCKED_ARROWS, count);
        }
        else if (property.equals(properties.EQUIPMENT))
        {
            var upcoming = (DisguiseEquipment) value;
            var existing = this.readEntry(CustomEntries.EQUIPMENT);

            var newInstance = DisguiseEquipment.prefilled()
                    .mergeIfNotNull(existing)
                    .merge(upcoming)
                    .build();

            this.writeEntry(CustomEntries.EQUIPMENT, newInstance);
        }
        else if (property.equals(properties.DISPLAY_DISGUISE_EQUIPMENT))
        {
            this.writeEntry(CustomEntries.DISPLAY_FAKE_EQUIPMENT, Boolean.TRUE.equals(value));
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.DISPLAY_FAKE_EQUIPMENT) || entry.equals(CustomEntries.EQUIPMENT))
        {
            if (!isSilent())
                sendPacketToAffectedPlayers(this.getEquipmentPacket());
        }
    }

    protected WrapperPlayServerUpdateAttributes buildAttributePacket()
    {
        List<WrapperPlayServerUpdateAttributes.Property> attributeProperties = new ObjectArrayList<>();

        bindTarget.syncableAttributes().forEach(instance ->
        {
            String id = instance.getAttribute().key().asString();
            var packetAttribute = Attributes.getByName(id);

            List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ObjectArrayList<>();
            for (var modifier : instance.getModifiers())
            {
                var packetModifier = new WrapperPlayServerUpdateAttributes.PropertyModifier(
                        new ResourceLocation(modifier.key().asString()),
                        UUID.randomUUID(),
                        modifier.getAmount(),
                        fromBukkitAttributeOperation(modifier.getOperation())
                );

                modifiers.add(packetModifier);
            }

            var property = new WrapperPlayServerUpdateAttributes.Property(packetAttribute, instance.getBaseValue(), modifiers);
            attributeProperties.add(property);
        });

        return new WrapperPlayServerUpdateAttributes(this.readEntryOrThrow(CustomEntries.SPAWN_ID), attributeProperties);
    }

    protected WrapperPlayServerUpdateAttributes.PropertyModifier.Operation fromBukkitAttributeOperation(AttributeModifier.Operation nmsOperation)
    {
        return switch (nmsOperation)
        {
            case ADD_NUMBER -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION;
            case ADD_SCALAR -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE;
            case MULTIPLY_SCALAR_1 -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL;
        };
    }

    @Override
    protected List<PacketWrapper<?>> doBuildSpawnPackets() throws BuildFailedException
    {
        var packets = new ObjectArrayList<PacketWrapper<?>>();
        var entityPackets = super.doBuildSpawnPackets();

        packets.addAll(entityPackets);
        packets.add(buildAttributePacket());

        return packets;
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var values = ValueIndex.BASE_LIVING;

        writeTemp(values.HEALTH, bindTarget.health());

        writeTemp(values.LIVING_FLAGS, bindTarget.livingEntityFlags());

        List<Color> colors = new ObjectArrayList<>();
        boolean hasAmbient = false;
        for (PotionEffect effect : bindTarget.activePotionEffects())
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

        writeTemp(values.STUCKED_ARROWS, bindTarget.arrowsInBody());
        writeTemp(values.BEE_STINGERS, bindTarget.beeStingersInBody());

        Optional<Vector3i> peBedPos = Optional.empty();
        var sleepingPos = bindTarget.sleepingPos();
        if (sleepingPos.isPresent())
        {
            var pos = sleepingPos.get();
            peBedPos = Optional.of(new Vector3i(pos.x(), pos.y(), pos.z()));
        }

        writeTemp(values.BED_POS, peBedPos);
    }
}
