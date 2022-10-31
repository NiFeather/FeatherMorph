package xiamomc.morph.misc.integrations.placeholderapi;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import xiamomc.morph.misc.integrations.placeholderapi.builtin.AvaliableDisguisesProvider;
import xiamomc.morph.misc.integrations.placeholderapi.builtin.StateNameProvider;
import xiamomc.pluginbase.Managers.DependencyManager;

import java.util.List;

public class PlaceholderIntegration extends PlaceholderExpansion
{
    public PlaceholderIntegration(DependencyManager depManager)
    {
        this.depManager = depManager;

        providers.addAll(ObjectArrayList.of(
                new StateNameProvider(),
                new AvaliableDisguisesProvider()
        ));
    }

    private final DependencyManager depManager;

    @Override
    public @NotNull String getIdentifier()
    {
        return "morph";
    }

    @Override
    public @NotNull String getAuthor()
    {
        return "MATRIX-feather";
    }

    @Override
    public @NotNull String getVersion()
    {
        return "0.4.1";
    }

    private static final List<IPlaceholderProvider> providers = new ObjectArrayList<>();

    /**
     * 添加一个Placeholder提供器
     * @param provider Placeholder提供器
     * @return 操作是否成功（是否已经注册过一个相同ID和匹配模式的提供器）
     */
    public static boolean addPlaceholderProvider(IPlaceholderProvider provider)
    {
        if (providers.stream().anyMatch(p -> providerEquals(p, provider)))
            return false;

        LoggerFactory.getLogger("morph").info("注册Placeholder提供器: " + provider.getPlaceholderIdentifier());
        providers.add(0, provider);
        return true;
    }

    private static boolean providerEquals(IPlaceholderProvider source, IPlaceholderProvider target)
    {
        if (source == null || target == null) return false;

        return source.getMatchMode() == target.getMatchMode()
                && source.getPlaceholderIdentifier().equals(target.getPlaceholderIdentifier());
    }

    private final String defaultString = "???";

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params)
    {
        if (player == null) return defaultString;

        var provider = providers.stream()
                .filter(p -> p.getMatchMode() == MatchMode.Exact
                            ? params.equalsIgnoreCase(p.getPlaceholderIdentifier())
                            : params.startsWith(p.getPlaceholderIdentifier()))
                .findFirst().orElse(null);

        if (provider == null) return defaultString;

        var val = provider.resolvePlaceholder(player, params);
        return val == null ? defaultString : val;
    }
}
