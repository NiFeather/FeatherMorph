package xyz.nifeather.morph.commands.brigadier.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

public class RelaxedStringArgumentType implements CustomArgumentType<String, String>
{
    public static RelaxedStringArgumentType relaxed()
    {
        return new RelaxedStringArgumentType();
    }

    /**
     * Parses the argument into the custom type ({@code T}). Keep in mind
     * that this parsing will be done on the server. This means that if
     * you throw a {@link CommandSyntaxException} during parsing, this
     * will only show up to the user after the user has executed the command
     * not while they are still entering it.
     *
     * @param reader string reader input
     * @return parsed value
     * @throws CommandSyntaxException if an error occurs while parsing
     * @see #parse(StringReader, Object)
     */
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException
    {
        int begin = reader.getCursor();

        if (!reader.canRead())
            reader.skip();

        while (reader.canRead() && !Character.isWhitespace(reader.peek()))
            reader.skip();

        return reader.getString().substring(begin, reader.getCursor());
    }

    /**
     * Gets the native type that this argument uses,
     * the type that is sent to the client.
     *
     * @return native argument type
     */
    @Override
    public ArgumentType<String> getNativeType()
    {
        return StringArgumentType.greedyString();
    }
}
