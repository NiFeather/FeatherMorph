package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.player.PlayerModelType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MannequinWatcher extends LivingEntityWatcher
{
    public MannequinWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.MANNEQUIN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.MANNEQUIN);
    }

    private volatile boolean hideDescription = false;

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        switch (property.id())
        {
            case PropertyNames.MANNEQUIN_SKIN_INTERNAL ->
            {
                ResolvableProfile resolvable = (ResolvableProfile) value;
                this.writePersistent(ValueIndex.MANNEQUIN.SKIN_PROFILE, itemProfile(resolvable));
            }

            case PropertyNames.MANNEQUIN_HIDE_DESCRIPTION ->
            {
                hideDescription = (Boolean) value;
                var existingDescription = this.readOr(ValueIndex.MANNEQUIN.DESCRIPTION, Optional.empty()).orElse(null);
                updateDescription(hideDescription, existingDescription);
            }

            case PropertyNames.MANNEQUIN_NPC_DESCRIPTION ->
            {
                updateDescription(hideDescription, (Component) value);
            }
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal + "";

            switch (animId)
            {
                case AnimationNames.LAY ->
                {
                    this.remove(ValueIndex.MANNEQUIN.POSE);
                    this.writePersistent(ValueIndex.MANNEQUIN.POSE, EntityPose.SLEEPING);

                    var playerPos = getBindingPlayer().getLocation();
                    var vec3i = new Vector3i(playerPos.getBlockX(), playerPos.getBlockY(), playerPos.getBlockZ());
                    this.writePersistent(ValueIndex.MANNEQUIN.BED_POS, Optional.of(vec3i));
                }
                case AnimationNames.CRAWL ->
                {
                    resetValues();
                    this.writePersistent(ValueIndex.MANNEQUIN.POSE, EntityPose.SWIMMING);
                }
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.MANNEQUIN.POSE, SpigotConversionUtil.fromBukkitPose(getBindingPlayer().getPose()));
                    resetValues();
                }
            }
        }
    }

    private void resetValues()
    {
        this.remove(ValueIndex.MANNEQUIN.POSE);
        this.writePersistent(ValueIndex.MANNEQUIN.BED_POS, Optional.empty());
        this.remove(ValueIndex.MANNEQUIN.BED_POS);
    }

    public void updateDescription(boolean hideDescription, @Nullable Component description)
    {
        this.writePersistent(ValueIndex.MANNEQUIN.DESCRIPTION, hideDescription ? Optional.empty() : Optional.ofNullable(description));
    }

    public static ItemProfile itemProfile(ResolvableProfile resolvableProfile)
    {
        List<ItemProfile.Property> properties = new ArrayList<>();

        for (ProfileProperty bukkitProperty : resolvableProfile.properties())
        {
            var peProperty = new ItemProfile.Property(bukkitProperty.getName(), bukkitProperty.getValue(), bukkitProperty.getSignature());
            properties.add(peProperty);
        }

        var bukkitSkinPatch = resolvableProfile.skinPatch();
        ItemProfile.SkinPatch skinPatch = new ItemProfile.SkinPatch(
                fromAdventureKey(bukkitSkinPatch.body()),
                fromAdventureKey(bukkitSkinPatch.cape()),
                fromAdventureKey(bukkitSkinPatch.elytra()),
                playerModelType(bukkitSkinPatch.model())
        );

        return new ItemProfile(resolvableProfile.name(), resolvableProfile.uuid(), properties, skinPatch);
    }

    @Nullable
    public static PlayerModelType playerModelType(@Nullable PlayerTextures.SkinModel model)
    {
        if (model == null) return null;

        return switch (model)
        {
            case CLASSIC -> PlayerModelType.WIDE;
            case SLIM -> PlayerModelType.SLIM;
        };
    }

    public static @Nullable ResourceLocation fromAdventureKey(@Nullable Key key)
    {
        return key == null ? null : new ResourceLocation(key);
    }
}
