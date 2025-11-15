package xyz.nifeather.morph.messages;

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @param resultFile The final dumped place
 * @param backupFile Backup of the original file on the disk before executing the dump, if there's any.
 */
public record DumpResult(File resultFile, @Nullable File backupFile)
{
}
