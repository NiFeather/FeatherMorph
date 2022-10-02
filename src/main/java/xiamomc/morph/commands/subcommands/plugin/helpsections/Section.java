package xiamomc.morph.commands.subcommands.plugin.helpsections;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Section
{
    private final List<Entry> entries = new ArrayList<>();
    public List<Entry> getEntries()
    {
        return entries;
    }

    private final String header;
    public String getHeader(){ return header; }

    private final String footer;
    public String getFooter(){ return footer; }

    public Section(String header, @Nullable String footer)
    {
        this.header = header;
        this.footer = footer;
    }

    public Section(String header)
    {
        this(header, null);
    }

    public void add(Entry e)
    {
        entries.add(e);
    }
}
