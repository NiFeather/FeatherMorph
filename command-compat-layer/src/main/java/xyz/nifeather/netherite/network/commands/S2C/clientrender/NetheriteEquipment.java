package xyz.nifeather.netherite.network.commands.S2C.clientrender;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class NetheriteEquipment
{
    public static final String air = "minecraft:air";

    public static final String nilNbt = "{}";

    @Expose
    @NotNull
    public String headId = air;

    @Expose
    @NotNull
    public String headNbt = nilNbt;

    @Expose
    @NotNull
    public String chestId = air;

    @Expose
    @NotNull
    public String chestNbt = nilNbt;

    @Expose
    @NotNull
    public String leggingId = air;

    @Expose
    @NotNull
    public String leggingNbt = nilNbt;

    @Expose
    @NotNull
    public String feetId = air;

    @Expose
    @NotNull
    public String feetNbt = nilNbt;

    @Expose
    @NotNull
    public String handId = air;

    @Expose
    @NotNull
    public String handNbt = nilNbt;

    @Expose
    @NotNull
    public String offhandId = air;

    @Expose
    @NotNull
    public String offhandNbt = nilNbt;
}
