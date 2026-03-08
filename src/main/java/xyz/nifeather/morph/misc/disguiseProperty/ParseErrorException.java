package xyz.nifeather.morph.misc.disguiseProperty;

import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.misc.IMaybeUserFriendlyException;
import xyz.nifeather.morph.misc.IUserFault;

import java.util.Optional;

public class ParseErrorException extends Exception implements IMaybeUserFriendlyException, IUserFault
{
    public final String propertyName;
    public final String methodName;

    @Nullable
    private final FormattableMessage localizableMessage;

    public ParseErrorException(String propertyName, String msg)
    {
        super(msg);
        this.propertyName = propertyName;
        localizableMessage = null;
        methodName = "";
    }

    public ParseErrorException(String propertyName, String msg, Throwable cause)
    {
        this(propertyName, msg, null, cause, null);
    }

    public ParseErrorException(String propertyName, String msg, @Nullable String methodName,
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
        return cause == null ? this.getMessage() : "%s \n\nCaused by %s: %s".formatted(this.getMessage(), cause.getClass().getSimpleName(), cause.getMessage());
    }

    @Override
    public Optional<FormattableMessage> localizableMessage()
    {
        if (getCause() instanceof IMaybeUserFriendlyException userFriendlyException)
        {
            var theirMessage = userFriendlyException.localizableMessage();
            return theirMessage.isEmpty() ? Optional.ofNullable(localizableMessage) : theirMessage;
        }
        else
        {
            return Optional.ofNullable(localizableMessage);
        }
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

        public ParseErrorException create()
        {
            var finalExceptionMessage = methodName.isBlank()
                    ? exceptionMessage
                    : "%s: %s".formatted(methodName, exceptionMessage);

            return new ParseErrorException(propertyName, finalExceptionMessage, methodName, cause, localizableMessage);
        }
    }
}
