package xyz.nifeather.morph.storage.offlinestore;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OfflineStateContainer
{
    @Expose
    public List<OfflineDisguiseState> disguiseStates = new CopyOnWriteArrayList<>();
}
