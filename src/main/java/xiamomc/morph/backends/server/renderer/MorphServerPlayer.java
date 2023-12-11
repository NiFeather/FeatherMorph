package xiamomc.morph.backends.server.renderer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class MorphServerPlayer extends ServerPlayer
{
    public MorphServerPlayer(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions)
    {
        super(server, world, profile, clientOptions);
    }
}
