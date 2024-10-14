package xyz.nifeather.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.storage.skill.ISkillOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BossbarOption implements ISkillOption
{
    public BossbarOption()
    {
    }

    public BossbarOption(BossbarCreateOption option, int distance)
    {
        this.createOption = option;
        this.applyDistance = distance;
    }

    private int applyDistance;
    public int getApplyDistance()
    {
        return applyDistance;
    }

    private BossbarCreateOption createOption;

    public BossbarCreateOption getCreateOption()
    {
        return createOption;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        if (createOption != null)
        {
            map.put("color", createOption.color.name().toLowerCase());
            map.put("style", createOption.overlay().name().toLowerCase());
            map.put("name", createOption.name());

            List<String> flags = new ObjectArrayList<>();
            createOption.flags().forEach(f -> flags.add(f.name().toLowerCase()));
            map.put("flags", flags);
        }

        map.put("distance", applyDistance);

        return map;
    }

    @Override
    public @Nullable ISkillOption fromMap(@Nullable Map<String, Object> map)
    {
        if (map == null) return null;

        //flag
        var flags = new ObjectArraySet<BossBar.Flag>();
        var rawFlags = tryGet(map, "flags", List.class);

        if (rawFlags != null) rawFlags.forEach(o ->
        {
            if (!(o instanceof String str)) return;

            var matchingFlag = BossBar.Flag.NAMES.value(str);
            if (matchingFlag != null) flags.add(matchingFlag);
        });

        var color = tryGet(map, "color", BossBar.Color.WHITE, o -> BossBar.Color.NAMES.value("" + o));
        var style = tryGet(map, "style", BossBar.Overlay.PROGRESS, o -> BossBar.Overlay.NAMES.value("" + o));
        var title = "" + map.getOrDefault("name", "<name>");
        int distance = tryGetInt(map, "distance", -1);

        return new BossbarOption(new BossbarCreateOption(title, color, style, flags), distance);
    }

    public record BossbarCreateOption(String name, BossBar.Color color, BossBar.Overlay overlay, Set<BossBar.Flag> flags)
    {
    }
}
