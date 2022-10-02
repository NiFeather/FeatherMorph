package xiamomc.morph.commands.subcommands.plugin.helpsections;

import java.util.ArrayList;
import java.util.List;

public class Section
{
    private final List<Entry> entries = new ArrayList<>();
    public List<Entry> getEntries()
    {
        return entries;
    }

    private final String title;
    public String getTitle(){ return title; }

    public Section(String title)
    {
        this.title = title;
    }

    public void add(Entry e)
    {
        entries.add(e);
    }
}
