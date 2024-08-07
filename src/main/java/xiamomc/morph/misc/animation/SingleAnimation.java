package xiamomc.morph.misc.animation;

/**
 *
 * @param subId 此动画分段的ID（子ID）
 * @param duration 此动画的持续时间（游戏刻）
 * @param availableForClient 此ID是否可以向客户端同步
 */
public record SingleAnimation(String subId, int duration, boolean availableForClient)
{
}
