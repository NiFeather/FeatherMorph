package xiamomc.morph.network;

/**
 * 客户端的初始化状态
 */
public enum InitializeState
{
    /**
     * 客户端未连接或初始化被中断
     */
    NOT_CONNECTED(-2),

    PENDING(-1),

    /**
     * 接收到初始化指令，但还没做更进一步的交流
     */
    HANDSHAKE(0),

    /**
     * 获取到客户端的API版本，但尚未收到客户端的初始化指令
     */
    API_CHECKED(1),

    /**
     * 所有初始化操作均已完成
     */
    DONE(2);

    private final int val;

    InitializeState(int value)
    {
        this.val = value;
    }

    public boolean greaterThan(InitializeState other)
    {
        return this.val > other.val;
    }

    public boolean worseThan(InitializeState other)
    {
        return this.val < other.val;
    }
}
