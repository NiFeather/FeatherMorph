package xyz.nifeather.morph.misc;

import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.Optional;

public class ExecutionErrorException extends Exception
{
    public final Optional<FormattableMessage> localizableMessage;
    public final String methodName;

    public ExecutionErrorException(String methodName, String msg)
    {
        this(methodName, msg, null);
    }

    public ExecutionErrorException(String methodName, String msg, Throwable cause)
    {
        this(methodName, msg, cause, null);
    }

    public ExecutionErrorException(String methodName, String msg,
                               Throwable cause, @Nullable FormattableMessage localizableMessage)
    {
        super(msg, cause);
        this.localizableMessage = Optional.ofNullable(localizableMessage);
        this.methodName = methodName == null ? "" : methodName;
    }


    public static ExecutionErrorGenerator forMethod(String method)
    {
        return new ExecutionErrorGenerator(method);
    }

    public static class ExecutionErrorGenerator
    {
        public String exceptionMessage = "unknown error (reason not given)";
        public String methodName = "";

        @Nullable
        public Throwable cause;

        @Nullable
        public FormattableMessage localizableMessage;

        public ExecutionErrorGenerator(String methodName)
        {
            this.methodName = methodName;
        }

        public ExecutionErrorGenerator withMessage(String msg)
        {
            this.exceptionMessage = msg;
            return this;
        }

        public ExecutionErrorGenerator causedBy(Throwable throwable)
        {
            this.cause = throwable;
            return this;
        }

        public ExecutionErrorGenerator withLocalizableMessage(FormattableMessage formattableMessage)
        {
            this.localizableMessage = formattableMessage;
            return this;
        }

        public ExecutionErrorException create()
        {
            var finalExceptionMessage = methodName.isBlank()
                    ? exceptionMessage
                    : "%s: %s".formatted(methodName, exceptionMessage);

            return new ExecutionErrorException(methodName, finalExceptionMessage, cause, localizableMessage);
        }
    }
}
