package xiamomc.morph.network;

import org.bukkit.entity.Player;

public class MorphClientOptions
{
    public MorphClientOptions(Player player)
    {
        this.player = player;
    }

    private final Player player;

    private boolean clientSideSelfView;

    public boolean isClientSideSelfView()
    {
        return clientSideSelfView;
    }

    public void setClientSideSelfView(boolean newVal)
    {
        clientSideSelfView = newVal;
    }
}
