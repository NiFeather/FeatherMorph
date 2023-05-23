package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
//import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.damagesource.DamageType;
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
        public NotScalableDamageSource(DamageSource source, Entity entity, Entity directEntity)
        {
            super(source.msgId);

            this.entity = entity;
            this.directEntity = directEntity;
        }

        public NotScalableDamageSource(DamageSource source)
        {
            super(source.msgId);
            this.entity = source.getEntity();
            this.directEntity = source.getDirectEntity();

            //this(source.typeHolder(), source.getDirectEntity(), source.getEntity());
        }

        private Entity entity;

        private Entity directEntity;

        @Override
        public Entity getEntity()
        {
            return entity;
        }

        @Override
        public Entity getDirectEntity()
        {
            return directEntity;
        }

        @Override
        public boolean scalesWithDifficulty()
        {
            return false;
        }

        /*
        public final List<TagKey<DamageType>> tags = new ObjectArrayList<>();

        @Override
        public boolean is(@NotNull TagKey<DamageType> tag)
        {
            return super.is(tag) || tags.contains(tag);
        }
        */

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

        /*
        public NotScalableDamageSource withTag(TagKey<DamageType> tag)
        {
            tags.add(tag);

            return this;
        }
        */

        public NotScalableDamageSource bypassArmor()
        {
            super.bypassArmor();
            return this;
        }

        public NotScalableDamageSource bypassResistance()
        {
            //tags.add(DamageTypeTags.BYPASSES_RESISTANCE);
            return this;
        }

        public NotScalableDamageSource bypassShield()
        {
            //tags.add(DamageTypeTags.BYPASSES_SHIELD);
            return this;
        }

        public NotScalableDamageSource bypassEnchantments()
        {
            super.bypassEnchantments();
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
