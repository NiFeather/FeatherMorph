package xyz.nifeather.morph.misc.disguiseProperty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.papermc.paper.math.Rotations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Keyed;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutputHandles
{
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static <X> String immediateException(String propertyName, X val) throws ParseErrorException
    {
        throw ParseErrorException.forProperty(propertyName)
                .withMessage("This property is not configured, or doesn't support output handles")
                .create();
    }

    public static String writeBoolean(String propertyName, Boolean val)
    {
        return val.toString().toLowerCase();
    }

    public static String writeFloat(String propertyName, Float fl)
    {
        return Float.toString(fl);
    }

    public static String writeDouble(String propertyName, Double d)
    {
        return Double.toString(d);
    }

    public static String writeInteger(String propertyName, Integer i)
    {
        return Integer.toString(i);
    }

    public static String writeRotations(String propertyName, Rotations rotations)
    {
        List<Double> array = new ArrayList<>();
        array.add(rotations.x());
        array.add(rotations.y());
        array.add(rotations.z());

        return gson.toJson(array);
    }

    public static <E extends Enum<E>> String writeEnum(String propertyName, Enum<E> eEnum)
    {
        return eEnum.name().toLowerCase();
    }

    public static String writeAdventureComponentJSON(String propertyName, Component component)
    {
        return JSONComponentSerializer.json().serialize(component);
    }

    public static String writeUUID(String propertyName, UUID uuid)
    {
        return uuid.toString();
    }

    public static String writeKeyed(String propertyName, Keyed keyed)
    {
        return keyed.key().asString();
    }
}
