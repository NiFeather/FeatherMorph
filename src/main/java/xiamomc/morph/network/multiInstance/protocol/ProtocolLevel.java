package xiamomc.morph.network.multiInstance.protocol;

public enum ProtocolLevel
{
    V1(1);

    private final int version;

    ProtocolLevel(int version)
    {
        this.version = version;
    }

    public int version()
    {
        return version;
    }

    public String versionString()
    {
        return "" + version;
    }

    public boolean isNewerThan(ProtocolLevel other)
    {
        return this.version > other.version;
    }

    public boolean isNewerThan(int other)
    {
        return this.version > other;
    }

    public boolean isOlderThan(ProtocolLevel other)
    {
        return this.version < other.version;
    }

    public boolean isOlderThan(int other)
    {
        return this.version < other;
    }

    public boolean equals(ProtocolLevel other)
    {
        return this.version == other.version;
    }

    public boolean equals(int other)
    {
        return this.version == other;
    }
}
