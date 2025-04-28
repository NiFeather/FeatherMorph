package xyz.nifeather.morph.network.server.respond;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public record ClientInitializeRecord(
        @Expose
        @SerializedName("client_features")
        List<String> clientFeatures,

        @Expose
        @SerializedName("api_version")
        int apiVersion,

        boolean handleSuccess
)
{
    public static ClientInitializeRecord fail()
    {
        return new ClientInitializeRecord(List.of(), -1, false);
    }
}
