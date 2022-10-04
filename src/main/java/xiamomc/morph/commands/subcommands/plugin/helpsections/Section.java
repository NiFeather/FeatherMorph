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

    public String getHeader()
    {
        return header;
    }

    private final List<String> footer;

    @Nullable
    public List<String> getFooter()
    {
        return footer;
    }

    private final String commandBaseName;
    public String getCommandBaseName() { return commandBaseName; }

    public Section(String name, String header, @Nullable List<String> footer)
    {
        this.commandBaseName = name;
        this.header = header;
        this.footer = footer;
    }

    public Section(String name, String header)
    {
        this(name, header, null);
    }

    public void add(Entry e)
    {
        entries.add(e);
    }
}
