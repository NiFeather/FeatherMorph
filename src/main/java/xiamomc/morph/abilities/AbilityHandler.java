package xiamomc.morph.abilities;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.impl.*;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.SkillConfigurationStore;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityHandler extends MorphPluginObject
{
    private final List<IMorphAbility> registedAbilities = new ArrayList<>();

    @Resolved
    private SkillConfigurationStore store;

    public boolean registerAbility(IMorphAbility ability)
    {
        if (registedAbilities.stream().anyMatch(a -> a.getIdentifier().equals(ability.getIdentifier())))
        {
            logger.error("已经注册过一个" + ability.getIdentifier().asString() + "的被动技能了");
            return false;
        }

        registedAbilities.add(ability);
        Bukkit.getPluginManager().registerEvents(ability, plugin);
        return true;
    }

    public boolean registerAbilities(List<IMorphAbility> abilities)
    {
        abilities.forEach(a -> registerAbility(a));
        return true;
    }

    @Initializer
    private void load()
    {
        registerAbilities(List.of(
                new BreatheUnderWaterAbility(),
                new BurnsUnderSunAbility(),
                new FeatherFallingAbility(),
                new FireResistanceAbility(),
                new FlyAbility(),
                new JumpBoostAbility(),
                new NightVisionAbility(),
                new NoFallDamageAbility(),
                new ReduceFallDamageAbility(),
                new ReduceMagicDamageAbility(),
                new SmallJumpBoostAbility(),
                new SnowyAbility(),
                new SpeedBoostAbility(),
                new TakesDamageFromWaterAbility()
        ));
    }

    @Nullable
    public IMorphAbility getAbility(@Nullable NamespacedKey key)
    {
        if (key == null) return null;

        var val = registedAbilities.stream()
                .filter(a -> a.getIdentifier().equals(key)).findFirst().orElse(null);

        if (val == null)
            logger.error("未知的被动技能: " + key.asString());

        return val;
    }

    @Nullable
    public List<IMorphAbility> getAbilitiesFor(EntityType type)
    {
        return store.getAbilityFor(type);
    }

    @Nullable
    public List<IMorphAbility> getAbilitiesFor(String id)
    {
        return store.getAbilityFor(id);
    }

    public void handle(Player player, DisguiseState state)
    {
        state.getAbilities().forEach(a -> a.handle(player, state));
    }
}
