package xiamomc.morph.abilities;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseState;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityHandler extends MorphPluginObject
{
    private final Map<AbilityFlag, Set<EntityType>> typeAbilityMap = new ConcurrentHashMap<>();

    public AbilityHandler()
    {
        //初始化Map
        for (var f : AbilityFlag.values())
            typeAbilityMap.put(f, EnumSet.noneOf(EntityType.class));
    }

    public void registerAbility(EntityType type, EnumSet<AbilityFlag> flags)
    {
        flags.forEach(f -> this.registerAbility(type, f));
    }

    public void registerAbility(Set<EntityType> types, AbilityFlag flag)
    {
        types.forEach(t -> this.registerAbility(t, flag));
    }

    public void registerAbility(EntityType type, AbilityFlag flag)
    {
        var targetList = typeAbilityMap.get(flag);

        if (targetList.contains(type)) return;

        targetList.add(type);
    }

    @Nullable
    public EnumSet<AbilityFlag> getFlagsFor(EntityType type)
    {
        var list = EnumSet.noneOf(AbilityFlag.class);

        typeAbilityMap.forEach((f, t) ->
        {
            if (t.contains(type)) list.add(f);
        });

        return list;
    }

    private final PotionEffect waterBreathEffect = new PotionEffect(PotionEffectType.WATER_BREATHING, 20, 0);
    private final PotionEffect conduitEffect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 20, 0);
    private final PotionEffect nightVisionEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0);
    private final PotionEffect fireResistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 0);
    private final PotionEffect jumpBoostEffect = new PotionEffect(PotionEffectType.JUMP, 5, 1);
    private final PotionEffect jumpBoostEffectSmall = new PotionEffect(PotionEffectType.JUMP, 5, 0);
    private final PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 5, 2);
    private final PotionEffect featherFallingEffect = new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 0);

    public void handle(Player player, DisguiseState state)
    {
        var playerLocation = player.getLocation();

        if (state.isAbilityFlagSet(AbilityFlag.CAN_BREATHE_UNDER_WATER) && player.isInWaterOrRainOrBubbleColumn())
        {
            player.addPotionEffect(conduitEffect);
            player.addPotionEffect(waterBreathEffect);
        }

        if (state.isAbilityFlagSet(AbilityFlag.HAS_FIRE_RESISTANCE))
        {
            player.addPotionEffect(fireResistance);
        }

        if (state.isAbilityFlagSet(AbilityFlag.TAKES_DAMAGE_FROM_WATER) && player.isInWaterOrRainOrBubbleColumn())
        {
            player.damage(1);
        }

        if (state.isAbilityFlagSet(AbilityFlag.BURNS_UNDER_SUN)
                && player.getWorld().getEnvironment().equals(World.Environment.NORMAL)
                && player.getEquipment().getHelmet() == null
                && player.getWorld().isDayTime()
                && player.getWorld().isClearWeather()
                && !player.isInWaterOrRainOrBubbleColumn()
                && playerLocation.getBlock().getLightFromSky() == 15)
        {
            player.setFireTicks(200);
        }

        if (state.isAbilityFlagSet(AbilityFlag.HAS_JUMP_BOOST))
            player.addPotionEffect(jumpBoostEffect);
        else if (state.isAbilityFlagSet(AbilityFlag.HAS_SMALL_JUMP_BOOST))
            player.addPotionEffect(jumpBoostEffectSmall);

        if (state.isAbilityFlagSet(AbilityFlag.HAS_SPEED_BOOST))
            player.addPotionEffect(speedEffect);

        if (state.isAbilityFlagSet(AbilityFlag.ALWAYS_NIGHT_VISION))
            player.addPotionEffect(nightVisionEffect);

        if (state.isAbilityFlagSet(AbilityFlag.HAS_FEATHER_FALLING))
            player.addPotionEffect(featherFallingEffect);

        if (state.isAbilityFlagSet(AbilityFlag.SNOWY))
        {
            var block = playerLocation.getBlock();

            if (block.getType().isAir()
                    && block.canPlace(Material.SNOW.createBlockData())
                    && block.getTemperature() <= 0.95)
            {
                block.setType(Material.SNOW);
            }

            player.setFreezeTicks(0);

            if (playerLocation.getBlock().getTemperature() > 1.0)
                player.setFireTicks(40);
        }
    }
}
