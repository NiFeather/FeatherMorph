package xyz.nifeather.morph.mirror;

import org.bukkit.entity.LivingEntity;

public interface IExecutor<TSourceType, TItemStack, TAction>
{
    public void onSneak(TSourceType player, boolean sneaking);
    public void onSwapHand(TSourceType player);

    public void onHotbarChange(TSourceType player, int slot);
    public void onStopUsingItem(TSourceType player, TItemStack itemStack);

    /**
     *
     * @param source
     * @param target
     * @return Whether to cancel the event
     * @apiNote Actually, only players can pass to `target`, for performance...
     */
    public boolean onHurtEntity(TSourceType source, LivingEntity target);

    /**
     *
     * @param player
     * @return Whether to cancel the event
     */
    public boolean onSwing(TSourceType player);

    public void onInteract(TSourceType player, TAction action);

    public void reset();
}
