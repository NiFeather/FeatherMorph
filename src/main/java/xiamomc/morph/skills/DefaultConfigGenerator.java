package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.skills.configurations.*;

import java.util.List;
import java.util.function.Consumer;

public class DefaultConfigGenerator
{
    private static SkillConfigurationContainer cachedContainer = null;

    private static void addConfiguration(List<SkillConfiguration> targetList, EntityType entityType, int cd, NamespacedKey key, @Nullable Consumer<SkillConfiguration> c)
    {
        var config = new SkillConfiguration(entityType, cd, key);

        if (c != null)
            c.accept(config);

        targetList.add(config);
    }

    private static void addConfiguration(List<SkillConfiguration> targetList, EntityType entityType, int cd, NamespacedKey key)
    {
        addConfiguration(targetList, entityType, cd, key, null);
    }

    public static SkillConfigurationContainer getDefaultConfiguration()
    {
        if (cachedContainer != null) return cachedContainer;

        var container = new SkillConfigurationContainer();
        var skills = container.configurations;

        //伪装物品
        addConfiguration(skills, EntityType.ARMOR_STAND, 20, SkillType.INVENTORY);
        addConfiguration(skills, EntityType.PLAYER, 20, SkillType.INVENTORY);

        //弹射物
        addConfiguration(skills, EntityType.BLAZE, 10, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.SMALL_FIREBALL, 1, "entity.blaze.shoot", 8)));

        addConfiguration(skills, EntityType.ENDER_DRAGON, 80, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.DRAGON_FIREBALL, 1, "entity.ender_dragon.shoot", 80)));

        addConfiguration(skills, EntityType.GHAST, 40, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.FIREBALL, 1, "entity.ghast.shoot", 35)));

        addConfiguration(skills, EntityType.LLAMA, 25, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8)));

        addConfiguration(skills, EntityType.TRADER_LLAMA, 25, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.LLAMA_SPIT, 1, "entity.llama.spit", 8)));

        addConfiguration(skills, EntityType.SHULKER, 40, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.SHULKER_BULLET, 0, "entity.shulker.shoot", 15, 15)));

        addConfiguration(skills, EntityType.SNOWMAN, 15, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.SNOWBALL, 1, "entity.snow_golem.shoot", 8)));

        addConfiguration(skills, EntityType.WITHER, 10, SkillType.LAUNCH_PROJECTIVE, c ->
                c.setProjectiveConfiguration(new ProjectiveConfiguration(EntityType.WITHER_SKULL, 1, "entity.wither.shoot", 24)));

        //药效
        addConfiguration(skills, EntityType.DOLPHIN, 180, SkillType.APPLY_EFFECT, c ->
                c.setEffectConfiguration(new EffectConfiguration(PotionEffectType.DOLPHINS_GRACE.getKey().asString(), 0, 180, true, false, null, 0, 9)));

        addConfiguration(skills, EntityType.ELDER_GUARDIAN, 1200, SkillType.APPLY_EFFECT, c ->
                c.setEffectConfiguration(new EffectConfiguration(PotionEffectType.SLOW_DIGGING.getKey().asString(), 2, 6000, true, true, "entity.elder_guardian.curse", 50, 50)));

        //其他
        addConfiguration(skills, EntityType.CREEPER, 80, SkillType.EXPLODE, c ->
                c.setExplosionConfiguration(new ExplosionConfiguration(true, 3, false)));

        addConfiguration(skills, EntityType.ENDERMAN, 40, SkillType.TELEPORT);
        addConfiguration(skills, EntityType.EVOKER, 100, SkillType.EVOKER);

        cachedContainer = container;
        return container;
    }
}
