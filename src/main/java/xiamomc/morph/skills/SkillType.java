package xiamomc.morph.skills;

public enum SkillType
{
    LAUNCH_PROJECTIVE("launch_projective"),
    TELEPORT("teleport"),
    EVOKER("summon_fangs_or_vex"),
    INVENTORY("fake_inventory_display"),
    APPLY_EFFECT("apply_effect"),
    EXPLODE("explode"),
    NONE("none");

    private final String id;

    private SkillType(String id)
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return this.id;
    }
}
