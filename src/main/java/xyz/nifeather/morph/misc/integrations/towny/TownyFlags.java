package xyz.nifeather.morph.misc.integrations.towny;

import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class TownyFlags
{
    //public static final BooleanDataField ALLOW_FLIGHT_IN_TOWN_MASTERTOGGLE = new BooleanDataField("feathermorph_allow_flight", true);

    public static final BooleanDataField ALLOW_OUTSIDERS_FLIGHT = new BooleanDataField("feathermorph_allow_outsiders_flight", true);
    public static final BooleanDataField ALLOW_OUTSIDERS_USE_SKILL = new BooleanDataField("feathermorph_allow_outsiders_use_skill", false);

    @ApiStatus.Internal
    public static final List<BooleanDataField> FLAGS_FOR_INIT = List.of(
            ALLOW_OUTSIDERS_FLIGHT,
            ALLOW_OUTSIDERS_USE_SKILL
    );
}
