package xyz.nifeather.morph.interfaces;

import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.offlinestore.OfflineDisguiseState;

import java.util.List;
import java.util.UUID;

public interface IManageOfflineStates
{
    public void pushDisguiseState(DisguiseState state);

    public List<OfflineDisguiseState> getAvaliableDisguiseStates();

    public OfflineDisguiseState popDisguiseState(UUID uuid);
}
