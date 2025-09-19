package xyz.nifeather.morph;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;

public class BuiltinDatapackNames
{
    private static final List<String> datapacks = Collections.synchronizedList(new ObjectArrayList<>());
    private static String add(String id)
    {
        datapacks.add(id);
        return id;
    }

    public static final String RECIPES = add("recipes");
    public static final String DISGUISE_TAGS = add("disguise_tags");
    public static final String LOOT_TABLES = add("loot_tables");

    public static List<String> values()
    {
        return List.copyOf(datapacks);
    }
}
