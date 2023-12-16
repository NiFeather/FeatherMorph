package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class ZombieValues extends MonsterValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle(false);
    public final SingleValue<Integer> ZOMBIE_TYPE = getSingle(0);
    public final SingleValue<Boolean> CONVERTING_DROWNED = getSingle(false);

    public ZombieValues()
    {
        super();

        registerSingle(IS_BABY, ZOMBIE_TYPE, CONVERTING_DROWNED);
    }
}
