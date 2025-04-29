package xyz.nifeather.morph.network.server.handlers;

import xyz.nifeather.morph.commands.subcommands.request.AcceptSubCommand;
import xyz.nifeather.morph.network.commands.C2S.*;
import xyz.nifeather.morph.network.commands.S2C.*;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CAddAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CClearAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CRemoveAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CSyncAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.*;
import xyz.nifeather.morph.network.commands.S2C.query.S2CQueryCommand;
import xyz.nifeather.morph.network.commands.S2C.set.*;
import xyz.nifeather.morph.network.server.ServerSetEquipCommand;
import xyz.nifeather.morph.skills.impl.ExplodeMorphSkill;
import xyz.nifeather.netherite.network.commands.C2S.*;
import xyz.nifeather.netherite.network.commands.S2C.*;
import xyz.nifeather.netherite.network.commands.S2C.clientrender.*;
import xyz.nifeather.netherite.network.commands.S2C.map.NetheriteS2CMapClearCommand;
import xyz.nifeather.netherite.network.commands.S2C.map.NetheriteS2CMapCommand;
import xyz.nifeather.netherite.network.commands.S2C.map.NetheriteS2CMapRemoveCommand;
import xyz.nifeather.netherite.network.commands.S2C.map.NetheriteS2CPartialMapCommand;
import xyz.nifeather.netherite.network.commands.S2C.query.NetheriteQueryType;
import xyz.nifeather.netherite.network.commands.S2C.query.NetheriteS2CQueryCommand;
import xyz.nifeather.netherite.network.commands.S2C.set.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LegacyCommandHandler
{
    public LegacyCommandHandler()
    {
        registerFromNetheriteToModernMethods();
        registerFromModernToNetherite();
    }

    //region Netherite to Modern

    private final Map<String, Function<NetheriteC2SCommand<?>, AbstractC2SCommand<?>>> fromNetheriteToModernConverts = new ConcurrentHashMap<>();

    private <X extends NetheriteC2SCommand<?>> LegacyCommandHandler registerNetheriteToModern(String cmdName,
                                                                                              Class<X> expectedClass,
                                                                                              Function<X, AbstractC2SCommand<?>> method)
    {
        Function<NetheriteC2SCommand<?>, AbstractC2SCommand<?>> mth = netheriteCommand ->
        {
            if (!expectedClass.isInstance(netheriteCommand))
            {
                throw new IllegalArgumentException("Input command '%s' is not expected type '%s'"
                        .formatted(netheriteCommand.getClass().getSimpleName(), expectedClass.getSimpleName()));
            }

            return method.apply((X) netheriteCommand);
        };

        fromNetheriteToModernConverts.put(cmdName, mth);

        return this;
    }

    protected void registerFromNetheriteToModernMethods()
    {
        this.registerNetheriteToModern(NetheriteC2SCommandNames.Animation, NetheriteC2SAnimationCommand.class, cmd ->
        {
            var animId = cmd.getAnimationId();
            return new C2SAnimationCommand(animId);
        }).registerNetheriteToModern(NetheriteC2SCommandNames.Initial, NetheriteC2SInitialCommand.class, cmd ->
        {
            return new C2SRequestInitialCommand();
        }).registerNetheriteToModern(NetheriteC2SCommandNames.Morph, NetheriteC2SMorphCommand.class, cmd ->
        {
            var id = cmd.getArgumentAt(0, "");
            return new C2SMorphCommand(id);
        }).registerNetheriteToModern(NetheriteC2SCommandNames.Option, NetheriteC2SOptionCommand.class, cmd ->
        {
            var option = cmd.getOption();
            var value = cmd.getValue();

            C2SSetSingleOptionCommand.ClientOptionEnum optionEnum = switch (option)
            {
                case CLIENTVIEW -> C2SSetSingleOptionCommand.ClientOptionEnum.CLIENTVIEW;
                case HUD -> C2SSetSingleOptionCommand.ClientOptionEnum.HUD;
            };

            return new C2SSetSingleOptionCommand(optionEnum, value);
        }).registerNetheriteToModern(NetheriteC2SCommandNames.Request, NetheriteC2SRequestCommand.class, cmd ->
        {
            C2SRequestCommand.Decision decision = switch (cmd.decision)
            {
                case ACCEPT -> C2SRequestCommand.Decision.ACCEPT;
                case DENY -> C2SRequestCommand.Decision.DENY;
                case UNKNOWN -> C2SRequestCommand.Decision.UNKNOWN;
            };

            return new C2SRequestCommand(decision, cmd.targetRequestName);
        }).registerNetheriteToModern(NetheriteC2SCommandNames.Skill, NetheriteC2SSkillCommand.class, cmd ->
        {
            return new C2SActivateSkillCommand();
        }).registerNetheriteToModern(NetheriteC2SCommandNames.ToggleSelf, NetheriteC2SToggleSelfCommand.class, cmd ->
        {
            C2SToggleSelfCommand.SelfViewMode mode = switch (cmd.getSelfViewMode())
            {
                case ON -> C2SToggleSelfCommand.SelfViewMode.ON;
                case OFF -> C2SToggleSelfCommand.SelfViewMode.OFF;
                case CLIENT_ON -> C2SToggleSelfCommand.SelfViewMode.CLIENT_ON;
                case CLIENT_OFF -> C2SToggleSelfCommand.SelfViewMode.CLIENT_OFF;
            };

            return new C2SToggleSelfCommand(mode);
        }).registerNetheriteToModern(NetheriteC2SCommandNames.Unmorph, NetheriteC2SUnmorphCommand.class, cmd ->
        {
            return new C2SUnmorphCommand();
        });
    }

    public AbstractC2SCommand<?> fromNetheriteCommand(NetheriteC2SCommand<?> netheriteCommand) throws RuntimeException
    {
        var convertMethod = fromNetheriteToModernConverts.getOrDefault(netheriteCommand.getBaseName(), null);

        if (convertMethod == null)
            throw new RuntimeException("No convert method found for input command %s".formatted(netheriteCommand.getBaseName()));

        return convertMethod.apply(netheriteCommand);
    }

    //endregion Netherite to Modern

    // CommandName <-> fromNetherite, toNetherite
    private final Map<String, Function<AbstractS2CCommand<?>, NetheriteS2CCommand<?>>> fromModernToNetheriteConverts = new ConcurrentHashMap<>();

    private <X extends AbstractS2CCommand<?>> LegacyCommandHandler registerModernToNetherite(String cmdName,
                                                                                                Class<X> expectedClass,
                                                                                                Function<X, NetheriteS2CCommand<?>> method)
    {
        Function<AbstractS2CCommand<?>, NetheriteS2CCommand<?>> mth = modernCommand ->
        {
            if (!expectedClass.isInstance(modernCommand))
            {
                throw new IllegalArgumentException("Input command '%s' is not expected type '%s'"
                        .formatted(modernCommand.getClass().getSimpleName(), expectedClass.getSimpleName()));
            }

            return method.apply((X) modernCommand);
        };

        fromModernToNetheriteConverts.put(cmdName, mth);

        return this;
    }

    protected void registerFromModernToNetherite()
    {
        this.registerModernToNetherite(S2CCommandNames.Current, S2CCurrentCommand.class, cmd ->
        {
            return new NetheriteS2CCurrentCommand(cmd.getDisguiseIdentifier());
        }).registerModernToNetherite(S2CCommandNames.ReAuth, S2CReAuthCommand.class, cmd ->
        {
            return new NetheriteS2CReAuthCommand();
        }).registerModernToNetherite(S2CCommandNames.UnAuth, S2CUnAuthCommand.class, cmd ->
        {
            return new NetheriteS2CUnAuthCommand();
        }).registerModernToNetherite(S2CCommandNames.SwapHands, S2CSwapCommand.class, cmd ->
        {
            return new NetheriteS2CSwapCommand();
        }).registerModernToNetherite(S2CCommandNames.Query, S2CQueryCommand.class, cmd ->
        {
            var type = cmd.queryType();
            var diff = cmd.getDiff();

            NetheriteQueryType netheriteType = switch (type)
            {
                case UNKNOWN -> NetheriteQueryType.UNKNOWN;
                case ADD -> NetheriteQueryType.ADD;
                case REMOVE -> NetheriteQueryType.REMOVE;
                case SET -> NetheriteQueryType.SET;
            };

            return new NetheriteS2CQueryCommand(netheriteType, diff.toArray(String[]::new));
        }).registerModernToNetherite(S2CCommandNames.Request, S2CRequestCommand.class, cmd ->
        {
            var requestType = cmd.type;
            var targetName = cmd.sourcePlayer;

            NetheriteS2CRequestCommand.NetheriteRequestType netheriteRequestType = switch (requestType)
            {
                case Unknown -> NetheriteS2CRequestCommand.NetheriteRequestType.Unknown;

                case NewRequest -> NetheriteS2CRequestCommand.NetheriteRequestType.NewRequest;

                case RequestSend -> NetheriteS2CRequestCommand.NetheriteRequestType.RequestSend;
                case RequestExpired -> NetheriteS2CRequestCommand.NetheriteRequestType.RequestExpired;
                case RequestExpiredOwner -> NetheriteS2CRequestCommand.NetheriteRequestType.RequestExpiredOwner;

                case RequestAccepted -> NetheriteS2CRequestCommand.NetheriteRequestType.RequestAccepted;
                case RequestDenied -> NetheriteS2CRequestCommand.NetheriteRequestType.RequestDenied;
            };

            return new NetheriteS2CRequestCommand(netheriteRequestType, targetName);
        }).registerModernToNetherite(S2CCommandNames.SetAggressive, S2CSetAggressiveCommand.class, cmd ->
        {
            return new NetheriteS2CSetAggressiveCommand(cmd.val);
        }).registerModernToNetherite(S2CCommandNames.SetFakeEquip, ServerSetEquipCommand.class, cmd ->
        {
            var modernSlot = cmd.getSlot();

            NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot netheriteSlot = switch (modernSlot)
            {
                case MAINHAND -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.MAINHAND;
                case OFF_HAND -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.OFF_HAND;

                case HELMET -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.HELMET;
                case CHESTPLATE -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.CHESTPLATE;
                case LEGGINGS -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.LEGGINGS;
                case BOOTS -> NetheriteS2CSetFakeEquipCommand.ProtocolEquipmentSlot.BOOTS;
            };

            return new LegacySetEquipCommand(cmd.getItemStack(), netheriteSlot);
        }).registerModernToNetherite(S2CCommandNames.SetProfile, S2CSetProfileCommand.class, cmd ->
        {
            return new NetheriteS2CSetProfileCommand(cmd.getProfileSNbt());
        }).registerModernToNetherite(S2CCommandNames.SetSelfViewIdentifier, S2CSetSelfViewIdentifierCommand.class, cmd ->
        {
            return new NetheriteS2CSetSelfViewIdentifierCommand(cmd.getIdentifier());
        }).registerModernToNetherite(S2CCommandNames.SetSkillCooldown, S2CSetSkillCooldownCommand.class, cmd ->
        {
            return new NetheriteS2CSetSkillCooldownCommand(cmd.val);
        }).registerModernToNetherite(S2CCommandNames.SetSNbt, S2CSetSNbtCommand.class, cmd ->
        {
            return new NetheriteS2CSetSNbtCommand(cmd.getSNbt());
        }).registerModernToNetherite(S2CCommandNames.SetSneaking, S2CSetSneakingCommand.class, cmd ->
        {
            return new NetheriteS2CSetSneakingCommand(cmd.sneaking);
        }).registerModernToNetherite(S2CCommandNames.SetSelfViewing, S2CSetSelfViewingStatusCommand.class, cmd ->
        {
            return new NetheriteS2CSetSelfViewingCommand(cmd.selfViewing());
        }).registerModernToNetherite(S2CCommandNames.SetModifyBoundingBox, S2CSetModifyBoundingBoxCommand.class, cmd ->
        {
            return new NetheriteS2CSetModifyBoundingBoxCommand(cmd.getModifyBoundingBox());
        }).registerModernToNetherite(S2CCommandNames.SetRevealing, S2CSetMobRevealingCommand.class, cmd ->
        {
            return new NetheriteS2CSetRevealingCommand(cmd.getValue());
        }).registerModernToNetherite(S2CCommandNames.SetAvailableAnimations, S2CSetAvailableAnimationsCommand.class, cmd ->
        {
            return new NetheriteS2CSetAvailableAnimationsCommand(cmd.getAvailableAnimations());
        }).registerModernToNetherite(S2CCommandNames.SetAnimationDisplayName, S2CSetAnimationDisplayNameCommand.class, cmd ->
        {
            return new NetheriteS2CSetAnimationDisplayNameCommand(cmd.getDisplayIdentifier());
        }).registerModernToNetherite(S2CCommandNames.SetDisplayingFakeEquip, S2CSetDisplayingFakeEquipCommand.class, cmd ->
        {
            return new NetheriteS2CSetDisplayingFakeEquipCommand(cmd.displaying);
        });

        // Admin reveal
        this.registerModernToNetherite(S2CCommandNames.SetReveal, S2CSyncAdminRevealCommand.class, cmd ->
        {
            return new NetheriteS2CMapCommand(cmd.getMap());
        }).registerModernToNetherite(S2CCommandNames.AddReveal, S2CAddAdminRevealCommand.class, cmd ->
        {
            return new NetheriteS2CPartialMapCommand(cmd.getMap());
        }).registerModernToNetherite(S2CCommandNames.RemoveReveal, S2CRemoveAdminRevealCommand.class, cmd ->
        {
            return new NetheriteS2CMapRemoveCommand(cmd.getTargetId());
        }).registerModernToNetherite(S2CCommandNames.ClearReveal, S2CClearAdminRevealCommand.class, cmd ->
        {
            return new NetheriteS2CMapClearCommand();
        });

        // Animation

        this.registerModernToNetherite(S2CCommandNames.Animation, S2CAnimationCommand.class, cmd ->
        {
            return new NetheriteS2CAnimationCommand(cmd.getAnimId());
        });

        // Client Renderer

        this.registerModernToNetherite(S2CCommandNames.CRSyncRender, S2CCRSyncRegisterCommand.class, cmd ->
        {
            return new NetheriteS2CRenderMapSyncCommand(cmd.getMap());
        }).registerModernToNetherite(S2CCommandNames.CRAdd, S2CCRRegisterCommand.class, cmd ->
        {
            return new NetheriteS2CRenderMapAddCommand(cmd.getPlayerNetworkId(), cmd.getMobId());
        }).registerModernToNetherite(S2CCommandNames.CRRemove, S2CCRUnregisterCommand.class, cmd ->
        {
            return new NetheriteS2CRenderMapRemoveCommand(cmd.getPlayerNetworkId());
        }).registerModernToNetherite(S2CCommandNames.CRClear, S2CCRClearCommand.class, cmd ->
        {
            return new NetheriteS2CRenderMapClearCommand();
        }).registerModernToNetherite(S2CCommandNames.CRMeta, S2CCRSetMetaCommand.class, cmd ->
        {
            var modernMeta = cmd.renderMeta;

            NetheriteS2CRenderMeta netheriteS2CRenderMeta = new NetheriteS2CRenderMeta(modernMeta.networkId);
            netheriteS2CRenderMeta.sNbt = modernMeta.sNbt;
            netheriteS2CRenderMeta.profileCompound = modernMeta.profileCompound;
            netheriteS2CRenderMeta.showOverridedEquipment = modernMeta.showOverridedEquipment;

            var modernEquipment = modernMeta.overridedEquipment;

            // 我们可能在未来修改新协议的东西，所以先这样？
            if (modernEquipment != null)
            {
                var netheriteEquipment = new NetheriteEquipment();

                netheriteEquipment.headId = modernEquipment.headId;
                netheriteEquipment.headNbt = modernEquipment.headNbt;

                netheriteEquipment.chestId = modernEquipment.chestId;
                netheriteEquipment.chestNbt = modernEquipment.chestNbt;

                netheriteEquipment.leggingId = modernEquipment.leggingId;
                netheriteEquipment.leggingNbt = modernEquipment.leggingNbt;

                netheriteEquipment.feetId = modernEquipment.feetId;
                netheriteEquipment.feetNbt = modernEquipment.feetNbt;

                netheriteEquipment.handId = modernEquipment.handId;
                netheriteEquipment.handNbt = modernEquipment.handNbt;

                netheriteEquipment.offhandId = modernEquipment.offhandId;
                netheriteEquipment.offhandNbt = modernEquipment.offhandNbt;

                netheriteS2CRenderMeta.overridedEquipment = netheriteEquipment;
            }

            return new NetheriteS2CRenderMapMetaCommand(netheriteS2CRenderMeta);
        });
    }

    public NetheriteS2CCommand<?> toNetheriteCommand(AbstractS2CCommand<?> modernCommand) throws RuntimeException
    {
        var convertMethod = fromModernToNetheriteConverts.getOrDefault(modernCommand.getBaseName(), null);

        if (convertMethod == null)
            throw new RuntimeException("No convert method found for input command %s".formatted(modernCommand.getBaseName()));

        return convertMethod.apply(modernCommand);
    }
}
