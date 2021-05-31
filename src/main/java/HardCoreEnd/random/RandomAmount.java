package HardCoreEnd.random;

import HardCoreEnd.util.MathUtil;
import gnu.trove.map.hash.TIntIntHashMap;
import org.jline.utils.Log;


import java.util.Random;

@FunctionalInterface
public interface RandomAmount{
    int generate(Random rand, int minAmount, int maxAmount);

    public static final RandomAmount

            exact = (rand, min, max) -> {
        return min;
    },

    linear = (rand, min, max) -> {
        return min+rand.nextInt(max-min+1);
    },

    preferSmaller = (rand, min, max) -> {
        return min+ MathUtil.floor(rand.nextDouble()*rand.nextDouble()*(1+max-min));
    },

    aroundCenter = (rand, min, max) -> {
        return MathUtil.clamp((int)Math.round(min+(max-min)*0.5D+(rand.nextDouble()-0.5D)*rand.nextDouble()*(1+max-min)), min, max);
    },

    gaussian = (rand, min, max) -> {
        return min+(int)Math.round(MathUtil.clamp(rand.nextGaussian()*0.5D, 0D, 1D)*(max-min));
    };
}
