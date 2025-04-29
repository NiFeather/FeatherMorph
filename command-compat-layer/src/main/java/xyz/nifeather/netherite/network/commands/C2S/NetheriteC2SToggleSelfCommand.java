package xyz.nifeather.netherite.network.commands.C2S;

import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.annotations.Environment;
import xyz.nifeather.netherite.network.annotations.EnvironmentType;

public class NetheriteC2SToggleSelfCommand extends NetheriteC2SCommand<NetheriteC2SToggleSelfCommand.SelfViewMode>
{
    public NetheriteC2SToggleSelfCommand(SelfViewMode val)
    {
        super(val);
    }

    public SelfViewMode getSelfViewMode()
    {
        return getArgumentAt(0);
    }


    @Override
    public String getBaseName()
    {
        return NetheriteC2SCommandNames.ToggleSelf;
    }

    @Environment(EnvironmentType.SERVER)
    @Override
    public void onCommand(BasicClientHandler<?> listener)
    {
        listener.onToggleSelfCommand(this);
    }

    public enum SelfViewMode
    {
        /**
         * 启用自身可见
         */
        ON("true"),

        /**
         * 禁用自身可见
         */
        OFF("false"),

        /**
         * 声明自身可见将被客户端处理
         */
        CLIENT_ON("client true"),

        /**
         * 声明自身可见不再被客户端处理
         */
        CLIENT_OFF("client false");

        private final String networkName;

        public static SelfViewMode fromBoolean(boolean value)
        {
            return value ? ON : OFF;
        }
        public static SelfViewMode fromString(String str)
        {
            if (str.equalsIgnoreCase("true")) return ON;
            else if (str.equalsIgnoreCase("false")) return OFF;
            else if (str.equalsIgnoreCase("client true")) return CLIENT_ON;
            else if (str.equalsIgnoreCase("client false")) return CLIENT_OFF;

            return OFF;
        }

        SelfViewMode(String networkName)
        {
            this.networkName = networkName;
        }

        @Override
        public String toString()
        {
            return networkName;
        }
    }
}
