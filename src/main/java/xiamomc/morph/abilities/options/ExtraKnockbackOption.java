package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.world.phys.Vec3;
import xiamomc.morph.storage.skill.ISkillOption;

public class ExtraKnockbackOption implements ISkillOption
{
    public ExtraKnockbackOption()
    {
    }

    public ExtraKnockbackOption(double x, double y, double z)
    {
        this.xMotion = x;
        this.yMotion = y;
        this.zMotion = z;
    }

    public static ExtraKnockbackOption from(double x, double y, double z)
    {
        return new ExtraKnockbackOption(x, y, z);
    }

    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return Double.isFinite(xMotion) && Double.isFinite(yMotion) && Double.isFinite(zMotion);
    }

    public Vec3 toVec3()
    {
        return new Vec3(xMotion, yMotion, zMotion);
    }

    @Expose
    @SerializedName("motion_x")
    public double xMotion = 0D;

    @Expose
    @SerializedName("motion_y")
    public double yMotion = 0D;

    @Expose
    @SerializedName("motion_z")
    public double zMotion = 0D;
}
