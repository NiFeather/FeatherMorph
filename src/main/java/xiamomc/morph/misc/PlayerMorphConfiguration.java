package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerMorphConfiguration
{
    @Expose
    public UUID uniqueId;

    @Expose
    public ArrayList<DisguiseInfo> unlockedDisguises;

    @Expose
    public boolean shownMorphPlayerMessageOnce;
}
