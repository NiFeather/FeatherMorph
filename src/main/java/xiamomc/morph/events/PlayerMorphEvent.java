package xiamomc.morph.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.DisguiseState;

public class PlayerMorphEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();

    private final DisguiseState state;

    /**
     * 会在玩家进行伪装、更换伪装或带State加入时触发
     * @param who 玩家
     * @param state 玩家的{@link DisguiseState}
     */
    public PlayerMorphEvent(@NotNull Player who, @NotNull DisguiseState state)
    {
        super(who);

        this.state = state;
    }

    public @NotNull DisguiseState getState()
    {
        return state;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
