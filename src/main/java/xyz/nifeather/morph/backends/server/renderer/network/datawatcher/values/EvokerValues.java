package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class EvokerValues extends RaiderValues
{
    public final SingleValue<Byte> SPELL_STATUS;

    public EvokerValues()
    {
        SPELL_STATUS = createSingle("evokder_spell_status", (byte)0, EntityDataTypes.BYTE);

        registerSingle(SPELL_STATUS);
    }

    public enum SpellStatus
    {
        NONE((byte)0),
        SUMMON_VEX((byte)1),
        ATTACK((byte)2),
        WOLOLO((byte)3),
        DISAPPEAR((byte)4),
        BLINDNESS((byte)5)
        ;

        public final byte val;

        SpellStatus(byte val)
        {
            this.val = val;
        }
    }
}
