package xyz.nifeather.morph.misc.gui;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    private final ItemStack defaultItem = new ItemStack(Material.BEDROCK);

    private void init()
    {
        for (EntityType value : EntityType.values())
        {
            if (value == EntityType.PLAYER || value == EntityType.UNKNOWN) continue;
            if (!value.isAlive()) continue;

            this.register(value);
        }

        register(EntityType.ENDER_DRAGON, Material.DRAGON_HEAD);
        register(EntityType.CREEPER, Material.CREEPER_HEAD);
        register(EntityType.SKELETON, Material.SKELETON_SKULL);
        register(EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SKULL);
        register(EntityType.ZOMBIE, Material.ZOMBIE_HEAD);

        register(EntityType.BEE, Material.HONEYCOMB);

        register(EntityType.PIGLIN, Material.PIGLIN_HEAD);
        register(EntityType.PIGLIN_BRUTE, Material.GOLDEN_AXE);

        register(EntityType.ARMADILLO, Material.ARMADILLO_SCUTE);
        register(EntityType.AXOLOTL, Material.AXOLOTL_BUCKET);
        register(EntityType.ARMOR_STAND, Material.ARMOR_STAND);
        register(EntityType.GIANT, Material.ZOMBIE_HEAD);
        register(EntityType.ILLUSIONER, Material.SPECTRAL_ARROW);
        register(EntityType.BLAZE, Material.BLAZE_ROD);

        var poisonArrow = new ItemStack(Material.TIPPED_ARROW);
        poisonArrow.editMeta(PotionMeta.class, potionMeta ->
        {
            potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 1, 0), true);
        });

        register(EntityType.BOGGED.key().asString(), poisonArrow);

        register(EntityType.GHAST, Material.FIRE_CHARGE);

        register(EntityType.BREEZE, Material.WIND_CHARGE);
        register(EntityType.CAMEL, Material.CACTUS);
        register(EntityType.CHICKEN, Material.CHICKEN);
        register(EntityType.COD, Material.COD);
        register(EntityType.COW, Material.MILK_BUCKET);
        register(EntityType.DROWNED, Material.TRIDENT);
        register(EntityType.ENDERMAN, Material.ENDER_PEARL);
        register(EntityType.FOX, Material.SWEET_BERRIES);
        register(EntityType.GLOW_SQUID, Material.GLOW_INK_SAC);
        register(EntityType.GUARDIAN, Material.PRISMARINE_CRYSTALS);
        register(EntityType.HORSE, Material.SADDLE);
        register(EntityType.HUSK, Material.SAND);
        register(EntityType.IRON_GOLEM, Material.IRON_BLOCK);
        register(EntityType.MAGMA_CUBE, Material.MAGMA_BLOCK);
        register(EntityType.MOOSHROOM, Material.RED_MUSHROOM_BLOCK);
        register(EntityType.PANDA, Material.BAMBOO);
        register(EntityType.PHANTOM, Material.PHANTOM_MEMBRANE);
        register(EntityType.PIG, Material.PORKCHOP);
        register(EntityType.PILLAGER, Material.CROSSBOW);
        register(EntityType.PUFFERFISH, Material.PUFFERFISH);
        register(EntityType.RABBIT, Material.RABBIT_FOOT);
        register(EntityType.SALMON, Material.SALMON);
        register(EntityType.SHEEP, Material.WHITE_WOOL);
        register(EntityType.SHULKER, Material.SHULKER_SHELL);
        register(EntityType.SLIME, Material.SLIME_BALL);
        register(EntityType.SNIFFER, Material.SNIFFER_EGG);
        register(EntityType.SNOW_GOLEM, Material.SNOWBALL);
        register(EntityType.SPIDER, Material.SPIDER_EYE);
        register(EntityType.SQUID, Material.INK_SAC);

        var slownessArrow = new ItemStack(Material.TIPPED_ARROW);
        slownessArrow.editMeta(PotionMeta.class, potionMeta ->
        {
            potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1, 0), false);
        });

        register(EntityType.STRAY.key().asString(), slownessArrow);

        register(EntityType.STRIDER, Material.WARPED_FUNGUS_ON_A_STICK);
        register(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH);
        register(EntityType.TURTLE, Material.TURTLE_EGG);
        register(EntityType.WARDEN, Material.SCULK_CATALYST);
        register(EntityType.WITCH, Material.SPLASH_POTION);
        register(EntityType.WITHER, Material.NETHER_STAR);
        register(EntityType.WOLF, Material.WOLF_ARMOR);
        register(EntityType.ZOMBIFIED_PIGLIN, Material.GOLDEN_SWORD);

        register(EntityType.GOAT, Material.GOAT_HORN);

        register(EntityType.TADPOLE, Material.TADPOLE_BUCKET);
    }

    private Material lookupEntitySpawnEgg(EntityType type)
    {
        var name = "%s_SPAWN_EGG".formatted(type.name().toUpperCase());

        var match = Material.matchMaterial(name);
        if (match == null)
        {
            //MorphPlugin.getInstance().getSLF4JLogger().warn("No spawn egg found for type " + type + "!");
            return defaultItem.getType();
        }
        else
        {
            return match;
        }
    }

    private void register(EntityType type)
    {
        this.register(type, lookupEntitySpawnEgg(type));
    }

    private void register(EntityType type, Material material)
    {
        var item = new ItemStack(material);

        item.editMeta(meta -> meta.setRarity(ItemRarity.COMMON));

        this.register(type.key().asString(), item);
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
        var stack = new ItemStack(Material.PLAYER_HEAD);

        stack.editMeta(meta -> meta.setRarity(ItemRarity.COMMON));

        PlayerSkinProvider.getInstance().fetchSkin(playerName)
                .thenAccept(optional ->
                {
                    if (optional.isEmpty()) return;

                    stack.editMeta(SkullMeta.class, skullMeta ->
                            skullMeta.setPlayerProfile(new CraftPlayerProfile(optional.get())));
                });

        return stack;
    }
}
