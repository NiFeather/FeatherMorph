package xiamomc.morph.storage.offlinestore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.interfaces.IManageOfflineStates;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.pluginbase.Annotations.Initializer;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class OfflineStorageManager extends MorphPluginObject implements IManageOfflineStates
{
    private File configurationFile;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private OfflineStates states = new OfflineStates();

    @Initializer
    private void load(MorphPlugin plugin) throws IOException
    {
        //初始化配置文件
        if (configurationFile == null)
            configurationFile = new File(URI.create(plugin.getDataFolder().toURI() + "/offline_store.json"));

        if (!configurationFile.exists())
        {
            //创建父目录
            if (!configurationFile.getParentFile().exists())
                Files.createDirectories(Paths.get(configurationFile.getParentFile().toURI()));

            if (!configurationFile.createNewFile())
            {
                Logger.error("未能创建文件，将不会加载离线存储！");
                return;
            }
        }

        reloadConfiguration();
    }

    public void reloadConfiguration()
    {
        //加载JSON配置
        OfflineStates storedStages = null;
        var success = false;

        //从文件读取并反序列化为配置
        try (var jsonStream = new InputStreamReader(new FileInputStream(configurationFile)))
        {
            storedStages = gson.fromJson(jsonStream, OfflineStates.class);
            success = true;
        }
        catch (IOException e)
        {
            Logger.warn("无法加载JSON配置：" + e.getMessage());
            e.printStackTrace();
        }

        //确保targetConfiguration不是null
        if (storedStages == null) storedStages = new OfflineStates();

        //设置并保存
        this.states = storedStages;

        saveConfiguration();
    }

    public void saveConfiguration()
    {
        try
        {
            var jsonString = gson.toJson(states);

            if (configurationFile.exists()) configurationFile.delete();

            if (!configurationFile.createNewFile())
            {
                Logger.error("未能创建文件，将不会保存离线存储！");
                return;
            }

            try (var stream = new FileOutputStream(configurationFile))
            {
                stream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将一个玩家的DisguiseState推到存储里
     * @param state DisguiseState
     */
    public void pushDisguiseState(DisguiseState state)
    {
        states.disguiseStates.add(state.toOfflineState());
    }

    /**
     * 获取所有可用的离线伪装存储
     * @return 存储列表
     */
    public List<OfflineDisguiseState> getAvaliableDisguiseStates()
    {
        return states.disguiseStates;
    }

    /**
     * 从存储里取出离线State并从池里移除此State
     * @param uuid 玩家UUID
     * @return 离线State
     */
    @Nullable
    public OfflineDisguiseState popDisguiseState(UUID uuid)
    {
        var targetState = states.disguiseStates.stream().filter(s -> s.playerUUID.equals(uuid)).findFirst();

        if (targetState.isPresent())
        {
            var state = targetState.get();
            states.disguiseStates.remove(state);

            if (state.disguiseData != null)
            {
                try
                {
                    state.disguise = DisguiseParser.parseDisguise(state.disguiseData);
                }
                catch (Throwable e)
                {
                    Logger.warn("无法从数据创建伪装：" + e.getMessage());
                    e.printStackTrace();
                }
            }

            saveConfiguration();

            return state;
        }

        return null;
    }
}
