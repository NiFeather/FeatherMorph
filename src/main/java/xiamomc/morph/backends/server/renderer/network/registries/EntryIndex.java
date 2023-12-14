package xiamomc.morph.backends.server.renderer.network.registries;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.misc.DisguiseEquipment;

import java.util.UUID;

public class EntryIndex
{
    public static final RegistryKey<GameProfile> PROFILE = RegistryKey.of("profile", new GameProfile(UUID.randomUUID(), "sample"));
    public static final RegistryKey<EntityType> ENTITY_TYPE = RegistryKey.of("entity_type", EntityType.UNKNOWN).doRequireNonNull();
    public static final RegistryKey<SingleWatcher> BINDING_WATCHER = new RegistryKey<>("binding_watcher", SingleWatcher.class).doRequireNonNull();
    public static final RegistryKey<String> CUSTOM_NAME = RegistryKey.of("custom_name", "").doRequireNonNull();
    public static final RegistryKey<DisguiseEquipment> EQUIPMENT = RegistryKey.of("equip", new DisguiseEquipment());
}
