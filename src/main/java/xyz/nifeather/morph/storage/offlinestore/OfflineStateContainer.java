package xyz.nifeather.morph.storage.offlinestore;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class OfflineStateContainer
{
    @Expose
    public List<OfflineDisguiseState> disguiseStates = new ObjectArrayList<>();
}
