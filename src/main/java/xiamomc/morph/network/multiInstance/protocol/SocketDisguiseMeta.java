package xiamomc.morph.network.multiInstance.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SocketDisguiseMeta
{
    @Expose
    @Nullable
    private Operation operation;

    public Operation getOperation()
    {
        return operation == null ? Operation.INVALID : operation;
    }

    @Expose
    private List<String> identifiers = new ObjectArrayList<>();

    public List<String> getIdentifiers()
    {
        return identifiers;
    }

    @Expose
    @Nullable
    private UUID uuid;

    /**
     * @return The uuid of the binding player. Not null if valid
     */
    @Nullable
    public UUID getBindingUuid()
    {
        return uuid;
    }

    public boolean isValid()
    {
        return uuid != null && operation != Operation.INVALID;
    }

    public SocketDisguiseMeta()
    {
    }

    public SocketDisguiseMeta(@NotNull Operation operation, List<String> ids, @NotNull UUID bindingUUID)
    {
        this.operation = operation;
        this.identifiers.addAll(ids);

        this.uuid = bindingUUID;
    }

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Override
    public String toString()
    {
        return gson.toJson(this);
    }
}

