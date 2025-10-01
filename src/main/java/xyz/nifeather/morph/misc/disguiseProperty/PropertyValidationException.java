package xyz.nifeather.morph.misc.disguiseProperty;

import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.misc.IMaybeUserFriendlyException;

import java.util.Optional;

public class PropertyValidationException extends Exception implements IMaybeUserFriendlyException
{
    public final String propertyName;
    public final String methodName;

    @Nullable
    private final FormattableMessage localizableMessage;

    public PropertyValidationException(String propertyName, String msg)
    {
        super(msg);
        this.propertyName = propertyName;
        localizableMessage = null;
        methodName = "";
    }

    public PropertyValidationException(String propertyName, String msg, Throwable cause)
    {
        this(propertyName, msg, null, cause, null);
    }

    public PropertyValidationException(String propertyName, String msg, @Nullable String methodName,
                                       Throwable cause, @Nullable FormattableMessage localizableMessage)
    {
        super(msg, cause);
        this.propertyName = propertyName;
        this.localizableMessage = localizableMessage;
        this.methodName = methodName == null ? "" : methodName;
    }

    @Override
    public String underlyingMessage()
    {
        var cause = getCause();
        return cause == null ? this.getMessage() : "%s (Caused by %s: %s)".formatted(this.getMessage(), cause.getClass().getSimpleName(), cause.getMessage());
    }

    @Override
    public Optional<FormattableMessage> localizableMessage()
    {
        if (getCause() instanceof IMaybeUserFriendlyException userFriendlyException)
            return userFriendlyException.localizableMessage();
        else
            return Optional.ofNullable(localizableMessage);
    }

    public static ParseErrorGenerator forProperty(String property)
    {
        return new ParseErrorGenerator(property);
    }

    public static class ParseErrorGenerator
    {
        public final String propertyName;
        public String exceptionMessage = "unknown error (reason not given)";
        public String methodName = "";

        @Nullable
        public Throwable cause;

        @Nullable
        public FormattableMessage localizableMessage;

        public ParseErrorGenerator(String propertyName)
        {
            this.propertyName = propertyName;
        }

        public ParseErrorGenerator byMethod(String methodName)
        {
            this.methodName = methodName;
            return this;
        }

        public ParseErrorGenerator withMessage(String msg)
        {
            this.exceptionMessage = msg;
            return this;
        }

        public ParseErrorGenerator causedBy(Throwable throwable)
        {
            this.cause = throwable;
            return this;
        }

        public ParseErrorGenerator withLocalizableMessage(FormattableMessage formattableMessage)
        {
            this.localizableMessage = formattableMessage;
            return this;
        }

        public PropertyValidationException create()
        {
            var finalExceptionMessage = methodName.isBlank()
                    ? exceptionMessage
                    : "%s: %s".formatted(methodName, exceptionMessage);

            return new PropertyValidationException(propertyName, finalExceptionMessage, methodName, cause, localizableMessage);
        }
    }
}
