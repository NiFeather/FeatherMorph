package xiamomc.morph.interfaces;

import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;

import java.util.List;
import java.util.UUID;

public interface IManageOfflineStates
{
    public void pushDisguiseState(DisguiseState state);

    public List<OfflineDisguiseState> getAvaliableDisguiseStates();

    public OfflineDisguiseState popDisguiseState(UUID uuid);
}
