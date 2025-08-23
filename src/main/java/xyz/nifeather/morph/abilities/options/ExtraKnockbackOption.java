package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class ExtraKnockbackOption implements ISkillAbilityOption
{
    public static class ExtraKnockbackOptionHandler implements ISkillAbilityOptionHandler<ExtraKnockbackOption>
    {
        @Override
        public Class<ExtraKnockbackOption> getOptionClass()
        {
            return ExtraKnockbackOption.class;
        }

        @Override
        public void writeOption(ExtraKnockbackOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("motion_x", option.xMotion);
            gsonMap.put("motion_y", option.yMotion);
            gsonMap.put("motion_z", option.zMotion);
        }

        @Override
        public @NotNull ExtraKnockbackOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            double xMotion = utilGetTypedOrThrow("motion_x", gsonMap, Number.class).doubleValue();
            double yMotion = utilGetTypedOrThrow("motion_y", gsonMap, Number.class).doubleValue();
            double zMotion = utilGetTypedOrThrow("motion_z", gsonMap, Number.class).doubleValue();

            return new ExtraKnockbackOption(xMotion, yMotion, zMotion);
        }
    }

    public static final ExtraKnockbackOptionHandler OPTION_HANDLER = new ExtraKnockbackOptionHandler();

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

    public final double xMotion;
    public final double yMotion;
    public final double zMotion;
}
