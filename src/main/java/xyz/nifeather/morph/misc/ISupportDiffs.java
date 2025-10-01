package xyz.nifeather.morph.misc;

/**
 * This object supports generating a diff version of the object from other instances
 */
public interface ISupportDiffs<T>
{
    T diff(T other);
}
