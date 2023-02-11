package xiamomc.morph.abilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.impl.*;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.SkillConfiguration;
import xiamomc.morph.storage.skill.SkillConfigurationStore;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbilityHandler extends MorphPluginObject
{
    private final List<IMorphAbility<?>> registedAbilities = new ObjectArrayList<>();

    @Resolved
    private SkillConfigurationStore store;

    private boolean initalizeDone;

    /**
     * 注册一个被动技能
     *
     * @param ability 技能ID
     * @return 操作是否成功
     */
    public boolean registerAbility(IMorphAbility<?> ability)
    {
        logger.info("注册被动技能：" + ability.getIdentifier().asString());

        if (registedAbilities.stream().anyMatch(a -> a.getIdentifier().equals(ability.getIdentifier())))
        {
            logger.error("已经注册过一个" + ability.getIdentifier().asString() + "的被动技能了");
            return false;
        }

        registedAbilities.add(ability);

        //SkillConfigurationStore只会在重载时给已经注册的被动添加设置
        //所有在重载/初始化完成后加入的技能都需要我们手动查询
        if (initalizeDone)
        {
            //添加设置
            store.getConfiguredSkills().forEach((c, s) ->
            {
                if (!ability.setOptionGeneric(c.getIdentifier(), c.getAbilityOptions(ability)))
                {
                    logger.warn("无法为" + c.getIdentifier() + " -> " + ability.getIdentifier() + "添加技能设置");
                }
            });

            //添加到map中所有符合条件的技能配置里
            var id = ability.getIdentifier().asString();
            var matchingConfigs = configToAbilitiesMap.entrySet()
                    .stream().filter(e -> e.getKey().getAbilitiyIdentifiers().contains(id)).toList();

            matchingConfigs.forEach(c ->
            {
                var list = configToAbilitiesMap.get(c.getKey());

                if (!list.contains(ability)) list.add(ability);
            });
        }

        Bukkit.getPluginManager().registerEvents(ability, plugin);
        return true;
    }

    /**
     * 获取所有已注册的被动
     *
     * @return 被动技能列表
     */
    public List<IMorphAbility<?>> getRegistedAbilities()
    {
        return new ObjectArrayList<>(registedAbilities);
    }

    /**
     * 注册一批被动技能
     *
     * @param abilities ID列表
     * @return 操作是否成功
     */
    public boolean registerAbilities(List<IMorphAbility<?>> abilities)
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
                new ChatOverrideAbility(),
                new BossbarAbility(),
                new NoSweetBushDamageAbility(),
                new AttributeModifyingAbility()
        ));

        initalizeDone = true;
    }

    @Nullable
    public IMorphAbility<?> getAbility(@Nullable NamespacedKey key)
    {
        if (key == null) return null;

        var val = registedAbilities.stream()
                .filter(a -> a.getIdentifier().equals(key)).findFirst().orElse(null);

        if (val == null)
            logger.warn("未知的被动技能: " + key.asString());

        return val;
    }

    private final Map<SkillConfiguration, List<IMorphAbility<?>>> configToAbilitiesMap = new Object2ObjectOpenHashMap<>();

    /**
     * 为某个伪装ID获取被动技能
     *
     * @param id 伪装ID
     * @return 被动技能列表
     */
    public List<IMorphAbility<?>> getAbilitiesFor(String id)
    {
        return this.getAbilitiesFor(id, false);
    }

    public List<IMorphAbility<?>> getAbilitiesFor(String id, boolean noFallback)
    {
        var entry = configToAbilitiesMap.entrySet().stream()
                .filter(s -> s.getKey().getIdentifier().equals(id)).findFirst().orElse(null);

        if (entry != null)
        {
            return entry.getValue();
        }
        else if (!noFallback)
        {
            var idSpilt = id.split(":", 2);
            if (idSpilt.length < 1) return null;

            var idNew = idSpilt[0] + ":" + MorphManager.disguiseFallbackName;

            return getAbilitiesFor(idNew, true);
        }

        return null;
    }

    public void setAbilities(SkillConfiguration configuration, List<IMorphAbility<?>> abilities)
    {
        configToAbilitiesMap.put(configuration, abilities);
    }

    public void clearAbilities()
    {
        configToAbilitiesMap.clear();
        registedAbilities.forEach(IMorphAbility::clearOptions);
    }

    public void handle(Player player, DisguiseState state)
    {
        state.getAbilities().forEach(a -> a.handle(player, state));
    }
}
