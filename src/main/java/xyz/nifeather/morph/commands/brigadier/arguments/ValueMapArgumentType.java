package xyz.nifeather.morph.commands.brigadier.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
public class ValueMapArgumentType implements CustomArgumentType<@NotNull Map<String, String>, @NotNull String>
{
    public static final Collection<String> EXAMPLES = ObjectArrayList.of("[foo=bar]", "[foo=bar, aabb=\"ccdd\"]");
    private static final Map<String, List<String>> EMPTY_MAP = new HashMap<>();
    private final Map<String, List<String>> properties = new ConcurrentHashMap<>();

    @Unmodifiable
    public Map<String, List<String>> properties()
    {
        return new Object2ObjectOpenHashMap<>(this.properties);
    }

    public void setProperty(String key, @Nullable List<String> values)
    {
        if (values == null)
        {
            this.unsetProperty(key);
            return;
        }

        this.properties.put(key, values);
    }

    public void unsetProperty(String key)
    {
        this.properties.remove(key);
    }

    public ValueMapArgumentType(Map<String, List<String>> properties)
    {
        this.properties.putAll(properties);
    }

    public ValueMapArgumentType()
    {
        this(EMPTY_MAP);
    }

    /**
     *
     * @param properties name <-> values Map
     */
    public static ValueMapArgumentType withArguments(Map<String, List<String>> properties)
    {
        return new ValueMapArgumentType(properties);
    }

    public static Map<String, String> get(String name, CommandContext<CommandSourceStack> context)
    {
        return context.getArgument(name, Map.class);
    }

    private static final Component ERR_NO_BRACKET = Component.translatableWithFallback(
            "morphclient.parsing.bracket.start",
            "Expected square bracket (\"[\") to start a string"
    );

    private static final Component ERR_EMPTY_KEY = Component.translatableWithFallback(
            "morphclient.parsing.key.empty",
            "May not have a empty key"
    );

    private static final Component ERR_SUGGEST_FAIL = Component.translatableWithFallback(
            "morphclient.parsing.fail",
            "Failed to suggest properties!"
    );

    private static SimpleCommandExceptionType errorForKeyNoValue(String key)
    {
        return new SimpleCommandExceptionType(Component.translatableWithFallback(
                "morphclient.parsing.value_map.no_value",
                "Missing value for key '%s'",
                key
        ));
    }

    private static SimpleCommandExceptionType errorForDuplicateKey(String key)
    {
        return new SimpleCommandExceptionType(Component.translatableWithFallback(
                "morphclient.parsing.value_map.duplicate_key",
                "Duplicate key '%s'",
                key
        ));
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        // Starting at /morph xx [abcd=ef]
        //                       ^ HERE
        //
        // What we want the cursor to stop at
        // /morph xx [ab=cd, cd=ef]
        //           ^  ^   ^  ^
        //           THESE PLACE

        var masterReader = new StringReader(builder.getInput());
        masterReader.setCursor(builder.getStart());

        return CompletableFuture.supplyAsync(() -> this.doSuggest(masterReader, builder));
    }

    private Suggestions doSuggest(StringReader masterReader, SuggestionsBuilder defaultBuilder)
    {
        if (masterReader.peek() == '[')
            masterReader.skip();

        KeyValuePair keyValuePair = new KeyValuePair(null, null, masterReader.getCursor(), masterReader.getCursor(), false);

        try
        {
            // 循环到最后一个KVP
            var parseResults = parseAll(masterReader, ',', ']');

            keyValuePair = parseResults.isEmpty() ? keyValuePair : parseResults.getLast();
        }
        catch (Throwable t)
        {
            //log.error("Failed to list suggestions!", t);
            //return defaultBuilder.createOffset(masterReader.getCursor()).suggest("???", ERR_SUGGEST_FAIL).build();
            return defaultBuilder.build();
        }

        // 读取一下输入的最后一个字符
        var peekReader = new StringReader(masterReader);
        peekReader.setCursor(masterReader.canRead() ? masterReader.getCursor() : peekReader.getTotalLength() - 1);

        //log.info("Peek is " + peekReader.peek() + " At " + masterReader.getCursor());

        // 遇到结尾了直接退出
        if (peekReader.peek() == ']')
            return defaultBuilder.build();

        // 最终Builder，该Builder的期望是在上面提到的一些地方停下
        SuggestionsBuilder finalBuilder;

        String keyInput = keyValuePair.key == null ? "" : keyValuePair.key;
        var hasMatchKey = this.properties.keySet().stream().anyMatch(k -> k.equals(keyInput));

        // Suggest Key
        if (!keyValuePair.hasValue)
        {
            finalBuilder = defaultBuilder.createOffset(keyValuePair.keyCursor);

            //FeatherMorphMain.getInstance().getSLF4JLogger().info("Builder offset is " + finalBuilder.getStart());

            if (!hasMatchKey)
            {
                boolean isEmptyInput = keyValuePair.key == null;

                this.properties.keySet().forEach(k ->
                {
                    if (isEmptyInput) finalBuilder.suggest(k);
                    else if (k.contains(keyInput)) finalBuilder.suggest(k);
                });
            }
        }
        else // Suggest Value
        {
            String valueInput = keyValuePair.value == null ? "" : keyValuePair.value;
            boolean isEmptyInput = keyValuePair.value == null;

            finalBuilder = defaultBuilder.createOffset(keyValuePair.valueCursor);

            var availableValues = this.properties.getOrDefault(keyValuePair.key, List.of());
            availableValues.forEach(v ->
            {
                if (isEmptyInput) finalBuilder.suggest(v);
                else if (v.contains(valueInput)) finalBuilder.suggest(v);
            });
        }

        return finalBuilder.build();
    }

    @Override
    public Map<String, String> parse(StringReader reader) throws CommandSyntaxException
    {
        if (reader.peek() != '[')
            throw new SimpleCommandExceptionType(ERR_NO_BRACKET).createWithContext(reader);

        // 跳过方括号
        reader.skip();

        Map<String, String> map = new Object2ObjectOpenHashMap<>();

        var values = this.parseAll(reader, ',', ']');

        // Check last
        var peekReader = new StringReader(reader);
        peekReader.setCursor(reader.canRead() ? reader.getCursor() : reader.getTotalLength() - 1);

        // 读完了但是没有遇到闭合括号！
        if (peekReader.peek() != ']')
            throw new SimpleCommandExceptionType(Component.translatable("parsing.expected", "]")).createWithContext(reader);
        else
            reader.skip();

        for (KeyValuePair pair : values)
        {
            if (pair.key == null)
            {
                reader.setCursor(pair.keyCursor);
                throw new SimpleCommandExceptionType(ERR_EMPTY_KEY).createWithContext(reader);
            }

            if (pair.value == null)
            {
                reader.setCursor(pair.keyCursor);
                throw errorForKeyNoValue(pair.key).createWithContext(reader);
            }

            if (map.containsKey(pair.key))
            {
                reader.setCursor(pair.keyCursor);
                throw errorForDuplicateKey(pair.key).createWithContext(reader);
            }

            map.put(pair.key, pair.value);
        }

        return map;
    }

    public record KeyValuePair(@Nullable String key, @Nullable String value, int keyCursor, int valueCursor, boolean hasValue)
    {
    }

    /**
     * @apiNote 总是会停在 terminator 和 endOfString 上
     */
    public List<KeyValuePair> parseAll(StringReader reader, char splitter, char endOfString) throws CommandSyntaxException
    {
        List<KeyValuePair> list = new ObjectArrayList<>();

        while (reader.canRead())
        {
            KeyValuePair pair = this.parseOnce(reader, splitter, endOfString);

            //FeatherMorphMain.getInstance().getSLF4JLogger().info("[parseAll] Adding " + pair + " :: readercursor is " + reader.getCursor());

            list.add(pair);

            if (pair.key == null)
                break;
        }

        return list;
    }

    /**
     * @return A string, NULL if the input equals 'terminator' or 'endOfString'
     * @throws CommandSyntaxException
     */
    // Possible end at:
    // [a=b]  |||  [a=b,c=d]  ||| [a=b
    //     ^           ^             ^
    @NotNull
    public KeyValuePair parseOnce(StringReader reader, char terminator, char endOfString) throws CommandSyntaxException
    {
        //log.info("Starting read... Peek is '%s'".formatted(reader.peek()));
        StringBuilder keyStringBuilder = new StringBuilder();
        @Nullable StringBuilder valueStringBuilder = null;

        int keyCursor = reader.getCursor();
        int valueCursor = reader.getCursor();
        boolean isKey = true;

        String key = null;
        String value = null;

        char last;

        while (reader.canRead())
        {
            last = reader.peek(0);
            char current = reader.read();

            //log.info("[parseOnce] Next is " + next);

            // 如果遇到了闭合括号，break;
            if (current == endOfString)
                break;

            // 遇到了结束符
            if (current == terminator)
                break;

            // Stop at first char that is not space
            if (isKey && last == ' ')
                keyCursor = reader.getCursor();

            //FeatherMorphMain.getInstance().getSLF4JLogger().info("Current: '%s'".formatted(next));

            //region 识别Key

            var builder = isKey ? keyStringBuilder : valueStringBuilder;

            // 遇到等于号，切换至Value模式
            if (current == '=' && isKey)
            {
                isKey = false;

                // 判断我们值的cursor要从哪里开始
                if (reader.canRead())
                {
                    // 如果等于号后面是引号，那么从那里开始，否则就从这里开始
                    if (StringReader.isQuotedStringStart(reader.peek()))
                        valueCursor = reader.getCursor() + 1;
                    else
                        valueCursor = reader.getCursor();
                }
                else // 以等于号结尾，从这里开始
                {
                    valueCursor = reader.getCursor();
                }

                continue;
            }

            if (!isKey && builder == null)
                builder = valueStringBuilder = new StringBuilder();

            //endregion 识别Key

            // 忽略开头的空格
            if (builder.isEmpty() && Character.isWhitespace(current))
                continue;

            // 遇到引号了，读取到下个引号结束
            if (StringReader.isQuotedStringStart(current))
            {
                var quoteReader = new StringReader(reader);
                String str;

                str = quoteReader.readStringUntil(current);
                reader.setCursor(quoteReader.getCursor());

                //log.info("APPENDING QUOTE STRING [%s]".formatted(str));
                builder.append(str);
                continue;
            }

            // 其他情况
            //log.info("APPENDING [%s]".formatted(current));
            builder.append(current);
        }

        if (!keyStringBuilder.isEmpty())
            key = keyStringBuilder.toString();

        if (valueStringBuilder != null)
            value = valueStringBuilder.toString();

        //log.info("DONE! result is [%s]".formatted(builder.toString()));
        return new KeyValuePair(key, value, keyCursor, valueCursor, !isKey);
    }

    @Override
    @NotNull
    public ArgumentType<String> getNativeType()
    {
        return StringArgumentType.greedyString();
    }

    @Override
    @NotNull
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
