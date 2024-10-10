package xyz.nifeather.morph.backends.server.renderer.network.registries;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import xyz.nifeather.morph.misc.DisguiseEquipment;

import java.util.UUID;

public class CustomEntries
{
    /**
     * This should always present for player disguise!
     */
    public static final CustomEntry<GameProfile> PROFILE = CustomEntry.of("profile", new GameProfile(UUID.randomUUID(), "sample")).doRequireNonNull();

    /**
     * Should profile listed in the tab list?
     * <b></b>
     * Also, 'TRUE' means the profile listing is also handled by external sources
     */
    public static final CustomEntry<Boolean> PROFILE_LISTED = CustomEntry.of("profile_listed", false);

    public static final CustomEntry<String> DISGUISE_NAME = CustomEntry.of("disguise_name", "").doRequireNonNull();

    public static final CustomEntry<DisguiseEquipment> EQUIPMENT = CustomEntry.of("equip", new DisguiseEquipment());
    public static final CustomEntry<Boolean> DISPLAY_FAKE_EQUIPMENT = CustomEntry.of("display_fake_equip", false);

    /**
     * @deprecated UUID for player disguise now is {@link CustomEntries#SPAWN_ID}
     */
    @Deprecated(since = "1.2.6", forRemoval = true)
    public static final CustomEntry<UUID> TABLIST_UUID = CustomEntry.of("tablist_uuid", Util.NIL_UUID);

    public static final CustomEntry<Boolean> WARDEN_CHARGING_ATTACK = CustomEntry.of("warden_charging_attack", false);
    public static final CustomEntry<Boolean> ATTACK_ANIMATION = CustomEntry.of("attack_animation", false);

    public static final CustomEntry<Integer> SLIME_SIZE_REAL = CustomEntry.of("slime_size_real", 0);

    public static final CustomEntry<String> ANIMATION = CustomEntry.of("animation", "morph:unknown");

    public static final CustomEntry<UUID> SPAWN_UUID = CustomEntry.of("spawn_uuid", Util.NIL_UUID);
    public static final CustomEntry<Integer> SPAWN_ID = CustomEntry.of("spawn_id", -1);

    public static final CustomEntry<Boolean> VANISHED = CustomEntry.of("vanished", false);
}
