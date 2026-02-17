package xyz.nifeather.morph.storage.offlinestore;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SavedDisguise
{
    /**
     * 伪装ID<br>
     * 会在伪装数据不可用、构建失败或恢复伪装时使用
     */
    @Expose
    @Nullable
    public String disguiseIdentifier;

    /**
     * 伪装数据，如果存在则优先使用这里的数据
     */
    @Expose
    public final Map<String, String> properties = new ConcurrentHashMap<>();

    public boolean isValid()
    {
        return disguiseIdentifier != null;
    }

    public static SavedDisguise fromState(DisguiseState state)
    {
        var instance = new SavedDisguise();

        instance.disguiseIdentifier = state.getDisguiseIdentifier();
        try
        {
            instance.properties.putAll(state.disguisePropertyHandler().toNetworkProperties());
        }
        catch (ParseErrorException | ExecutionErrorException e)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger()
                    .error("Failed to save properties for disguise", e);
        }

        return instance;
    }
}
