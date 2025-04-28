package xyz.nifeather.morph.network.server.respond;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record InitializeRespond(
        @Expose
        @SerializedName("server_features")
        @Unmodifiable
        List<String> serverFeatures,

        @Expose
        @SerializedName("api_version")
        int apiVersion
)
{
}
