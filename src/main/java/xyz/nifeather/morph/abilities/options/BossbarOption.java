package xyz.nifeather.morph.abilities.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Boss;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BossbarOption implements ISkillAbilityOption
{
    public static class BossbarOptionHandler implements ISkillAbilityOptionHandler<BossbarOption>
    {
        @Override
        public Class<BossbarOption> getOptionClass()
        {
            return BossbarOption.class;
        }

        @Override
        public void writeOption(BossbarOption option, @NotNull Map<String, Object> gsonMap)
        {
            var createOption = option.getCreateOption();

            if (createOption != null)
            {
                gsonMap.put("color", createOption.color.name().toLowerCase());
                gsonMap.put("style", createOption.overlay().name().toLowerCase());
                gsonMap.put("name", createOption.name());

                List<String> flags = new ObjectArrayList<>();
                createOption.flags().forEach(f -> flags.add(f.name().toLowerCase()));
                gsonMap.put("flags", flags);
            }

            gsonMap.put("distance", option.applyDistance);
        }

        @Override
        public @NotNull BossbarOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            var colorString = utilGetTypedOrThrow("color", gsonMap, String.class);
            var styleString = utilGetTypedOrThrow("style", gsonMap, String.class);
            var name = utilGetTypedOrThrow("name", gsonMap, String.class);

            int distance = utilGetTypedOrThrow("distance", gsonMap, Number.class).intValue();

            var color = InputHandles.readEnumNonNull(BossBar.Color.values(), "color", colorString)
                    .orElseThrow(() -> new ParseErrorException("color", "No value match for bossbar color '%s'".formatted(colorString)));

            var style = InputHandles.readEnumNonNull(BossBar.Overlay.values(), "style", styleString)
                    .orElseThrow(() -> new ParseErrorException("style", "No value match for bossbar style '%s'".formatted(styleString)));

            List<String> rawFlagList = utilGetTypedOrThrow("flags", gsonMap, List.class)
                    .stream()
                    .map(Object::toString)
                    .toList();

            String clazzSimpleName = this.getClass().getSimpleName();
            Set<BossBar.Flag> flags = new ObjectArraySet<>();
            for (String input : rawFlagList)
            {
                var flag = InputHandles.readEnumNonNull(BossBar.Flag.values(), clazzSimpleName, input)
                        .orElseThrow(() -> new ParseErrorException(clazzSimpleName, "No matching bossbar flag for input '%s'".formatted(input)));

                flags.add(flag);
            }

            var createOption = new BossbarCreateOption(name, color, style, flags);
            return new BossbarOption(createOption, distance);
        }
    }

    public static final BossbarOptionHandler OPTION_HANDLER = new BossbarOptionHandler();

    public BossbarOption(BossbarCreateOption option, int distance)
    {
        this.createOption = option;
        this.applyDistance = distance;
    }

    private final int applyDistance;
    public int getApplyDistance()
    {
        return applyDistance;
    }

    private final BossbarCreateOption createOption;

    public BossbarCreateOption getCreateOption()
    {
        return createOption;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    public record BossbarCreateOption(String name,
                                      BossBar.Color color,
                                      BossBar.Overlay overlay,
                                      Set<BossBar.Flag> flags)
    {
    }
}
