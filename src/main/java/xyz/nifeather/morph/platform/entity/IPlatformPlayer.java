package xyz.nifeather.morph.platform.entity;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface IPlatformPlayer extends IPlatformEntity
{
    String playerName();

    boolean hasPermission(String perm);

    void sendMessage(Component adventureComponent);
    void sendMessage(String... msg);

    void sendActionBar(Component component);

    boolean isOnline();

    void kick(Component reason);

    IPlatformEntity getTargetEntity(int distance);

    Set<String> getListeningPluginChannels();

    void sendPluginMessage(String channel, byte @NotNull [] buffer);
}
