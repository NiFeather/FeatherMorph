package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class PlayerValues extends LivingEntityValues
{
    public final SingleValue<Float> ABSORPTION_AMOUNT = getSingle(0f);
    public final SingleValue<Integer> SCORE = getSingle(0);
    public final SingleValue<Byte> SKIN = getSingle((byte)0); //127
    public final SingleValue<Byte> MAINHAND = getSingle((byte)1);

    public PlayerValues()
    {
        super();

        System.out.println("SKIN is on index " + SKIN.index());

        registerSingle(ABSORPTION_AMOUNT, SCORE, SKIN, MAINHAND);
    }
}
