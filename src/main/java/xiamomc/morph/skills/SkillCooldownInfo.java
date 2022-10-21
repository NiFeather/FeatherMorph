package xiamomc.morph.skills;

public class SkillCooldownInfo
{
    private final IMorphSkill skill;

    /**
     * 获取对应的技能
     * @return 技能
     */
    public IMorphSkill getSkill()
    {
        return skill;
    }

    private long cooldown;

    private long lastInvoke = Long.MIN_VALUE;

    private boolean invokedOnce;

    /**
     * 获取上次调用时间
     * @return 上次调用的时间
     */
    public long getLastInvoke()
    {
        return lastInvoke;
    }

    /**
     * 设置上次调用时间
     * @param val 上次调用的时间
     */
    public void setLastInvoke(Long val)
    {
        lastInvoke = val;
        invokedOnce = true;
    }

    public boolean skillInvokedOnce()
    {
        return invokedOnce;
    }

    /**
     * 获取冷却时间
     * @return 冷却时间
     */
    public long getCooldown()
    {
        return cooldown;
    }

    /**
     * 设置冷却时间
     * @param val 冷却时间
     */
    public void setCooldown(long val)
    {
        cooldown = val;
    }

    public SkillCooldownInfo(IMorphSkill skill)
    {
        this.skill = skill;
    }
}
