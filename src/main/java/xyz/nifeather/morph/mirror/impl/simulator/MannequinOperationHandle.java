package xyz.nifeather.morph.mirror.impl.simulator;

import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.mirror.IOperationHandle;
import xyz.nifeather.morph.mirror.SimulateResult;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

public class MannequinOperationHandle implements IOperationHandle<Mannequin>
{
    @Override
    public SimulateResult simulateLeftClick(Mannequin entity)
    {
        return SimulateResult.fail();
    }

    @Override
    public SimulateResult simulateRightClick(Mannequin entity)
    {
        return SimulateResult.fail();
    }

    @Override
    public void simulateSneak(Mannequin entity, boolean sneaking)
    {
        System.out.println("SNeaking? " + sneaking);
        entity.setPose(sneaking ? Pose.SNEAKING : Pose.STANDING);
    }

    @Override
    public void simulateSwap(Mannequin entity)
    {
        var equipment = entity.getEquipment();

        var mainHandItem = equipment.getItemInMainHand();
        var offhandItem = equipment.getItemInOffHand();

        equipment.setItemInMainHand(offhandItem);
        equipment.setItemInOffHand(mainHandItem);
    }

    @Override
    public void scrollHotbar(Mannequin entity, int targetSlot)
    {
    }

    @Override
    public void releaseUsingItem(Mannequin entity, ItemStack referenceItem)
    {
    }

    @Override
    public boolean operationAllowed(Player source)
    {
        return source.hasPermission(CommonPermissions.MIRROR)
                && source.hasPermission(CommonPermissions.MIRROR_MANNEQUIN);
    }

    @Override
    public boolean affectedByMirror(Mannequin entity)
    {
        return true;
    }
}
