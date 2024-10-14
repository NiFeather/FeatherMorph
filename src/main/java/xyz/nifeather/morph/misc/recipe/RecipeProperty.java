package xyz.nifeather.morph.misc.recipe;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record RecipeProperty(NamespacedKey key,
                             boolean shaped, List<String> shape,
                             Map<String, Material> materials,
                             @NotNull Material resultMaterial,
                             @Nullable Component resultName,
                             @Nullable List<Component> lore)
{
}
