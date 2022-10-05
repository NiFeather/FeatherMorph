package xiamomc.morph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.misc.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataManager extends MorphPluginObject implements IManagePlayerData
{
    private File configurationFile;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private MorphConfiguration morphConfiguration = new MorphConfiguration();

    private final List<DisguiseInfo> cachedInfos = new ArrayList<>();

    @Initializer
    private void load(MorphPlugin plugin) throws IOException
    {
        //初始化配置文件
        if (configurationFile == null)
            configurationFile = new File(URI.create(plugin.getDataFolder().toURI() + "/data.json"));

        if (!configurationFile.exists())
        {
            //创建父目录
            if (!configurationFile.getParentFile().exists())
                Files.createDirectories(Paths.get(configurationFile.getParentFile().toURI()));

            if (!configurationFile.createNewFile())
            {
                Logger.error("未能创建文件，将不会加载玩家配置！");
                return;
            }
        }

        reloadConfiguration();
    }

    //region Implementation of IManagePlayerData

    @Override
    public void reloadConfiguration()
    {
        //加载JSON配置
        MorphConfiguration targetConfiguration = null;
        var success = false;

        //从文件读取并反序列化为配置
        try (var jsonStream = new InputStreamReader(new FileInputStream(configurationFile)))
        {
            targetConfiguration = gson.fromJson(jsonStream, MorphConfiguration.class);
            success = true;
        }
        catch (IOException e)
        {
            Logger.warn("无法加载JSON配置：" + e.getMessage());
            e.printStackTrace();
        }

        //确保targetConfiguration不是null
        if (targetConfiguration == null) targetConfiguration = new MorphConfiguration();

        //设置并保存
        morphConfiguration = targetConfiguration;
        if (success) saveConfiguration();
    }

    private void saveConfiguration()
    {
        try
        {
            var jsonString = gson.toJson(morphConfiguration);

            if (configurationFile.exists()) configurationFile.delete();

            if (!configurationFile.createNewFile())
            {
                Logger.error("未能创建文件，将不会保存玩家配置！");
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

    @Override
    public PlayerMorphConfiguration getPlayerConfiguration(Player player)
    {
        var valueOptional = morphConfiguration.playerMorphConfigurations
                .stream().filter(c -> c.uniqueId.equals(player.getUniqueId())).findFirst();

        if (valueOptional.isPresent()) return valueOptional.get();
        else
        {
            var newInstance = new PlayerMorphConfiguration();
            newInstance.uniqueId = player.getUniqueId();
            newInstance.shownTutorialOnce = false;
            newInstance.unlockedDisguises = new ArrayList<>();

            var msg = Component.text("不知道如何使用伪装? 发送 /mmorph help 即可查看！");
            player.sendMessage(MessageUtils.prefixes(player, msg));

            morphConfiguration.playerMorphConfigurations.add(newInstance);
            return newInstance;
        }
    }

    @Override
    public void addNewMorphToPlayer(Player player, Entity entity)
    {
        var playerConfiguration = getPlayerConfiguration(player);
        var info = getDisguiseInfo(entity.getType());

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(info)))
        {
            playerConfiguration.unlockedDisguises.add(info);
            saveConfiguration();
        }
        else return;
    }

    @Override
    public void addNewPlayerMorphToPlayer(Player sourcePlayer, Player targtPlayer)
    {
        var playerConfiguration = getPlayerConfiguration(sourcePlayer);

        if (playerConfiguration.unlockedDisguises.stream().noneMatch(c -> c.equals(targtPlayer.getName())))
            playerConfiguration.unlockedDisguises.add(this.getDisguiseInfo(targtPlayer.getName()));
        else
            return;

        saveConfiguration();
    }

    @Override
    public DisguiseInfo getDisguiseInfo(EntityType type)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(type)))
            cachedInfos.add(new DisguiseInfo(type));

        return cachedInfos.stream().filter(o -> o.equals(type)).findFirst().get();
    }

    @Override
    public DisguiseInfo getDisguiseInfo(String playerName)
    {
        if (this.cachedInfos.stream().noneMatch(o -> o.equals(playerName)))
            cachedInfos.add(new DisguiseInfo(playerName));

        return cachedInfos.stream().filter(o -> o.equals(playerName)).findFirst().get();
    }

    @Override
    public ArrayList<DisguiseInfo> getAvaliableDisguisesFor(Player player)
    {
        return getPlayerConfiguration(player).unlockedDisguises;
    }

    //endregion Implementation of IManagePlayerData
}
