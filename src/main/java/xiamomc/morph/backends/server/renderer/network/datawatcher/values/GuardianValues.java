package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class GuardianValues extends MonsterValues
{
    public final SingleValue<Boolean> RETRACING_SPIKES = getSingle(false);
    public final SingleValue<Integer> TARGET_ENTITY_ID = getSingle(-1);

    public GuardianValues()
    {
        super();

        registerSingle(RETRACING_SPIKES, TARGET_ENTITY_ID);
    }
}
