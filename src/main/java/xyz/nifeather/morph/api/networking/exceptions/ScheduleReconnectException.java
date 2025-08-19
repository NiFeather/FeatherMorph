package xyz.nifeather.morph.api.networking.exceptions;

/**
 * Disconnected the player because A reconnect has been scheduled for them.<br>
 * Normally changing the `modify_bounding_boxes` option could trigger this exception.
 */
public class ScheduleReconnectException extends RuntimeException
{
    public ScheduleReconnectException(String s)
    {
        super(s);
    }
}
