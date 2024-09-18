package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractFishValues;

public class PufferfishValues extends AbstractFishValues
{
    public final SingleValue<Integer> PUFF_STATE = createSingle("puff_state", PuffStates.SMALL);

    public PufferfishValues()
    {
        registerSingle(PUFF_STATE);
    }

    public final static class PuffStates
    {
        public static final int SMALL = 0;
        public static final int MID = 1;
        public static final int LARGE = 2;
    }
}
