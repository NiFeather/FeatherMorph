package xyz.nifeather.morph.misc.gui;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.values.MannequinPropertyCollection;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;
import xyz.nifeather.morph.utilities.GameProfileUtils;

import java.util.*;
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

    private final List<ITextIconProvider> customComponentSupplier = ObjectLists.synchronize(new ObjectArrayList<>());
    
    protected Material getDisplayBaseMaterial()
    {
        return Material.SNOWBALL;
    }

    protected ItemStack generateDefaultItem()
    {
        var item = ItemStack.of(getDisplayBaseMaterial());
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("bedrock"));

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

        register(EntityType.ARMOR_STAND, createIconForType(Material.ARMOR_STAND));
        register(EntityType.GIANT, createIconForType(Material.ZOMBIE_HEAD));
        register(EntityType.ILLUSIONER, createIconForType(Material.SPECTRAL_ARROW));
        register(EntityType.MANNEQUIN, createIconForType(Material.PLAYER_HEAD));

        registerComponentIconProvider(this::lookupGenericIcon);
    }

    public void registerComponentIconProvider(ITextIconProvider supplier)
    {
        customComponentSupplier.add(supplier);
    }

    private ItemStack createIconForEntityType(EntityType type)
    {
        var name = "%s_SPAWN_EGG".formatted(type.name().toUpperCase());

        var match = Material.matchMaterial(name);
        if (match == null)
            return defaultItem;
        else
            return createIconForType(match);
    }

    private void register(EntityType type)
    {
        this.register(type, createIconForEntityType(type));
    }

    private void register(EntityType type, ItemStack item)
    {
        item.editMeta(meta -> meta.setRarity(ItemRarity.COMMON));
        this.register(type.key().asString(), item);
    }

    private void register(EntityType type, Material material)
    {
        register(type, createIconForType(material));
    }

    private void register(String disguiseIdentifier, ItemStack stack)
    {
        registry.put(disguiseIdentifier, stack);
    }

    private ItemStack createIconForType(Material targetModelMaterial)
    {
        var materialItem = ItemStack.of(targetModelMaterial);
        var model = materialItem.getData(DataComponentTypes.ITEM_MODEL);

        Key modelKey;
        if (model == null)
            modelKey = targetModelMaterial.getKey();
        else
            modelKey = model.key();

        var targetItem = ItemStack.of(getDisplayBaseMaterial());
        targetItem.setData(DataComponentTypes.ITEM_MODEL, modelKey);
        return targetItem;
    }

    private ItemStack createIconForPlayer(String playerName)
    {
        var stack = createIconForType(Material.PLAYER_HEAD);
        stack.setData(DataComponentTypes.RARITY, ItemRarity.COMMON);

        PlayerSkinProvider.getInstance().fetchSkin(playerName)
                .thenAccept(optional ->
                {
                    if (optional.isEmpty()) return;

                    stack.setData(DataComponentTypes.PROFILE,
                            ResolvableProfile.resolvableProfile(new CraftPlayerProfile(optional.get())));
                });

        return stack;
    }

    //region Icon lookup API

    @Nullable
    private Component fallbackTextIcon;

    public Component fallbackTextIcon()
    {
        if (fallbackTextIcon != null)
            return fallbackTextIcon;

        // https://namemc.com/skin/521a2092e3d4d8ff
        var profile = ResolvableProfile.resolvableProfile()
                .addProperty(new ProfileProperty("textures", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg5NmEzN2Y2NTU2MDkyNjVjMGZhYjM2ZWI0MzY1YjFiMWM1YTYwY2ViOWQzYWE5ZmVjMmJjNWU3NTZhY2QifX19"))
                .build();

        var component = Component.object(ObjectContents.playerHead(profile));
        fallbackTextIcon = component;

        return component;
    }

    /**
     * Lookup text icon for the given disguise identifier.<br>
     * Note that to not confuse with {@link IconLookup#lookupDisguiseIcon(DisguiseState)}, as this function will only return *static* for the given disguise. <br>
     * <br>
     * > Skin for Mannequin disguises will only return a steve face.<br>
     * > Skin for Player disguises will only return skin for target name, ignoring the skin that has been actually set.
     * @param disguiseIdentifier The disguise identifier
     * @return An instance of {@link Component}
     */
    @NotNull
    public Component lookupTextIcon(String disguiseIdentifier)
    {
        for (ITextIconProvider stringOptionalFunction : this.customComponentSupplier)
        {
            var result = stringOptionalFunction.resolve(disguiseIdentifier);
            System.out.println("Text icon for %s is %s".formatted(disguiseIdentifier, result.orElse(null)));
            if (result.isPresent()) return result.get();
        }

        return fallbackTextIcon();
    }

    public Optional<Component> getSkullIcon(Key modelKey)
    {
        var asString = modelKey.value();

        switch (asString)
        {
            case "player_head" ->
            {
                var head = ObjectContents.playerHead().texture(Key.key("entity/player/wide/steve"));
                return Optional.of(Component.object(head.build()));
            }

            case "zombie_head" ->
            {
                var head = ObjectContents.playerHead().texture(Key.key("entity/zombie/zombie"));
                return Optional.of(Component.object(head.build()));
            }
        }

        return Optional.empty();
    }

    public Optional<Component> lookupGenericIcon(String disguiseIdentifier)
    {
        var item = lookup(disguiseIdentifier);

        var profile = item.getData(DataComponentTypes.PROFILE);
        if (profile != null)
            return Optional.of(Component.object(ObjectContents.playerHead(profile)));

        var model = item.getData(DataComponentTypes.ITEM_MODEL);
        if (model != null)
        {
            if (model.asString().contains("skull") || model.asString().contains("head"))
                return getSkullIcon(model);

            return Optional.of(Component.object(ObjectContents.sprite(Key.key("items"), Key.key("item/" + model.value()))));
        }

        return Optional.empty();
    }

    public ItemStack lookup(String disguiseIdentifier)
    {
        ItemStack item;
        if (disguiseIdentifier.startsWith(DisguiseTypes.PLAYER.getNameSpace()))
            item = createIconForPlayer(DisguiseTypes.PLAYER.toStrippedId(disguiseIdentifier));
        else
            item = this.registry.getOrDefault(disguiseIdentifier, defaultItem);

        return item;
    }

    //region Dynamic icon lookup

    /**
     * Lookup text icon for the given disguise.<br>
     * If the disguise contains {@link PropertyNames#MANNEQUIN_SKIN} or {@link PropertyNames#PLAYER_SKIN} property set, this will return disguise's skin
     * @param state The disguise to lookup
     * @return An instance of {@link Component}
     */
    public Component lookupDisguiseIcon(DisguiseState state)
    {
        var propertyHandler = state.disguisePropertyHandler();

        if (propertyHandler.contains(PropertyNames.MANNEQUIN_SKIN))
            return lookupMannequinIcon(state);
        else if (propertyHandler.contains(PropertyNames.PLAYER_SKIN))
            return lookupPlayerIcon(state);

        return lookupTextIcon(state.getDisguiseIdentifier());
    }

    public Component lookupPlayerIcon(DisguiseState state)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);
        var optional = state.disguisePropertyHandler().getOptional(properties.SKIN);

        return optional.map(skin -> (Component) Component.object(ObjectContents.playerHead(GameProfileUtils.asPlayerProfile(skin))))
                .orElse(IconLookup.instance().fallbackTextIcon());
    }

    public Component lookupMannequinIcon(DisguiseState state)
    {
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(MannequinPropertyCollection.class);
        var optional = state.disguisePropertyHandler().getOptional(properties.SKIN);

        return optional.map(skin -> (Component) Component.object(ObjectContents.playerHead(skin)))
                .orElse(IconLookup.instance().fallbackTextIcon());
    }

    //endregion Dynamic icon lookup

    //endregion Icon lookup API
}
