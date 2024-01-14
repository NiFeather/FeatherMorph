package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class ZombieValues extends MonsterValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle("zombie_is_baby", false);
    public final SingleValue<Integer> ZOMBIE_TYPE = getSingle("zombie_type", 0);
    public final SingleValue<Boolean> CONVERTING_DROWNED = getSingle("zombie_converting_drowned", false);

    public ZombieValues()
    {
        super();

        registerSingle(IS_BABY, ZOMBIE_TYPE, CONVERTING_DROWNED);
    }
}
