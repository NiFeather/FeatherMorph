package xiamomc.morph.storage.offlinestore;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class OfflineStates
{
    @Expose
    public List<OfflineDisguiseState> disguiseStates = new ArrayList<>();
}
