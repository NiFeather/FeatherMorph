package xyz.nifeather.morph.network.server;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.ConnectionState;
import xyz.nifeather.morph.network.InitializeState;
import xyz.nifeather.morph.network.PlayerOptions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerSession
{
    public PlayerSession(Player bindingPlayer, List<String> clientFeatures)
    {
        options = new PlayerOptions<>(bindingPlayer);
        this.clientFeatures.addAll(clientFeatures);
    }

    public final PlayerOptions<Player> options;

    @NotNull
    public InitializeState initializeState = InitializeState.NOT_CONNECTED;

    @NotNull
    public ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    public final List<String> clientFeatures = new CopyOnWriteArrayList<>();

    public static final class SessionBuilder
    {
        public static SessionBuilder builder(Player player)
        {
            return new SessionBuilder(player);
        }

        private final List<String> features = new CopyOnWriteArrayList<>();
        private final Player bindingPlayer;

        public SessionBuilder(Player bindingPlayer)
        {
            this.bindingPlayer = bindingPlayer;
        }

        public SessionBuilder withClientFeature(List<String> list)
        {
            features.addAll(list);
            return this;
        }

        public PlayerSession build()
        {
            return new PlayerSession(
                    bindingPlayer,
                    List.copyOf(features)
            );
        }
    }
}
