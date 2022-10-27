package xiamomc.morph.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
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
        var dslConfig = Dsl.config()
                .setUseProxyProperties(true)
                .setUseProxySelector(true)
                .setConnectTimeout(3000)
                .setReadTimeout(3000)
                .setRequestTimeout(3000);

        http = Dsl.asyncHttpClient(dslConfig);

        config.onConfigRefresh(c ->
        {
            localeCode = config.getOrDefault(String.class, ConfigOption.LANGUAGE_CODE);
            allowTranslatable = config.getOrDefault(Boolean.class, ConfigOption.LANGUAGE_ALLOW_FALLBACK);

            switchLanguage(localeCode);
        }, true);

        initializeFallbackLanguage(false);
    }

    private static boolean allowTranslatable;

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

    private String localeCode;

    private final String urlPattern = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.19.2/assets/minecraft/lang/";

    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private AsyncHttpClient http;

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

        try
        {
            var targetFile = new File(URI.create(langDirUri + "/" + languageName + ".json"));

            if (targetFile.exists() && !overWrite)
                return;

            var req = http.prepareGet(urlPattern + languageName + ".json");

            req.setReadTimeout(3000);
            req.setRequestTimeout(3000);

            req.execute(new RequestHandler(s ->
            {
                try (var stream = new FileOutputStream(targetFile))
                {
                    stream.write(s.getBytes());
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
            }, t ->
            {
                logger.warn("FAIL! " + t.getMessage());
                t.printStackTrace();

                if (onFail != null)
                    onFail.accept(null);
            }));
        }
        catch (Throwable t)
        {
            logger.error("http failed: " + t.getMessage());
            t.printStackTrace();

            if (onFail != null)
                onFail.accept(null);
        }
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
            return allowTranslatable
                    ? Component.translatable(key)
                    : Component.text(key);
        }
    }

    private static class RequestHandler extends AsyncCompletionHandler<String>
    {
        public RequestHandler(@Nullable Consumer<String> onSuccess, @Nullable Consumer<Throwable> onFail)
        {
            this.onFail = onFail;
            this.onSuccess = onSuccess;
        }

        @Nullable
        private final Consumer<String> onSuccess;

        @Nullable
        private final Consumer<Throwable> onFail;

        @Override
        public String onCompleted(Response response)
        {
            var resp = response.getResponseBody();

            if (onSuccess != null)
                onSuccess.accept(response.getResponseBody());

            return resp;
        }

        @Override
        public void onThrowable(Throwable t)
        {
            super.onThrowable(t);

            if (onFail != null)
                onFail.accept(t);
        }
    }
}
