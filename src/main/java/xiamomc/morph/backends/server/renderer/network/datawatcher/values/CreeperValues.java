package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class CreeperValues extends MonsterValues
{
    public final SingleValue<Integer> STATE = getSingle("creeper_state", 0);
    public final SingleValue<Boolean> IS_CHARGED_CREEPER = getSingle("creeper_is_charged", false).withRandom(false, false, false, false, true);
    public final SingleValue<Boolean> IGNITED = getSingle("creeper_ignited", false);

    public CreeperValues()
    {
        registerSingle(STATE, IS_CHARGED_CREEPER, IGNITED);
    }
}
