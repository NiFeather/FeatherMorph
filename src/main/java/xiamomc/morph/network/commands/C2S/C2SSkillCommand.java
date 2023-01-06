package xiamomc.morph.network.commands.C2S;

import org.bukkit.entity.Player;

public class C2SSkillCommand extends AbstractC2SCommand
{
    @Override
    public String getBaseName()
    {
        return "skill";
    }

    @Override
    public void onCommand(Player player, String[] arguments)
    {
        var manager = morphManager();
        var state = manager.getDisguiseStateFor(player);

        if (state != null && state.getSkillCooldown() <= 0)
            manager.executeDisguiseSkill(player);
    }
}
