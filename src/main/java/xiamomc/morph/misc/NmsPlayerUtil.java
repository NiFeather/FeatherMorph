package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang.NullArgumentException;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class NmsPlayerUtil
{
    public static boolean setLastDamageSource(Player nmsPlayer, DamageSource damageSource)
    {
        Field lastDamageSourceField;
        Field lastDamageTimeField;

        try
        {
            var list = new ObjectArrayList<Field>();
            var fields = getAllFields(list, nmsPlayer.getClass());

            //cg -> lastDamageSource
            var lastDamageSource = "cg";
            lastDamageSourceField = fields.stream()
                    .filter(f -> f.getName().equals(lastDamageSource) && f.getType().getName().contains("DamageSource"))
                    .findFirst().orElse(null);

            var lastDamageStamp = "ch";
            lastDamageTimeField = fields.stream()
                    .filter(f -> f.getName().equals(lastDamageStamp) && f.getType().getName().contains("long"))
                    .findFirst().orElse(null);

            if (lastDamageSourceField == null) throw new NullArgumentException("lastDamageSourceField");
            if (lastDamageTimeField == null) throw new NullArgumentException("lastDamageTimeField");



            lastDamageSourceField.setAccessible(true);
            lastDamageTimeField.setAccessible(true);

            lastDamageSourceField.set(nmsPlayer, damageSource);
            lastDamageTimeField.set(nmsPlayer, nmsPlayer.getLevel().getGameTime());
        }
        catch (Throwable ignored)
        {
            ignored.printStackTrace();
            return false;
        }

        return true;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type)
    {
        LoggerFactory.getLogger("T " + type);

        if (type == LivingEntity.class)
            fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null)
        {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
