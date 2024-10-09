package xyz.nifeather.morph.storage.mirrorlogging;

import com.google.gson.annotations.Expose;

public class MirrorSingleEntry
{
    public MirrorSingleEntry(String playerName, String uuid, String targetPlayerName, OperationType operationType, int repeatingTimes, long timeMillis)
    {
        this.playerName = playerName;
        this.uuid = uuid;
        this.targetPlayerName = targetPlayerName;

        this.operationType = operationType;
        this.repeatingTimes = repeatingTimes;

        this.startingTimeMills = timeMillis;
    }

    @Expose
    private String playerName;

    public String playerName()
    {
        return playerName;
    }

    @Expose
    private String uuid;

    public String uuid()
    {
        return uuid;
    }

    @Expose
    private String targetPlayerName;

    public String targetPlayerName()
    {
        return targetPlayerName;
    }

    @Expose
    private OperationType operationType;

    public OperationType operationType()
    {
        return operationType;
    }

    @Expose
    private int repeatingTimes;

    public int repeatingTimes()
    {
        return repeatingTimes;
    }

    public int increaseRepeatingTimes()
    {
        repeatingTimes++;

        return repeatingTimes;
    }

    @Expose
    private long startingTimeMills;

    public long timeMills()
    {
        return startingTimeMills;
    }
}