package xyz.nifeather.morph.utilities;

import net.kyori.adventure.text.Component;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.IMaybeUserFriendlyException;

public class ExceptionUtils
{
    public static FormattableMessage getExceptionMessageShort(Throwable t)
    {
        if (t instanceof IMaybeUserFriendlyException e)
            return e.localizableMessage().orElse(new FormattableMessage(FeatherMorphMain.getInstance(), t.getMessage()));

        return new FormattableMessage(FeatherMorphMain.getInstance(), t.getMessage());
    }

    public static Component getExceptionDetail(Throwable t)
    {
        if (t instanceof IMaybeUserFriendlyException e)
            return Component.text(e.underlyingMessage());
        else if (t.getCause() != null)
            return Component.text(t.getCause().getMessage());
        else
            return Component.text(t.getMessage());
    }
}
