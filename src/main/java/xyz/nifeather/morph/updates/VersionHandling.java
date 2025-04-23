package xyz.nifeather.morph.updates;

public class VersionHandling
{
    public static VersionInfo toVersionInfo(String str)
    {
        if (str == null || str.equals("null") || str.isBlank())
            return VersionInfo.INVALID_VERSION;

        var strSpilt = str.split("\\.");

        var major = strSpilt.length >= 1 ? tryParse(strSpilt[0]) : 0;
        var minor = strSpilt.length >= 2 ? tryParse(strSpilt[1]) : 0;
        var patch = strSpilt.length >= 3 ? tryParse(strSpilt[2]) : 0;
        var channel = strSpilt.length >= 4 ? strSpilt[3] : "DefaultRelease";

        return new VersionInfo(major, minor, patch, channel);
    }

    private static int tryParse(String str)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (Throwable t)
        {
            return -1;
        }
    }

    public static record VersionInfo(int major, int minor, int patch, String channel)
    {
        @Override
        public String toString()
        {
            return "%s.%s.%s.%s".formatted(major, minor, patch, channel);
        }

        public boolean isInvalid()
        {
            return this == INVALID_VERSION;
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof VersionInfo other)) return false;

            return major == other.major
                    && minor == other.minor
                    && patch == other.patch
                    && channel.equalsIgnoreCase(other.channel);
        }

        /**
         * 将此版本和另一版本比对
         * @param input
         * @return NEWER: other版本比这个版本高，反之OLDER或者EQUAL
         */
        public CompareResult compare(VersionInfo input)
        {
            // 2.0.0 <-> 2.0.0
            // 2.0.0 <-> 2.0.0.beta1
            if (input.major == this.major && input.minor == this.minor && input.patch == this.patch)
            {
                if (input.channel.equals(this.channel)) // 2.0.0.abc <-> 2.0.0.abc
                    return CompareResult.EQUAL;
                else
                    return CompareResult.NOT_ON_SAME_CHANNEL; // 2.0.0.abc <-> 2.0.0.bcd
            }

            if (input.major > this.major) // 2.x.x <-> 1.x.x
                return CompareResult.INPUT_NEWER;
            else if (input.major < this.major) // 1.x.x <-> 2.x.x
                return CompareResult.INPUT_OLDER;

            if (input.minor > this.minor) // 2.2.x <-> 2.1.x
                return CompareResult.INPUT_NEWER;
            else if (input.minor < this.minor) // 2.1.x <-> 2.2.x
                return CompareResult.INPUT_OLDER;

            if (input.patch > this.patch) // 2.2.2 <-> 2.2.1
                return CompareResult.INPUT_NEWER;
            else
                return CompareResult.INPUT_OLDER; // 2.2.1 <-> 2.2.2
        }

        public static VersionInfo INVALID_VERSION = new VersionInfo(0, 0, 0, "Invalid");
    }

    public enum CompareResult
    {
        EQUAL,
        INPUT_NEWER,
        INPUT_OLDER,
        NOT_ON_SAME_CHANNEL
    }
}
