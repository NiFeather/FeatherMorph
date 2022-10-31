package xiamomc.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Boss;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import xiamomc.morph.storage.skill.ISkillOption;

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

        //color
        var colorName = (String) map.get("color");

        var color = BossBar.Color.NAMES.value(colorName == null ? "" : colorName);
        if (color == null) color = BossBar.Color.WHITE;

        //flag
        var flags = new ObjectArraySet<BossBar.Flag>();
        var rawFlags = (List<?>) map.get("flags");

        if (rawFlags != null)
        {
            rawFlags.forEach(o ->
            {
                if (!(o instanceof String str)) return;

                var matchingFlag = BossBar.Flag.NAMES.value(str);
                if (matchingFlag != null) flags.add(matchingFlag);
            });
        }

        //type
        var rawOverlay = (String) map.get("style");
        var overlay = BossBar.Overlay.NAMES.value(rawOverlay == null ? "progress" : rawOverlay);
        overlay = overlay == null ? BossBar.Overlay.PROGRESS : overlay;

        //title
        var title = (String) map.get("name");
        title = title == null ? "<name>" : title;

        //distance
        int distance;
        var rawDistance = "" + map.get("distance");

        try
        {
            if (rawDistance.equalsIgnoreCase("null")) distance = -1;
            else distance = ((Double) Double.parseDouble(rawDistance)).intValue();
        }
        catch (Throwable t)
        {
            distance = -1;
        }

        return new BossbarOption(new BossbarCreateOption(title, color, overlay, flags), distance);
    }

    public record BossbarCreateOption(String name, BossBar.Color color, BossBar.Overlay overlay, Set<BossBar.Flag> flags)
    {
    }
}
