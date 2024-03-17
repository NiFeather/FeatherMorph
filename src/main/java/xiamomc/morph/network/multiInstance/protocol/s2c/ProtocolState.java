package xiamomc.morph.network.multiInstance.protocol.s2c;

public enum ProtocolState
{
    INVALID(-3),
    NOT_CONNECTED(-2),
    WAITING_LOGIN(-1),
    LOGIN(0),
    SYNC(1),
    WAIT_LISTEN(2);

    private final int stateCode;

    ProtocolState(int stateCode)
    {
        this.stateCode = stateCode;
    }

    public boolean loggedIn()
    {
        return this.stateCode > LOGIN.stateCode;
    }
}

