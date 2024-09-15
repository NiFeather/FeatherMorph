package xiamomc.morph.misc.gui;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiamomc.morph.misc.DisguiseTypes;

import java.util.Map;
import java.util.Optional;
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

        register(EntityType.PIGLIN, Material.PIGLIN_HEAD);
        register(EntityType.PIGLIN_BRUTE, Material.GOLDEN_AXE);

        register(EntityType.ARMADILLO, Material.ARMADILLO_SCUTE);
        register(EntityType.AXOLOTL, Material.AXOLOTL_BUCKET);
        register(EntityType.ARMOR_STAND, Material.ARMOR_STAND);
        register(EntityType.GIANT, Material.ZOMBIE_HEAD);
        register(EntityType.ILLUSIONER, Material.SPECTRAL_ARROW);
        register(EntityType.BLAZE, Material.BLAZE_ROD);
        register(EntityType.BOGGED, Material.BROWN_MUSHROOM);
        register(EntityType.BREEZE, Material.BREEZE_ROD);
        register(EntityType.CAMEL, Material.CACTUS);
        //register(EntityType.CAT, Material.STRING);
        register(EntityType.CHICKEN, Material.FEATHER);
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
        //register(EntityType.POLAR_BEAR, Material.SNOWBALL);
        register(EntityType.PUFFERFISH, Material.PUFFERFISH);
        register(EntityType.RABBIT, Material.RABBIT_FOOT);
        register(EntityType.SALMON, Material.SALMON);
        register(EntityType.SHEEP, Material.WHITE_WOOL);
        register(EntityType.SHULKER, Material.SHULKER_SHELL);
        //register(EntityType.SKELETON_HORSE, Material.BONE);
        register(EntityType.SLIME, Material.SLIME_BALL);
        register(EntityType.SNIFFER, Material.SNIFFER_EGG);
        register(EntityType.SNOW_GOLEM, Material.SNOWBALL);
        register(EntityType.SPIDER, Material.SPIDER_EYE);
        register(EntityType.SQUID, Material.INK_SAC);
        register(EntityType.STRAY, Material.TIPPED_ARROW);
        register(EntityType.STRIDER, Material.WARPED_FUNGUS_ON_A_STICK);
        register(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_BUCKET);
        register(EntityType.TURTLE, Material.TURTLE_EGG);
        register(EntityType.WARDEN, Material.SCULK_CATALYST);
        register(EntityType.WITCH, Material.SPLASH_POTION);
        register(EntityType.WITHER, Material.WITHER_ROSE);
        register(EntityType.WOLF, Material.BONE);
        register(EntityType.ZOMBIFIED_PIGLIN, Material.GOLDEN_SWORD);

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
        this.register(type.key().asString(), new ItemStack(material));
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

        return item.clone();
    }

    public ItemStack lookupPlayer(String playerName)
    {
        var stack = CraftItemStack.asCraftCopy(new ItemStack(Material.PLAYER_HEAD));
        var nmsHandle = stack.handle;

        /*
        // No, this won't display the skin.
        stack.editMeta(SkullMeta.class, skull ->
        {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        });
        */

        // This displays the skin, but only under online mode
        var profile = new ResolvableProfile(Optional.of(playerName), Optional.empty(), new PropertyMap());
        nmsHandle.applyComponents(DataComponentMap.builder()
                        .set(DataComponents.PROFILE, profile)
                        .build());

        return stack;
    }
}
