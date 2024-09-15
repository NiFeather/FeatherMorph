package xiamomc.morph.providers.animation;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.AnimationNames;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnimationSet
{
    public static final SingleAnimation RESET = new SingleAnimation(AnimationNames.RESET, 1, true);
    public static final SingleAnimation TRY_RESET = new SingleAnimation(AnimationNames.TRY_RESET, 1, true);

    public static final SingleAnimation EXEC_DISABLE_SKILL = new SingleAnimation(AnimationNames.INTERNAL_DISABLE_SKILL, 0, false);
    public static final SingleAnimation EXEC_ENABLE_SKILL = new SingleAnimation(AnimationNames.INTERNAL_ENABLE_SKILL, 0, false);
    public static final SingleAnimation EXEC_DISABLE_AMBIENT = new SingleAnimation(AnimationNames.INTERNAL_DISABLE_AMBIENT, 0, false);
    public static final SingleAnimation EXEC_ENABLE_AMBIENT = new SingleAnimation(AnimationNames.INTERNAL_ENABLE_AMBIENT, 0, false);
    public static final SingleAnimation EXEC_DISABLE_BOSSBAR = new SingleAnimation(AnimationNames.INTERNAL_DISABLE_BOSSBAR, 0, false);
    public static final SingleAnimation EXEC_ENABLE_BOSSBAR = new SingleAnimation(AnimationNames.INTERNAL_ENABLE_BOSSBAR, 0, false);

    // SequenceId <-> <Sequence, IsPersistent>
    private final Map<String, Pair<List<SingleAnimation>, Boolean>> animationMap = new ConcurrentHashMap<>();

    private final List<String> registeredAnimations = new ObjectArrayList<>();

    /**
     * Register an non-persistent sequence<br>
     * Also see {@link AnimationSet#register(String, List, boolean)}
     * @param sequenceID
     * @param sequence
     */
    protected void registerCommon(String sequenceID, List<SingleAnimation> sequence)
    {
        this.register(sequenceID, sequence, false);
    }

    /**
     * Register a persistent sequence<br>
     * Also see {@link AnimationSet#register(String, List, boolean)}
     * @param sequenceID
     * @param sequence
     */
    protected void registerPersistent(String sequenceID, List<SingleAnimation> sequence)
    {
        this.register(sequenceID, sequence, true);
    }

    /**
     * Register sequence
     * @param sequenceID The identifier of this sequence
     * @param sequence The sequence of {@link SingleAnimation} instances
     * @param isPersistent Whether this sequence result in persistent effects.
     *                     If True, the server won't send the packet to tell the client to clear the name displayed on GUI after the sequence finished playing.
     */
    protected void register(String sequenceID, List<SingleAnimation> sequence, boolean isPersistent)
    {
        animationMap.put(sequenceID, Pair.of(sequence, isPersistent));
        registeredAnimations.add(sequenceID);
    }

    /**
     * Gets the animation sequence for the given ID(Name)
     * @param animationId The animation ID(Name) to lookup
     * @return A pair, left is the sequence, right is whether the sequence is persistent
     */
    @NotNull
    public Pair<List<SingleAnimation>, Boolean> sequenceOf(String animationId)
    {
        return animationMap.getOrDefault(animationId, Pair.of(List.of(), false));
    }

    /**
     * Get all available animation names that is suitable for client (and the command tab complete) in this AnimationSet
     */
    public List<String> getAvailableAnimationsForClient()
    {
        return new ObjectArrayList<>(registeredAnimations);
    }
}
