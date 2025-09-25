package xyz.nifeather.morph.utilities;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GameProfileUtils
{
    public static GameProfile convertPlayerProfile(@NotNull PlayerProfile profile)
    {
        ImmutableMultimap.Builder<String, Property> map = ImmutableMultimap.builder();
        profile.getProperties().forEach(p ->
                map.put(p.getName(), new Property(p.getName(), p.getValue(), p.getSignature())));

        return new GameProfile(profile.getId(), profile.getName(), new PropertyMap(map.build()));
    }

    public static UserProfile toPacketEventsUserProfile(GameProfile profile)
    {
        var userProfile = new UserProfile(profile.id(), profile.name());

        List<TextureProperty> propertyList = new ObjectArrayList<>();
        profile.properties().forEach((str, property) ->
        {
            if (property == null) return;

            var textureProperty = new TextureProperty(property.name(), property.value(), property.signature());
            propertyList.add(textureProperty);
        });

        userProfile.setTextureProperties(propertyList);

        return userProfile;
    }
}