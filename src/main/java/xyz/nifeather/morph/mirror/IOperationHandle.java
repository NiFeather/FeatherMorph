package xyz.nifeather.morph.mirror;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IOperationHandle<E extends LivingEntity>
{
    SimulateResult simulateLeftClick(E entity);
    SimulateResult simulateRightClick(E entity);
    void simulateSneak(E entity, boolean sneaking);
    void simulateSwap(E entity);
    void scrollHotbar(E entity, int targetSlot);
    void releaseUsingItem(E entity, ItemStack referenceItem);

    boolean operationAllowed(Player source);
    boolean affectedByMirror(E entity);
}
