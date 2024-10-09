package xyz.nifeather.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CapeStrings extends AbstractMorphStrings
{
    public static FormattableMessage cherry()
    {
        return getFormattable(getKey("cherry"), "<color:#F8BBD0>樱花");
    }

    public static FormattableMessage vanilla()
    {
        return getFormattable(getKey("vanilla"), "<color:#85BC51>香草");
    }

    public static FormattableMessage migrator()
    {
        return getFormattable(getKey("migrator"), "<color:red>迁移者");
    }

    public static FormattableMessage minecon2016()
    {
        return getFormattable(getKey("minecon_2016"), "Minecon 2016");
    }

    public static FormattableMessage minecon2015()
    {
        return getFormattable(getKey("minecon_2015"), "Minecon 2015");
    }

    public static FormattableMessage minecon2013()
    {
        return getFormattable(getKey("minecon_2013"), "Minecon 2013");
    }

    public static FormattableMessage minecon2012()
    {
        return getFormattable(getKey("minecon_2012"), "Minecon 2012");
    }

    public static FormattableMessage minecon2011()
    {
        return getFormattable(getKey("minecon_2011"), "Minecon 2011");
    }

    public static FormattableMessage mojangClassic()
    {
        return getFormattable(getKey("mojang_0"), "Mojang Classic");
    }

    public static FormattableMessage mojang()
    {
        return getFormattable(getKey("mojang_1"), "Mojang");
    }

    public static FormattableMessage mojangStudios()
    {
        return getFormattable(getKey("mojang_2"), "Mojang Studios");
    }

    public static FormattableMessage millionthSale()
    {
        return getFormattable(getKey("millionth_sale"), "一百万披风");
    }

    public static FormattableMessage dbCape()
    {
        return getFormattable(getKey("db"), "dB披风");
    }

    public static FormattableMessage julian()
    {
        return getFormattable(getKey("julian"), "雪人");
    }

    public static FormattableMessage jpTranslator()
    {
        return getFormattable(getKey("jp_translator"), "日本翻译者");
    }

    public static FormattableMessage cnTranslator()
    {
        return getFormattable(getKey("cn_translator"), "Crowdin中文翻译者");
    }

    public static FormattableMessage mrMessiah()
    {
        return getFormattable(getKey("mr_messiah"), "黑桃");
    }

    public static FormattableMessage prismarine()
    {
        return getFormattable(getKey("prismarine"), "海晶石");
    }

    public static FormattableMessage turtle()
    {
        return getFormattable(getKey("turtle"), "海龟");
    }

    public static FormattableMessage birthday()
    {
        return getFormattable(getKey("birthday"), "生日披风");
    }

    public static FormattableMessage valentine()
    {
        return getFormattable(getKey("valentine"), "情人披风");
    }

    public static FormattableMessage realmsMaker()
    {
        return getFormattable(getKey("realms_maker"), "Realms 地图创作者");
    }

    public static FormattableMessage translator()
    {
        return getFormattable(getKey("translator"), "Crowdin翻译者");
    }

    public static FormattableMessage scrolls()
    {
        return getFormattable(getKey("scrolls"), "Scrolls披风");
    }

    public static FormattableMessage cobalt()
    {
        return getFormattable(getKey("cobalt"), "Cobalt");
    }

    public static FormattableMessage mojira()
    {
        return getFormattable(getKey("mojira_moderator"), "Mojira管理员");
    }

    public static FormattableMessage other()
    {
        return getFormattable(getKey("other"), "<color:#dadada>其他");
    }

    private static String getKey(String key)
    {
        return "cape." + key;
    }
}
