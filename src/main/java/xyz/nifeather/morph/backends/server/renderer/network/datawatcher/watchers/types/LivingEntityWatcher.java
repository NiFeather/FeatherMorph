package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.kyori.adventure.text.Component;
import net.minecraft.world.InteractionHand;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;
import xyz.nifeather.morph.utilities.AttributeUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    protected final Map<NamespacedKey, AttributeInstance> entityAttributes = new ConcurrentHashMap<>();
    protected final Map<NamespacedKey, AttributeInstance> dirtyAttributes = new ConcurrentHashMap<>();

    @Override
    public boolean containsEntityAttribute(NamespacedKey id)
    {
        return entityAttributes.keySet().stream().anyMatch(a -> a.key().equals(id));
    }

    @Override
    @Nullable
    public AttributeInstance readEntityAttribute(NamespacedKey key)
    {
        return entityAttributes.getOrDefault(key, null);
    }

    @Override
    public void writeEntityAttribute(NamespacedKey id, org.bukkit.attribute.AttributeInstance attribute)
    {
        entityAttributes.put(id, attribute);
        dirtyAttributes.put(id, attribute);

        if (!isSilent())
            sendPacketToAffectedPlayers(buildPartialAttributePacket());
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);

        switch (property.id())
        {
            case PropertyNames.ENTITY_CUSTOM_NAME ->
            {
                Component component = value instanceof Component component1 ? component1 : Component.empty();
                this.writePersistent(ValueIndex.BASE_LIVING.CUSTOM_NAME, component.equals(Component.empty()) ? Optional.empty() : Optional.of(component));
            }

            case PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE ->
            {
                Boolean bool = value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString());
                this.writePersistent(ValueIndex.BASE_LIVING.CUSTOM_NAME_VISIBLE, bool);
            }

            case PropertyNames.ENTITY_ARROW_COUNT ->
            {
                int count = (Integer) value;
                this.writePersistent(ValueIndex.BASE_LIVING.STUCKED_ARROWS, count);
            }

            case PropertyNames.ENTITY_EQUIPMENT ->
            {
                var upcoming = (DisguiseEquipment) value;
                var existing = this.readEntry(CustomEntries.EQUIPMENT);

                var newInstance = DisguiseEquipment.prefilled()
                        .mergeIfNotNull(existing)
                        .merge(upcoming)
                        .build();

                this.writeEntry(CustomEntries.EQUIPMENT, newInstance);
            }

            case PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT ->
            {
                this.writeEntry(CustomEntries.DISPLAY_FAKE_EQUIPMENT, Boolean.TRUE.equals(value));
            }

            case PropertyNames.LIVING_ENTITY_STATIC_HEALTH ->
            {
                this.writePersistent(ValueIndex.BASE_LIVING.HEALTH, ((Number) value).floatValue());
            }

            case PropertyNames.LIVING_ENTITY_BED_POS ->
            {
                var jomlVector = (org.joml.Vector3i) value;

                var peVec3i = new Vector3i(jomlVector.x(), jomlVector.y(), jomlVector.z());
                this.writePersistent(ValueIndex.BASE_LIVING.BED_POS, Optional.of(peVec3i));
            }

            case PropertyNames.LIVING_ENTITY_INVISIBLE ->
            {
                var invisible = (Boolean) value;
                int flag = this.read(ValueIndex.BASE_ENTITY.GENERAL);

                if (invisible)
                    flag |= 0x20;
                else if ((flag & 0x20) == 0x20)
                    flag ^= 0x20;

                this.writePersistent(ValueIndex.BASE_ENTITY.GENERAL, (byte) flag);
            }
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onPropertyDiscard(SingleProperty<X> property)
    {
        super.onPropertyDiscard(property);

        if (property.id().equals(PropertyNames.LIVING_ENTITY_BED_POS))
        {
            this.writePersistent(ValueIndex.BASE_LIVING.BED_POS, Optional.empty());
            this.remove(ValueIndex.BASE_LIVING.BED_POS);
        }
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

    protected WrapperPlayServerUpdateAttributes buildFullAttributePacket()
    {
        dirtyAttributes.clear();

        var map = new ConcurrentHashMap<NamespacedKey, AttributeInstance>();
        var player = getBindingPlayer();

        var syncableAttributes = AttributeUtils.syncableAttributesFor(getEntityType());
        for (Attribute syncableAttribute : syncableAttributes)
        {
            var instance = this.entityAttributes.getOrDefault(syncableAttribute.getKey(), null);
            if (instance == null) instance = player.getAttribute(syncableAttribute);
            if (instance == null) continue;

            map.put(syncableAttribute.getKey(), instance);
        }

        return buildAttributePacket(map);
    }

    protected WrapperPlayServerUpdateAttributes buildPartialAttributePacket()
    {
        var map = new ConcurrentHashMap<>(dirtyAttributes);
        dirtyAttributes.clear();

        return buildAttributePacket(map);
    }

    protected WrapperPlayServerUpdateAttributes buildAttributePacket(Map<NamespacedKey, AttributeInstance> attributes)
    {
        List<WrapperPlayServerUpdateAttributes.Property> attributeProperties = new ObjectArrayList<>();

        attributes.forEach((id, instance) ->
        {
            var packetAttribute = Attributes.getByName(id.asString());
            if (packetAttribute == null) // Yes this is nullable.
            {
                logger.warn("Unknown attribute for packet: " + id.asString());
                return;
            }

            List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ObjectArrayList<>();
            for (AttributeModifier modifier : instance.getModifiers())
            {
                var packetModifier = new WrapperPlayServerUpdateAttributes.PropertyModifier(
                        new ResourceLocation(modifier.getKey().asString()),
                        UUID.randomUUID(),
                        modifier.getAmount(),
                        fromBukkitOperation(modifier.getOperation())
                );

                modifiers.add(packetModifier);
            }

            var property = new WrapperPlayServerUpdateAttributes.Property(packetAttribute, instance.getBaseValue(), modifiers);
            attributeProperties.add(property);
        });

        return new WrapperPlayServerUpdateAttributes(this.readEntryOrThrow(CustomEntries.SPAWN_ID), attributeProperties);
    }

    protected WrapperPlayServerUpdateAttributes.PropertyModifier.Operation fromBukkitOperation(AttributeModifier.Operation bukkitOperation)
    {
        return switch (bukkitOperation)
        {
            case ADD_NUMBER -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION;
            case ADD_SCALAR -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE;
            case MULTIPLY_SCALAR_1 -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL;
        };
    }

    @Override
    public List<PacketWrapper<?>> buildSpawnPackets() throws BuildFailedException
    {
        var packets = new ObjectArrayList<PacketWrapper<?>>();
        var entityPackets = super.buildSpawnPackets();

        packets.addAll(entityPackets);
        packets.add(buildFullAttributePacket());
        packets.add(getEquipmentPacket());

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

    @Override
    public boolean haveAnimation(WrapperPlayServerEntityAnimation.EntityAnimationType animationType)
    {
        return animationType != WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP;
    }
}
