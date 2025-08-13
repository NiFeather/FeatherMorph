package xyz.nifeather.morph.commands.subcommands.plugin.helpsections;

import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Section
{
    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public List<Entry> getEntries()
    {
        return entries;
    }

    private final FormattableMessage description;

    public FormattableMessage getDescription()
    {
        return description;
    }

    private final List<FormattableMessage> notes;

    @Nullable
    public List<FormattableMessage> getNotes()
    {
        return notes;
    }

    private final String commandBaseName;
    public String getCommandBaseName() { return commandBaseName; }

    public Section(String commandBaseName, FormattableMessage description, @Nullable List<FormattableMessage> notes)
    {
        this.commandBaseName = commandBaseName;
        this.description = description;
        this.notes = notes;
    }

    public void add(Entry e)
    {
        entries.add(e);
    }
}
