package xiamomc.morph.backends.fallback;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class NilDisguise implements Cloneable
{
    public NilDisguise(EntityType type)
    {
        this.type = type;
        this.name = type.translationKey();
    }

    public EntityType type;

    public String name;

    public ChatColor glowingColor = ChatColor.WHITE;

    public final Map<String, Object> customData = new Object2ObjectOpenHashMap<>();

    public boolean saddled;

    public GameProfile profile;

    @Override
    protected NilDisguise clone()
    {
        NilDisguise obj;

        try
        {
            obj = (NilDisguise) super.clone();
        }
        catch (Throwable t)
        {
            obj = new NilDisguise(this.type);
        }

        obj.type = this.type;
        obj.name = this.name;
        obj.glowingColor = this.glowingColor;

        obj.saddled = this.saddled;
        obj.profile = this.profile;

        System.out.printf("Cloning %s profile %s to %s profile %s%n", this, this.profile, obj, obj.profile);

        obj.customData.putAll(this.customData);

        return obj;
    }
}
