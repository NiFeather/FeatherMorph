package xiamomc.morph.transforms.easings.impl;

import xiamomc.morph.transforms.easings.Easing;
import xiamomc.morph.transforms.easings.IEasing;

public class EasingImpl implements IEasing
{
    private final Easing easing;

    public EasingImpl(Easing easing)
    {
        this.easing = easing;
    }

    private static final double elasticConst = (Math.PI * 2) / 3;
    private static final double elasticInOutConst = (Math.PI * 2) / 4.5;

    private static final double backConst1 = 1.70158;
    private static final double backConst2 = backConst1 + 1;
    private static final double backConst3 = backConst1 * 1.525;

    @Override
    public double apply(double progress)
    {
        return switch (easing)
        {
            default -> progress;

            case InSine -> 1 - Math.cos((progress * Math.PI) / 2);
            case OutSine -> Math.sin((progress * Math.PI) / 2);
            case InOutSine -> -(Math.cos(progress * Math.PI) - 1) / 2;

            case InCubic -> progress * progress * progress;
            case OutCubic -> 1 + (--progress * progress * progress);
            case InOutCubic -> progress < 0.5 ? 4 * progress * progress * progress : 1 - Math.pow(-2 * progress + 2, 3) / 2;

            case InQuint -> progress * progress * progress * progress * progress;
            case OutQuint -> 1 + (--progress * progress * progress * progress * progress);
            case InOutQuint -> progress < 0.5 ? 16 * progress * progress * progress * progress * progress : 1 - Math.pow(-2 * progress + 2, 5) / 2;

            case InCirc -> 1 - Math.sqrt(1 - progress * progress);
            case OutCirc -> Math.sqrt(1 - Math.pow(progress - 1, 2));
            case InOutCirc -> progress < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * progress, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * progress + 2, 2)) + 1) / 2;

            case InElastic -> progress == 0
                    ? 0
                    : progress == 1
                        ? 1
                        : -Math.pow(2, 10 * progress - 10) * Math.sin((progress * 10 - 10.75) * elasticConst);
            case OutElastic -> progress == 0
                    ? 0
                    : progress == 1
                        ? 1
                        : Math.pow(2, -10 * progress) * Math.sin((progress * 10 - 0.75) * elasticConst) + 1;
            case InOutElastic -> progress == 0
                    ? 0
                    : progress == 1
                        ? 1
                        : progress < 0.5
                            ? -(Math.pow(2, 20 * progress - 10) * Math.sin((20 * progress - 11.125) * elasticInOutConst)) / 2
                            : (Math.pow(2, -20 * progress + 10) * Math.sin((20 * progress - 11.125) * elasticConst)) / 2 + 1;

            case InQuad -> progress * progress;
            case OutQuad -> 1 - (1 - progress) * (1 - progress);
            case InOutQuad -> progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;

            case InQuart -> progress * progress * progress * progress;
            case OutQuart -> 1 - (--progress * progress * progress * progress);
            case InOutQuart -> progress < 0.5
                    ? 8 * progress * progress * progress * progress
                    : 1 - Math.pow(-2 * progress + 2, 4) / 2;

            case InExpo -> progress == 0 ? 0 : Math.pow(2, 10 * progress - 10);
            case OutExpo -> progress == 1 ? 1 : 1 - Math.pow(2, -10 * progress);
            case InOutExpo -> progress == 0
                    ? 0
                    : progress == 1
                        ? 1
                        : progress < 0.5
                            ? Math.pow(2, 20 * progress - 10) / 2
                            : (2 - Math.pow(2, -20 * progress + 10)) / 2;

            case InBack -> backConst2 * progress * progress * progress - backConst1 * progress * progress;
            case OutBack -> 1 + backConst2 * Math.pow(progress - 1, 3) + backConst1 * Math.pow(progress - 1, 2);
            case InOutBack -> progress < 0.5
                    ? (Math.pow(2 * progress, 2) * ((backConst3 + 1) * 2 * progress - backConst3)) / 2
                    : (Math.pow(2 * progress - 2, 2) * ((backConst3 + 1) * (progress * 2 - 2) + backConst3) + 2) / 2;

            case InBounce -> 1 - calcOutBounce(1 - progress);
            case OutBounce -> calcOutBounce(progress);
            case InOutBounce -> progress < 0.5
                    ? (1 - calcOutBounce(1 - 2 * progress)) / 2
                    : (1 + calcOutBounce(2 * progress - 1)) / 2;
        };
    }

    private static double calcOutBounce(double progress)
    {
        var n1 = 7.5625;
        var d1 = 2.75;

        if (progress < 1 / d1) {
            return n1 * progress * progress;
        } else if (progress < 2 / d1) {
            return n1 * (progress -= 1.5 / d1) * progress + 0.75;
        } else if (progress < 2.5 / d1) {
            return n1 * (progress -= 2.25 / d1) * progress + 0.9375;
        } else {
            return n1 * (progress -= 2.625 / d1) * progress + 0.984375;
        }
    }
}
