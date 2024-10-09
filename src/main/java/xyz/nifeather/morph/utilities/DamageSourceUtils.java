package xyz.nifeather.morph.utilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DamageSourceUtils
{
    public static NotScalableDamageSource toNotScalable(DamageSource source)
    {
        return new NotScalableDamageSource(source);
    }

    public static class NotScalableDamageSource extends DamageSource
    {
        public NotScalableDamageSource(DamageSource source)
        {
            this(source.typeHolder(), source.getDirectEntity(), source.getEntity());
        }

        public NotScalableDamageSource(Holder<DamageType> type, @Nullable Entity source, @Nullable Entity attacker)
        {
            super(type, source, attacker);
        }

        public NotScalableDamageSource(Holder<DamageType> type, Vec3 position)
        {
            super(type, position);
        }

        public NotScalableDamageSource(Holder<DamageType> type, @Nullable Entity attacker)
        {
            super(type, attacker);
        }

        public NotScalableDamageSource(Holder<DamageType> type)
        {
            super(type);
        }

        @Override
        public boolean scalesWithDifficulty()
        {
            return false;
        }

        public final List<TagKey<DamageType>> tags = new ObjectArrayList<>();

        @Override
        public boolean is(@NotNull TagKey<DamageType> tag)
        {
            return super.is(tag) || tags.contains(tag);
        }

        private boolean noSourceLocation;

        @Nullable
        @Override
        public Vec3 getSourcePosition()
        {
            return noSourceLocation ? null : super.getSourcePosition();
        }

        //region Utilities

        public NotScalableDamageSource noSourceLocation()
        {
            this.noSourceLocation = true;

            return this;
        }

        public NotScalableDamageSource withTag(TagKey<DamageType> tag)
        {
            tags.add(tag);

            return this;
        }

        public NotScalableDamageSource bypassArmor()
        {
            tags.add(DamageTypeTags.BYPASSES_ARMOR);
            return this;
        }

        public NotScalableDamageSource bypassResistance()
        {
            tags.add(DamageTypeTags.BYPASSES_RESISTANCE);
            return this;
        }

        public NotScalableDamageSource bypassShield()
        {
            tags.add(DamageTypeTags.BYPASSES_SHIELD);
            return this;
        }

        public NotScalableDamageSource bypassEnchantments()
        {
            tags.add(DamageTypeTags.BYPASSES_ENCHANTMENTS);
            return this;
        }

        public NotScalableDamageSource bypassEverything()
        {
            this.bypassArmor().bypassShield()
                    .bypassResistance().bypassEnchantments();

            return this;
        }

        //endregion
    }
}
