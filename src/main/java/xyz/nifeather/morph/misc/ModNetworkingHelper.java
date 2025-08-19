package xyz.nifeather.morph.misc;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.WrapperProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CAddAdminRevealCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRSetMetaCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CRenderMeta;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.utilities.MapMetaUtils;
import xyz.nifeather.morph.utilities.NbtUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.HashMap;

public class ModNetworkingHelper extends MorphPluginObject
{
    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    /**
     * 生成用于橙字显示的部分map(mapp)指令
     * @param diff 用于生成的伪装状态
     */
    public S2CAddAdminRevealCommand genPartialMapCommand(DisguiseState... diff)
    {
        var map = new HashMap<Integer, String>();
        for (DisguiseState disguiseState : diff)
        {
            var player = disguiseState.getPlayer();
            map.put(player.getEntityId(), player.getName());
        }

        return new S2CAddAdminRevealCommand(map);
    }

    /**
     * 将某一客户端指令发送给所有拥有橙字显示权限的玩家
     * @param cmd 目标指令
     */
    public void sendCommandToRevealablePlayers(AbstractS2CCommand<?> cmd)
    {
        var target = featherMorph().getPlatform().onlinePlayers().stream()
                .filter(p -> p.hasPermission(CommonPermissions.DISGUISE_REVEALING))
                .toList();

        target.forEach(p -> clientHandler.sendCommand(p, cmd));
    }

    public void sendCommandToAllPlayers(AbstractS2CCommand<?> cmd)
    {
        featherMorph().getPlatform().onlinePlayers().forEach(p -> clientHandler.sendCommand(p, cmd));
    }

    public NetworkDisguiseStateRecord prepareMeta(int networkId)
    {
        return new NetworkDisguiseStateRecord(networkId, this);
    }

    public NetworkDisguiseStateRecord prepareMeta(Entity entity)
    {
        return prepareMeta(entity.getEntityId());
    }

    public static class NetworkDisguiseStateRecord
    {
        public NetworkDisguiseStateRecord(int networkId, ModNetworkingHelper bindingHelper)
        {
            this.renderMeta = new S2CRenderMeta(networkId);
            this.bindingHelper = bindingHelper;
        }

        private final ModNetworkingHelper bindingHelper;

        private final S2CRenderMeta renderMeta;

        public NetworkDisguiseStateRecord setSNbt(String snbt)
        {
            renderMeta.sNbt = snbt;
            return this;
        }

        public NetworkDisguiseStateRecord setOverridedEquip(EntityEquipment equipment)
        {
            renderMeta.overridedEquipment = MapMetaUtils.toPacketEquipment(equipment);
            return this;
        }

        public NetworkDisguiseStateRecord setProfileCompound(String compoundString)
        {
            renderMeta.profileCompound = compoundString;
            return this;
        }

        public NetworkDisguiseStateRecord setDisguiseEquipmentShown(boolean newValue)
        {
            renderMeta.showOverridedEquipment = newValue;
            return this;
        }

        /**
         * 这里是准备发送给客户端的Meta信息
         * @param state
         * @return
         */
        public NetworkDisguiseStateRecord forDisguiseState(DisguiseState state)
        {
            return forWrapper(state.getDisguiseWrapper());
        }

        public NetworkDisguiseStateRecord forWrapper(DisguiseWrapper<?> wrapper)
        {
            var profile = wrapper.readProperty(WrapperProperties.PROFILE).orElse(new GameProfile(Uuids.NIL_UUID, "NIL"));
            var profileStr = NbtUtils.getCompoundString(NbtUtils.toCompoundTag(profile));

            this.setProfileCompound(profileStr)
                    //.setSNbt(NbtUtils.getCompoundString(wrapper.getCompound()))
                    .setDisguiseEquipmentShown(wrapper.readProperty(OffTreeProperties.DISPLAY_FAKE_EQUIPMENT))
                    .setOverridedEquip(wrapper.getFakeEquipments());

            return this;
        }

        public S2CCRSetMetaCommand build()
        {
            return new S2CCRSetMetaCommand(renderMeta);
        }

        public void send()
        {
            bindingHelper.sendCommandToAllPlayers(build());
        }
    }
}
