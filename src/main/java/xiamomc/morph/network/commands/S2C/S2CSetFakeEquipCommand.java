package xiamomc.morph.network.commands.S2C;

public class S2CSetFakeEquipCommand extends S2CSetCommand<Boolean>
{
    public S2CSetFakeEquipCommand(boolean val)
    {
        super(val);
    }

    @Override
    public String getBaseName()
    {
        return "fake_equip";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, false);
    }
}
