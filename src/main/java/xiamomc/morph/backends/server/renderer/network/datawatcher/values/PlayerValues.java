package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class PlayerValues extends AbstractValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = SingleValue.of(15, 0f);
    public final SingleValue<Integer> SCORE = SingleValue.of(16, 0);
    public final SingleValue<Byte> SKIN = SingleValue.of(17, (byte)127);
    public final SingleValue<Byte> MAINHAND = SingleValue.of(18, (byte)1);

    public PlayerValues()
    {
        super();

        registerValue(ABSORPTION_AMOUNT, SCORE, SKIN, MAINHAND);
    }
}
