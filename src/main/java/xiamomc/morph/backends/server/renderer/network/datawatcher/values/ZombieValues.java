package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class ZombieValues extends MonsterValues
{
    public final SingleValue<Boolean> IS_BABY = createSingle("zombie_is_baby", false);
    public final SingleValue<Integer> ZOMBIE_TYPE = createSingle("zombie_type", 0);
    public final SingleValue<Boolean> CONVERTING_DROWNED = createSingle("zombie_converting_drowned", false);

    public ZombieValues()
    {
        super();

        registerSingle(IS_BABY, ZOMBIE_TYPE, CONVERTING_DROWNED);
    }
}
