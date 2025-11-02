package xyz.nifeather.morph.mirror;

import net.minecraft.world.InteractionHand;
import org.bukkit.inventory.EquipmentSlot;

/**
 * 操作模拟结果
 *
 * @param success 是否成功
 * @param hand    与 {@link InteractionHand} 对应的 {@link EquipmentSlot}
 */
public record SimulateResult(boolean success, EquipmentSlot hand, boolean forceSwing)
{
    public static SimulateResult success(EquipmentSlot hand)
    {
        return of(true, hand);
    }

    public static SimulateResult success(EquipmentSlot hand, boolean clickedOnBlock)
    {
        return of(true, hand, clickedOnBlock);
    }

    public static SimulateResult fail()
    {
        return of(false, null);
    }

    public static SimulateResult of(boolean success, EquipmentSlot hand)
    {
        return new SimulateResult(success, hand, false);
    }

    public static SimulateResult of(boolean success, EquipmentSlot hand, boolean clickedOnBlock)
    {
        return new SimulateResult(success, hand, clickedOnBlock);
    }
}
