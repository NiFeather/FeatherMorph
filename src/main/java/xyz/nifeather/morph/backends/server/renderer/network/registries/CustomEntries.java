package xyz.nifeather.morph.backends.server.renderer.network.registries;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import org.jetbrains.annotations.ApiStatus;
import xyz.nifeather.morph.misc.DisguiseEquipment;

import java.util.UUID;

public class CustomEntries
{
    /**
     * This should always present for player disguise!
     */
    public static final CustomEntry<GameProfile> PROFILE = CustomEntry.of("profile", new GameProfile(UUID.randomUUID(), "sample")).doRequireNonNull();

    public static final CustomEntry<String> DISGUISE_NAME = CustomEntry.of("disguise_name", "").doRequireNonNull();

    public static final CustomEntry<DisguiseEquipment> EQUIPMENT = CustomEntry.of("equip", new DisguiseEquipment());
    public static final CustomEntry<Boolean> DISPLAY_FAKE_EQUIPMENT = CustomEntry.of("display_fake_equip", false);

    @ApiStatus.Internal
    public static final CustomEntry<Boolean> PROFILE_LISTED = CustomEntry.of("profile_listed", false);

    /**
     * @deprecated UUID for player disguise now is {@link CustomEntries#SPAWN_ID}
     */
    @Deprecated(since = "1.2.6", forRemoval = true)
    public static final CustomEntry<UUID> TABLIST_UUID = CustomEntry.of("tablist_uuid", Util.NIL_UUID);

    public static final CustomEntry<Boolean> WARDEN_CHARGING_ATTACK = CustomEntry.of("warden_charging_attack", false);

    public static final CustomEntry<Boolean> IS_AGGRESSIVE = CustomEntry.of("is_aggressive", false);

    /**
     * Animation played upon player attack (on player main hand swing)
     */
    public static final CustomEntry<Boolean> ATTACK_ANIMATION = CustomEntry.of("attack_animation", false);

    public static final CustomEntry<Integer> SLIME_SIZE_REAL = CustomEntry.of("slime_size_real", 0);

    public static final CustomEntry<String> ANIMATION = CustomEntry.of("animation", "morph:unknown");

    public static final CustomEntry<UUID> SPAWN_UUID = CustomEntry.of("spawn_uuid", Util.NIL_UUID);
    public static final CustomEntry<Integer> SPAWN_ID = CustomEntry.of("spawn_id", -1);

    public static final CustomEntry<Boolean> VANISHED = CustomEntry.of("vanished", false);

    public static final CustomEntry<Boolean> WARDEN_VANISHED = CustomEntry.of("warden_vanished", false);

    public static final CustomEntry<Boolean> ARMOR_STAND_SMALL = CustomEntry.of("armorstand_is_small", false);
    public static final CustomEntry<Boolean> ARMOR_STAND_NO_BASE_PLATE = CustomEntry.of("armorstand_no_baseplate", false);
    public static final CustomEntry<Boolean> ARMOR_STAND_SHOW_ARMS = CustomEntry.of("armorstand_show_arms", false);

    public static final CustomEntry<Boolean> DONT_INCLUDE_PACKET_IDENTIFIER = CustomEntry.of("dont_include_packet_identifier", false);

    /**
     * "Overlayed" means that this value is not stored in the watcher's registry,
     *  instead it will be generated on call.
     */
    public static final CustomEntry<Float> OVERLAYED_YAW = CustomEntry.of("overlayed_yaw", 0f);

    /**
     * "Overlayed" means that this value is not stored in the watcher's registry,
     *  instead it will be generated on call.
     */
    public static final CustomEntry<Float> OVERLAYED_PITCH = CustomEntry.of("overlayed_pitch", 0f);
}
