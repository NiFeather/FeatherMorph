package xyz.nifeather.morph.misc.disguiseProperty;

import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.Optional;

public class ParseErrorException extends Exception
{
    public final String propertyName;
    public final Optional<FormattableMessage> localizableMessage;
    public final String methodName;

    public ParseErrorException(String propertyName, String msg)
    {
        super(msg);
        this.propertyName = propertyName;
        localizableMessage = Optional.empty();
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
        this.localizableMessage = Optional.ofNullable(localizableMessage);
        this.methodName = methodName == null ? "" : methodName;
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
