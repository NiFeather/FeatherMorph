package xiamomc.morph.network.commands.S2C;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.misc.NbtUtils;

public class S2CSetSNbtCommand extends S2CSetCommand<String>
{
    public S2CSetSNbtCommand(String tag)
    {
        super(tag);
    }

    @Override
    public String getBaseName()
    {
        return "nbt";
    }

    @Override
    public String buildCommand()
    {
        return super.buildCommand() + " " + this.getArgumentAt(0, "{}");
    }
}
