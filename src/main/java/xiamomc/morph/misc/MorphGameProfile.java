package xiamomc.morph.misc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

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

        this.name = profile.getName();

        profile.getProperties().forEach(p ->
        {
            map.put(p.getName(), new Property(p.getName(), p.getValue(), p.getSignature()));
        });
    }

    private String name;

    public MorphGameProfile(GameProfile profile)
    {
        super(profile.getId(), profile.getName());

        this.name = profile.getName();

        profile.getProperties().forEach((s, p) ->
        {
            map.put(p.name(), new Property(p.name(), p.value(), p.signature()));
        });
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
