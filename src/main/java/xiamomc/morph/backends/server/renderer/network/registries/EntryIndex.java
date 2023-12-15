package xiamomc.morph.backends.server.renderer.network.registries;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.misc.DisguiseEquipment;

import java.util.UUID;

public class EntryIndex
{
    public static final RegistryKey<GameProfile> PROFILE = RegistryKey.of("profile", new GameProfile(UUID.randomUUID(), "sample"));
    public static final RegistryKey<String> DISGUISE_NAME = RegistryKey.of("disguise_name", "").doRequireNonNull();

    public static final RegistryKey<DisguiseEquipment> EQUIPMENT = RegistryKey.of("equip", new DisguiseEquipment());
    public static final RegistryKey<Boolean> DISPLAY_FAKE_EQUIPMENT = RegistryKey.of("display_fake_equip", false);
}
