package xiamomc.morph.network.commands.S2C;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.utilities.NbtUtils;

public class S2CSetNbtCommand extends S2CSetCommand<CompoundTag>
{
    public S2CSetNbtCommand(CompoundTag tag)
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
        var nbt = this.getArgumentAt(0, new CompoundTag());
        return super.buildCommand() + " " + NbtUtils.getCompoundString(nbt);
    }
}
