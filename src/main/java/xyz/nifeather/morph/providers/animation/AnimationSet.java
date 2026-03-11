package xyz.nifeather.morph.providers.animation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AnimationSet
{
    /**
     * For internal use, so that we can notify clients using the S2CSetAnimationDisplayNameCommand can aware that the current action has finished playing.
     */
    @ApiStatus.Internal
    public static final ActionStage FINISH = new PlayableAction.ActionStageBuilder().legacyName("reset").build();

    // SequenceId <-> <Sequence, IsPersistent>
    private final Map<String, PlayableAction> animationMap = new ConcurrentHashMap<>();

    private final List<String> registeredAnimations = new CopyOnWriteArrayList<>();

    /**
     * Register a {@link PlayableAction} to this AnimationSet.
     * @param actionName Name of the action, is used to determine whether a player has the permission to run the action.
     * @param action The {@link PlayableAction}
     */
    protected void register(String actionName, PlayableAction action)
    {
        animationMap.put(actionName, action);
        registeredAnimations.add(actionName);
    }

    /**
     * Gets the animation sequence for the given ID(Name)
     *
     * @param animationId The animation ID(Name) to lookup
     * @return A pair, left is the sequence, right is whether the sequence is persistent
     */
    @Nullable
    public PlayableAction getAction(@NotNull String animationId)
    {
        return animationMap.getOrDefault(animationId, null);
    }

    /**
     * Get all available animation names that is suitable for client (and the command tab complete) in this AnimationSet
     */
    public List<String> getAvailableAnimationsForClient()
    {
        return new ObjectArrayList<>(registeredAnimations);
    }
}
