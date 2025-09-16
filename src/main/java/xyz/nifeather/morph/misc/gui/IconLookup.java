package xyz.nifeather.morph.misc.gui;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconLookup
{
    private static IconLookup instance;

    public static IconLookup instance()
    {
        if (instance == null) instance = new IconLookup();

        return instance;
    }

    public IconLookup()
    {
        init();
    }

    // DisguiseIdentifier <-> IconItem
    private final Map<String, ItemStack> registry = new ConcurrentHashMap<>();

    protected ItemStack generateDefaultItem()
    {
        var item = ItemStack.of(Material.SNOWBALL);
        item.editMeta(meta -> meta.setItemModel(NamespacedKey.minecraft("bedrock")));

        return item;
    }

    private final ItemStack defaultItem = generateDefaultItem();

    private void init()
    {
        for (EntityType value : EntityType.values())
        {
            if (value == EntityType.PLAYER || value == EntityType.UNKNOWN) continue;
            if (!value.isAlive()) continue;

            this.register(value);
        }

        register(EntityType.ARMOR_STAND, Material.ARMOR_STAND);
        register(EntityType.GIANT, Material.ZOMBIE_HEAD);
        register(EntityType.ILLUSIONER, Material.SPECTRAL_ARROW);
    }

    private ItemStack lookupEntitySpawnEgg(EntityType type)
    {
        var name = "%s_SPAWN_EGG".formatted(type.name().toUpperCase());

        var match = Material.matchMaterial(name);
        if (match == null)
        {
            return defaultItem;
        }
        else
        {
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
            var item = ItemStack.of(Material.SNOWBALL);
            item.editMeta(meta -> meta.setItemModel(key));

            return item;
        }
    }

    private void register(EntityType type)
    {
        this.register(type, lookupEntitySpawnEgg(type));
    }

    private void register(EntityType type, ItemStack item)
    {
        item.editMeta(meta -> meta.setRarity(ItemRarity.COMMON));
        this.register(type.key().asString(), item);
    }

    private void register(EntityType type, Material material)
    {
        register(type, ItemStack.of(material));
    }

    private void register(String disguiseIdentifier, ItemStack stack)
    {
        registry.put(disguiseIdentifier, stack);
    }

    public ItemStack lookup(String disguiseIdentifier)
    {
        ItemStack item;
        if (disguiseIdentifier.startsWith(DisguiseTypes.PLAYER.getNameSpace()))
            item = lookupPlayer(DisguiseTypes.PLAYER.toStrippedId(disguiseIdentifier));
        else
            item = this.registry.getOrDefault(disguiseIdentifier, defaultItem);

        return item;
    }

    public ItemStack lookupPlayer(String playerName)
    {
        var stack = new ItemStack(Material.SNOWBALL);

        stack.setData(DataComponentTypes.RARITY, ItemRarity.COMMON);
        stack.setData(DataComponentTypes.ITEM_MODEL, Key.key("player_head"));

        PlayerSkinProvider.getInstance().fetchSkin(playerName)
                .thenAccept(optional ->
                {
                    if (optional.isEmpty()) return;

                    stack.setData(DataComponentTypes.PROFILE,
                            ResolvableProfile.resolvableProfile(new CraftPlayerProfile(optional.get())));
                });

        return stack;
    }
}
