package xiamomc.morph.misc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MorphGameProfile extends GameProfile
{
    private String name;
    private UUID uuid;

    /**
     * Constructs a new Game Profile with the specified ID and name.
     * <p/>
     * Either ID or name may be null/empty, but at least one must be filled.
     *
     * @param profile   profile
     * @throws IllegalArgumentException Both ID and name are either null or empty
     */
    public MorphGameProfile(@NotNull PlayerProfile profile)
    {
        super(
                Objects.requireNonNull(profile.getId(), "Null profile ID!"),
                Objects.requireNonNull(profile.getName(), "Null profile Name!")
        );

        setUUID(profile.getId());
        setName(profile.getName());

        profile.getProperties().forEach(p ->
                map.put(p.getName(), new Property(p.getName(), p.getValue(), p.getSignature())));
    }

    public MorphGameProfile(GameProfile profile)
    {
        super(profile.getId(), profile.getName());

        setUUID(profile.getId());
        setName(profile.getName());

        profile.getProperties().forEach((s, p) ->
                map.put(p.name(), new Property(p.name(), p.value(), p.signature())));
    }

    @Override
    public String getName()
    {
        return name != null ? name : super.getName();
    }

    public void setName(String str)
    {
        if (str == null || str.isBlank() || str.isEmpty()) return;

        this.name = str;
    }

    public void setUUID(UUID newuuid)
    {
        if (newuuid == null)
            throw new IllegalArgumentException("UUID must not be null!");

        this.uuid = newuuid;
    }

    @Override
    public UUID getId()
    {
        //Why
        return uuid != null ? uuid : super.getId();
    }

    private final PropertyMap map = new PropertyMap();

    /**
     * Returns any known properties about this game profile.
     *
     * @return Modifiable map of profile properties.
     */
    @Override
    public PropertyMap getProperties() {
        return map;
    }

    public static UserProfile toPacketEventsUserProfile(GameProfile profile)
    {
        var userProfile = new UserProfile(profile.getId(), profile.getName());

        List<TextureProperty> propertyList = new ObjectArrayList<>();
        profile.getProperties().forEach((str, property) ->
        {
            var textureProperty = new TextureProperty(property.name(), property.value(), property.signature());
            propertyList.add(textureProperty);
        });

        userProfile.setTextureProperties(propertyList);

        return userProfile;
    }
}
