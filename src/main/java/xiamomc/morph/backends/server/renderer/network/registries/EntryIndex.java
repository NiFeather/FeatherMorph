package xiamomc.morph.backends.server.renderer.network.registries;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import xiamomc.morph.misc.DisguiseEquipment;

import java.util.UUID;

public class EntryIndex
{
    public static final RegistryKey<GameProfile> PROFILE = RegistryKey.of("profile", new GameProfile(UUID.randomUUID(), "sample"));
    public static final RegistryKey<String> DISGUISE_NAME = RegistryKey.of("disguise_name", "").doRequireNonNull();

    public static final RegistryKey<DisguiseEquipment> EQUIPMENT = RegistryKey.of("equip", new DisguiseEquipment());
    public static final RegistryKey<Boolean> DISPLAY_FAKE_EQUIPMENT = RegistryKey.of("display_fake_equip", false);

    public static final RegistryKey<UUID> TABLIST_UUID = RegistryKey.of("tablist_uuid", Util.NIL_UUID);

    public static final RegistryKey<Boolean> WARDEN_CHARGING_ATTACK = RegistryKey.of("warden_charging_attack", false);
    public static final RegistryKey<Boolean> ATTACK_ANIMATION = RegistryKey.of("attack_animation", false);

    public static final RegistryKey<Integer> SLIME_SIZE_REAL = RegistryKey.of("slime_size_real", 0);
}
