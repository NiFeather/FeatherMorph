package xiamomc.morph.misc.skins;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class SkinCacheRoot
{
    @Expose
    @SerializedName("profiles")
    public List<SingleSkin> storedSkins = new ObjectArrayList<>();
}
