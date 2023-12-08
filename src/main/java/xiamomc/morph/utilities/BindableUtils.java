package xiamomc.morph.utilities;

import xiamomc.pluginbase.Bindables.BindableList;

public class BindableUtils
{
    public static String bindableListToString(BindableList<?> bindableList)
    {
        var builder = new StringBuilder();
        var it = bindableList.listIterator();

        while (it.hasNext())
        {
            var obj = it.next();
            builder.append(obj);

            if (it.hasNext()) builder.append(", ");
        }

        return "[%s]".formatted(builder);
    }
}
