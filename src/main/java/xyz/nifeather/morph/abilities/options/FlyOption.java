package xyz.nifeather.morph.abilities.options;

import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public class FlyOption implements ISkillAbilityOption
{
    public static class FlyOptionHandler implements ISkillAbilityOptionHandler<FlyOption>
    {
        @Override
        public Class<FlyOption> getOptionClass()
        {
            return FlyOption.class;
        }

        @Override
        public void writeOption(FlyOption option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("fly_speed", option.getFlyingSpeed());
            gsonMap.put("minimum_hunger", option.getMinimumHunger());
        }

        @Override
        public @NotNull FlyOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            float flySpeed = utilGetTypedOrThrow("fly_speed", gsonMap, Number.class).floatValue();
            int minimumHunger = utilGetTypedOrThrow("minimum_hunger", gsonMap, Number.class).intValue();

            var clazzSimpleName = this.getClass().getSimpleName();
            if (!Float.isFinite(flySpeed))
                throw new ParseErrorException(clazzSimpleName, "Non-Finite fly speed");

            return new FlyOption(flySpeed, minimumHunger);
        }
    }

    public static final FlyOptionHandler OPTION_HANDLER = new FlyOptionHandler();

    public FlyOption(float speed)
    {
        this(speed, 6);
    }

    public FlyOption(float speed, int minimumHunger)
    {
        this.flyingSpeed = speed;
        this.minimumHunger = minimumHunger;
    }

    private final float flyingSpeed;

    public float getFlyingSpeed()
    {
        return flyingSpeed;
    }

    private final int minimumHunger;

    public int getMinimumHunger()
    {
        return minimumHunger;
    }

    @Override
    public boolean isValid()
    {
        return !Float.isNaN(flyingSpeed);
    }
}
