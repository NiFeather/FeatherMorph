package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;

public class NetheriteS2CRenderMeta
{
    public NetheriteS2CRenderMeta(int networkId)
    {
        this.networkId = networkId;
    }

    @Expose
    public int networkId = -1;

    @Expose
    @Nullable
    public String sNbt;

    @Expose
    @Nullable
    public NetheriteEquipment overridedEquipment;

    @Nullable
    @Expose
    public String profileCompound;

    @Expose
    public boolean showOverridedEquipment;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();

    @Override
    public String toString()
    {
        return gson.toJson(this);
    }

    public static NetheriteS2CRenderMeta fromString(String arg)
    {
        return gson.fromJson(arg, NetheriteS2CRenderMeta.class);
    }
}
