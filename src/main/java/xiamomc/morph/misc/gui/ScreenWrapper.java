package xiamomc.morph.misc.gui;

import de.themoep.inventorygui.InventoryGui;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xiamomc.pluginbase.ScheduleInfo;

public class ScreenWrapper extends MorphPluginObject
{
    protected Bindable<Boolean> isDynamic = new Bindable<>(false);

    private final Player bindingPlayer;

    protected final String playerLocale;

    public static final Sound clickSound = Sound.sound().type(Key.key("ui.button.click")).volume(0.45f).build();
    public static final Sound openSound = Sound.sound().type(Key.key("entity.experience_orb.pickup")).volume(0.55f).build();

    protected Player getBindingPlayer()
    {
        return bindingPlayer;
    }

    protected InventoryGui guiInstance;

    private ScheduleInfo updateScheduleInfo;

    public ScreenWrapper(Player bindingPlayer)
    {
        this.bindingPlayer = bindingPlayer;
        this.playerLocale = MessageUtils.getLocale(bindingPlayer);

        isDynamic.onValueChanged((o, n) ->
        {
            if (n && isCurrent()) updateScheduleInfo = this.addSchedule(this::update);
            else if (this.updateScheduleInfo != null) this.updateScheduleInfo.cancel();
        });
    }

    protected boolean isCurrent()
    {
        if (guiInstance == null) return false;

        return guiInstance.equals(InventoryGui.getOpen(bindingPlayer));
    }

    protected void onUpdate()
    {
    }

    private void update()
    {
        if (plugin.getCurrentTick() % 10 != 0 && isDynamic.get())
        {
            this.addSchedule(this::update);
            return;
        }

        if (!isCurrent()) return;

        this.onUpdate();

        this.addSchedule(this::update);

        this.guiInstance.draw(bindingPlayer);
    }

    public void show()
    {
        if (this.guiInstance == null)
        {
            logger.error("Attempting to show a null GUI to the player! No continuing...");
            return;
        }

        this.guiInstance.show(bindingPlayer);

        if (isDynamic.get())
            updateScheduleInfo = this.addSchedule(this::update);
    }
}
