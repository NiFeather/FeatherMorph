package xyz.nifeather.morph.abilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Map;

public interface ISkillAbilityOptionHandler<O extends ISkillAbilityOption>
{
    /**
     * Store the given option to the given option map
     */
    void writeOption(O option, @NotNull Map<String, Object> gsonMap);

    /**
     * Get the class of the option that this handler would produce
     */
    Class<O> getOptionClass();

    /**
     * Whether this handler accepts null option map input (The incoming 'gsonMap' is a Null value).<br>
     * If TRUE, {@link ISkillAbilityOptionHandler#readOptionNullable(Map)} will be called, and this handler would need to implement it, or an {@link UnsupportedOperationException} will be thrown once a null map in given
     */
    default boolean acceptNullableOptions()
    {
        return false;
    }

    /**
     * In flavor of {@link ISkillAbilityOptionHandler#readOption(Map)}, but a Null option map may be given.<br>
     * Will be called instead of {@link ISkillAbilityOptionHandler#readOption(Map)} if {@link ISkillAbilityOptionHandler#acceptNullableOptions()} returns TRUE
     * @throws NullPointerException This handler doesn't accept null option map, but a null value is given
     * @throws UnsupportedOperationException If this handler doesn't implement this method
     * @throws ParseErrorException If anything goes wrong
     */
    default O readOptionNullable(@Nullable Map<String, Object> gsonMap) throws NullPointerException, UnsupportedOperationException, ParseErrorException
    {
        if (gsonMap == null)
        {
            if (acceptNullableOptions())
                throw new UnsupportedOperationException("Method not implemented");
            else
                throw new NullPointerException(this.getClass().getSimpleName() + " Requires a non-null gsonMap, but got Null!");
        }
        else
        {
            return readOption(gsonMap);
        }
    }

    /**
     * Read option from the given map
     * @throws ParseErrorException If any goes wrong, abort with this exception
     * @implNote It's recommended to wrap exceptions inside a {@link ParseErrorException} using {@link ParseErrorException#ParseErrorException(String, String, Throwable)}
     */
    @NotNull
    O readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException;

    default <T> T utilGetTypedOrThrow(String key, Map<String, Object> gsonMap, Class<T> expectedType) throws ParseErrorException
    {
        var val = gsonMap.getOrDefault(key, null);
        if (val == null)
            throw new ParseErrorException(this.getClass().getSimpleName(), "Key '%s' not present in the option map".formatted(key));

        if (expectedType.isInstance(val))
            return (T) val;
        else
            throw new ParseErrorException(this.getClass().getSimpleName(), "Input key '%s' is not a instance of expected type '%s'".formatted(key, expectedType));
    }
}
