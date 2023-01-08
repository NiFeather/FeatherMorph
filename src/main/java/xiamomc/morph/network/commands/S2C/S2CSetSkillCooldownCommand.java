package xiamomc.morph.network.commands.S2C;

public class S2CSetSkillCooldownCommand extends S2CSetCommand<Long>
{
    public S2CSetSkillCooldownCommand(long value)
    {
        super(value);
    }

    @Override
    public String getBaseName()
    {
        return "cd";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + getArgumentAt(0, 0L);
    }
}
