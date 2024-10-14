package xyz.nifeather.morph.network.modrinthapi;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class ModrinthVersionResponse
{
    @SerializedName("id")
    public String modrinthId;

    @SerializedName("project_id")
    public String projectId;

    @SerializedName("author_id")
    public String authorId;

    @SerializedName("featured")
    public boolean manualFeatured;

    @SerializedName("name")
    public String versionName;

    @SerializedName("version_number")
    public String versionNumber;

    public String changelog;

    @SerializedName("changelog_url")
    public String changelogUrl;

    @SerializedName("date_published")
    public String datePublished;

    public int downloads;

    @SerializedName("version_type")
    public String versionType;

    public String status;

    @Nullable
    @SerializedName("requested_status")
    public String requestedStatus;

    //public Object[] files;

    //public Object[] dependencies;

    @SerializedName("game_versions")
    public String[] gameVersions;

    public String[] loaders;
}
