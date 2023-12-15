package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class PlayerValues extends LivingEntityValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = getSingle(0f);
    public final SingleValue<Integer> SCORE = getSingle(0);
    public final SingleValue<Byte> SKIN_FLAGS = getSingle((byte)0); //127
    public final SingleValue<Byte> MAINHAND = getSingle((byte)1);
    public final SingleValue<Object> LEFT_SHOULDER_PARROT_COMPOUND = getSingle(new Object());
    public final SingleValue<Object> RIGHT_SHOULDER_PARROT_COMPOUND = getSingle(new Object());

    public PlayerValues()
    {
        super();

        registerSingle(ABSORPTION_AMOUNT, SCORE, SKIN_FLAGS, MAINHAND);
    }
}
