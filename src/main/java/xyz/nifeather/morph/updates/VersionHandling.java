package xyz.nifeather.morph.updates;

import org.jetbrains.annotations.NotNull;

public class VersionHandling
{
    public static VersionInfo toVersionInfo(String str)
    {
        if (str == null) return VersionInfo.INVALID_VERSION;

        var strSpilt = str.split("\\.");

        var major = strSpilt.length >= 1 ? tryParse(strSpilt[0]) : -1;
        var minor = strSpilt.length >= 2 ? tryParse(strSpilt[1]) : -1;
        var patch = strSpilt.length >= 3 ? tryParse(strSpilt[2]) : -1;
        var edition = strSpilt.length >= 4 ? strSpilt[3] : "Standard";

        return new VersionInfo(major, minor, patch, edition);
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

    public static record VersionInfo(int major, int minor, int patch, @NotNull String channel)
    {
        @Override
        public String toString()
        {
            return "%s.%s.%s".formatted(major, minor, patch);
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
         * @param other
         * @return
         */
        public CompareResult compare(VersionInfo other)
        {
            if (!other.channel.equalsIgnoreCase(this.channel))
                return CompareResult.NOT_ON_SAME_CHANNEL;

            var majorCompare = integerCompare(other.major, this.major);
            var minorCompare = integerCompare(other.minor, this.minor);
            var patchCompare = integerCompare(other.patch, this.patch);

            if (majorCompare == CompareResult.EQUAL
                    && minorCompare == CompareResult.EQUAL
                    && patchCompare == CompareResult.EQUAL)
            {
                return CompareResult.EQUAL;
            }
            else if (majorCompare == CompareResult.NEWER
                        || minorCompare == CompareResult.NEWER
                        || patchCompare == CompareResult.NEWER)
            {
                return CompareResult.OLDER;
            }
            else if (majorCompare == CompareResult.OLDER
                    || minorCompare == CompareResult.OLDER
                    || patchCompare == CompareResult.OLDER)
            {
                return CompareResult.NEWER;
            }

            return CompareResult.NOT_ON_SAME_CHANNEL;
        }

        /**
         * 比较两个数之间的版本关系
         * @param a
         * @param b
         * @return a 相较于 b 的关系，如果a更大就返回NEWER，反之返回OLDER
         */
        private CompareResult integerCompare(int a, int b)
        {
            return a > b ? CompareResult.NEWER : (a == b ? CompareResult.EQUAL : CompareResult.OLDER);
        }

        public static VersionInfo INVALID_VERSION = new VersionInfo(0, 0, 0, "Invalid");
    }

    public enum CompareResult
    {
        EQUAL,
        NEWER,
        OLDER,
        NOT_ON_SAME_CHANNEL
    }
}
