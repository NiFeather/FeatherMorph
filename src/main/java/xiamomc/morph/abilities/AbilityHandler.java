package xiamomc.morph.abilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbilityHandler extends MorphPluginObject
{
    private final List<IMorphAbility> registedAbilities = new ObjectArrayList<>();

    @Resolved
    private SkillConfigurationStore store;

    /**
     * 注册一个被动技能
     *
     * @param ability 技能ID
     * @return 操作是否成功
     */
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

    /**
     * 注册一批被动技能
     *
     * @param abilities ID列表
     * @return 操作是否成功
     */
    public boolean registerAbilities(List<IMorphAbility> abilities)
    {
        var success = new AtomicBoolean(false);

        abilities.forEach(a ->
        {
            if (!registerAbility(a))
                success.set(false);
        });

        return success.get();
    }

    @Initializer
    private void load()
    {
        registerAbilities(ObjectList.of(
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
                new TakesDamageFromWaterAbility(),
                new WardenLessAwareAbility(),
                new ChatOverrideAbility()
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
