package xiamomc.morph.commands.subcommands.plugin.helpsections;

public record Entry(String permission, String baseName, String description, String suggestingCommand)
{
    @Override
    public String toString()
    {
        return baseName + "的Entry";
    }
}
