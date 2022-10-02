package xiamomc.morph.commands.subcommands.plugin.helpsections;

public record Entry(String permission, String message)
{
    @Override
    public String toString()
    {
        return message;
    }
}
