package xyz.nifeather.morph.mirror.impl.simulator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.mirror.IOperationHandle;
import xyz.nifeather.morph.mirror.SimulateResult;

public class FallbackOperationHandle implements IOperationHandle<LivingEntity>
{
    public static final FallbackOperationHandle INSTANCE = new FallbackOperationHandle();

    @Override
    public SimulateResult simulateLeftClick(LivingEntity entity)
    {
        return SimulateResult.fail();
    }

    @Override
    public SimulateResult simulateRightClick(LivingEntity entity)
    {
        return SimulateResult.fail();
    }

    @Override
    public void simulateSneak(LivingEntity entity, boolean sneaking)
    {
    }

    @Override
    public void simulateSwap(LivingEntity entity)
    {
    }

    @Override
    public void scrollHotbar(LivingEntity entity, int targetSlot)
    {
    }

    @Override
    public void releaseUsingItem(LivingEntity entity, ItemStack referenceItem)
    {
    }

    @Override
    public boolean operationAllowed(Player source)
    {
        return false;
    }

    @Override
    public boolean affectedByMirror(LivingEntity entity)
    {
        return false;
    }
}
