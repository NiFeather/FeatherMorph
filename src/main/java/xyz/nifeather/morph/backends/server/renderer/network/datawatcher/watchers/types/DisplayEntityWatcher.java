package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.util.Vector3f;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public abstract class DisplayEntityWatcher extends EntityWatcher
{
    public DisplayEntityWatcher(Player bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        switch (property.id())
        {
            case PropertyNames.DISPLAY_WIDTH ->
            {
                var width =  (Float) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.CULLING_BOUNDINGBOX_WIDTH, width);
            }

            case PropertyNames.DISPLAY_HEIGHT ->
            {
                var height = (Float) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.CULLING_BOUNDINGBOX_HEIGHT, height);
            }

            case PropertyNames.DISPLAY_SCALE ->
            {
                var jomlVector = (org.joml.Vector3f) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.SCALE, new Vector3f(jomlVector.x(), jomlVector.y(), jomlVector.z()));
            }

            case PropertyNames.DISPLAY_GLOW_COLOR ->
            {
                var color = (Color) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.GLOW_COLOR_OVERRIDE, color.asARGB());
            }

            case PropertyNames.DISPLAY_SHADOW_RADIUS ->
            {
                var shadowRadius = (Float) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.SHADOW_RANGE, shadowRadius);
            }

            case PropertyNames.DISPLAY_SHADOW_STRENGTH ->
            {
                var shadowStrength = (Float) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.SHADOW_STRENGTH, shadowStrength);
            }

            case PropertyNames.DISPLAY_LIGHT_OVERRIDE ->
            {
                var lightOverride = (Integer) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.BRIGHTNESS_OVERRIDE, lightOverride);
            }

            case PropertyNames.DISPLAY_TRANSLATION ->
            {
                var jomlTranslation = (org.joml.Vector3f) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.TRANSLATION, new Vector3f(jomlTranslation.x(), jomlTranslation.y(), jomlTranslation.z()));
            }

            case PropertyNames.DISPLAY_BILLBOARD ->
            {
                var bukkitBillboard = (Display.Billboard) value;
                this.writePersistent(ValueIndex.DISPLAY_ENTITY.BILLBOARD_RENDER_CONSTRAINTS, (byte) bukkitBillboard.ordinal());
            }
        }

        super.onPropertyWrite(property, value);
    }
}
