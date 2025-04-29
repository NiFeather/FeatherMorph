package xyz.nifeather.netherite;

import xyz.nifeather.netherite.network.commands.C2S.*;

public class HandleType<TCommand>
{
    public final String handleTypeName;

    public HandleType(String name)
    {
        this.handleTypeName = name;
    }

    public static final HandleType<NetheriteC2SInitialCommand> REQUEST_INITIAL = new HandleType<>("request_initial");
    public static final HandleType<NetheriteC2SMorphCommand> REQUEST_MORPH = new HandleType<>("request_morph");
    public static final HandleType<NetheriteC2SOptionCommand> SET_SINGLE_OPTION = new HandleType<>("set_single_option");
    public static final HandleType<NetheriteC2SSkillCommand> ACTIVATE_SKILL = new HandleType<>("activate_skill");
    public static final HandleType<NetheriteC2SToggleSelfCommand> TOGGLE_SELF_VIEW = new HandleType<>("toggle_self_view");
    public static final HandleType<NetheriteC2SUnmorphCommand> REQUEST_UNMORPH = new HandleType<>("request_unmorph");
    public static final HandleType<NetheriteC2SRequestCommand> PROCESS_EXCHANGE_REQUEST = new HandleType<>("process_exchange_request");
    public static final HandleType<NetheriteC2SAnimationCommand> PLAY_ANIMATION = new HandleType<>("play_animation");
}
