package xiamomc.morph.misc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MorphGameProfile extends GameProfile
{
    /**
     * Constructs a new Game Profile with the specified ID and name.
     * <p/>
     * Either ID or name may be null/empty, but at least one must be filled.
     *
     * @param profile   profile
     * @throws IllegalArgumentException Both ID and name are either null or empty
     */
    public MorphGameProfile(PlayerProfile profile)
    {
        super(profile.getId(), profile.getName());

        profile.getProperties().forEach(p ->
        {
            map.put(p.getName(), new Property(p.getName(), p.getValue(), p.getSignature()));
        });
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

}
