package xiamomc.morph.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Configuration.Bindable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Minecraft语言相关
 */
public class MinecraftLanguageHelper extends MorphPluginObject
{
    @Initializer
    private void load(MorphConfigManager config)
    {
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .proxy(ProxySelector.getDefault())
                .build();

        config.bind(localeCode, ConfigOption.LANGUAGE_CODE);
        config.bind(allowTranslatable, ConfigOption.LANGUAGE_ALLOW_FALLBACK);

        localeCode.onValueChanged((o, n) -> switchLanguage(n), true);

        initializeFallbackLanguage(false);
    }

    private static Bindable<Boolean> allowTranslatable = new Bindable<>(false);

    private void initializeFallbackLanguage(boolean isRetry)
    {
        getLanguageAsset("en_us", false, success ->
        {
            var map = getLanguageMap(getLanguageFile("en_us"), "en_us");

            if (map == null)
            {
                if (isRetry)
                    logger.error("无法初始化fallback语言");
                else
                    getLanguageAsset("en_us", true, s -> initializeFallbackLanguage(true), null);
            }

            fallbackLanguageMap = map;
        }, null);
    }

    private Bindable<String> localeCode = new Bindable<>("en_us");

    private final String urlPattern = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.19.2/assets/minecraft/lang/";

    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private HttpClient http;

    private final String langDirUri = plugin.getDataFolder().toURI() + "/mclang";
    private final File langDir = new File(URI.create(langDirUri));

    @Nullable
    private static HashMap<?, ?> languageMap;

    @Nullable
    private static HashMap<?, ?> fallbackLanguageMap;

    /**
     * 获取某个语言代码对应的File
     * @param languageName 语言代码
     * @return 一个File
     */
    private File getLanguageFile(String languageName)
    {
        return new File(URI.create(langDirUri + "/" + languageName + ".json"));
    }

    /**
     * 尝试从某个文件获取语言Map
     * @param targetFile 目标文件
     * @param languageName 语言名称
     * @return 语言Map
     */
    private HashMap<?, ?> getLanguageMap(File targetFile, String languageName)
    {
        try (var stream = new FileInputStream(targetFile))
        {
            var map = gson.fromJson(new InputStreamReader(stream), HashMap.class);

            if (!map.containsKey("language.code"))
            {
                logger.error("无效的语言配置:" + languageName);
                return null;
            }

            return map;
        }
        catch (Throwable t)
        {
            logger.error("未能读取语言资源：" + t.getMessage());
            t.printStackTrace();
            return null;
        }
    }

    /**
     * 切换LanguageHelper的首选语言
     * @param languageName 目标语言代码
     * @return 操作是否成功
     */
    private boolean switchLanguage(String languageName)
    {
        var targetFile = getLanguageFile(languageName);

        if (targetFile.exists())
        {
            languageMap = getLanguageMap(targetFile, languageName);
        }
        else
        {
            logger.warn("没有找到和" + languageName + "匹配的语言资源，正在尝试下载...");
            getLanguageAsset(languageName, false,
                    success ->
                    {
                        switchLanguage(languageName);
                    },
                    fail ->
                    {
                        logger.error("无法下载语言文件");
                    });
        }

        return true;
    }

    /**
     * 获取语言资源并保存到插件目录中
     *
     * @param languageName 语言ID
     */
    public void getLanguageAsset(String languageName, boolean overWrite, @Nullable Consumer<?> onSuccess, @Nullable Consumer<?> onFail)
    {
        if (!langDir.exists())
        {
            var result = langDir.mkdirs();

            if (!result)
            {
                logger.error("未能创建" + langDir + ", 无法下载语言文件");
                return;
            }
        }

            var targetFile = new File(URI.create(langDirUri + "/" + languageName + ".json"));

            if (targetFile.exists() && !overWrite)
                return;

            this.addSchedule(c ->
            {
                try
                {
                    var req = new URL(urlPattern + languageName + ".json");
                    var con = (HttpURLConnection) req.openConnection();

                    con.setRequestMethod("GET");
                    con.setInstanceFollowRedirects(true);
                    con.setReadTimeout(3000);
                    con.setConnectTimeout(3000);

                    String str;
                    StringBuilder builder = new StringBuilder();
                    var reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    while ((str = reader.readLine()) != null)
                        builder.append(str).append("\n");

                    var success = con.getResponseCode() == 200;

                    con.disconnect();

                    if (success)
                    {
                        try (var stream = new FileOutputStream(targetFile))
                        {
                            stream.write(builder.toString().getBytes());
                            logger.info("成功下载" + languageName + "的语言文件！");
                        }
                        catch (Throwable t)
                        {
                            logger.error("未能写入语言资源：" + t.getMessage());
                            t.printStackTrace();

                            if (onFail != null)
                                onFail.accept(null);
                        }

                        if (onSuccess != null)
                            onSuccess.accept(null);
                    }
                    else
                    {
                        logger.warn("未能下载语言文件：" + con.getResponseCode() + " :: " + con.getRequestMethod());

                        if (onFail != null)
                            onFail.accept(null);
                    }
                }
                catch (Throwable t)
                {
                    logger.error("无法创建请求: " + t.getMessage());
                    t.printStackTrace();

                    if (onFail != null)
                        onFail.accept(null);
                }
            }, 1, true);
    }

    /**
     * 获取和key对应的文本
     * @param key key
     * @return 一段文本
     */
    @Nullable
    public static String get(String key)
    {
        if (languageMap == null)
        {
            if (fallbackLanguageMap == null) return null;
            else
            {
                var fallback = fallbackLanguageMap.get(key);

                return fallback == null ? null : "" + fallback;
            }
        }
        else
        {
            var content = languageMap.get(key);

            return content == null ? null : "" + content;
        }
    }

    public static Component getComponent(String key)
    {
        var content = get(key);

        if (content != null)
        {
            return Component.text(content);
        }
        else
        {
            return allowTranslatable.get()
                    ? Component.translatable(key)
                    : Component.text(key);
        }
    }
}
