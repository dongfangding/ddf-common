package com.ddf.boot.common.core.util;

import cn.hutool.core.util.RandomUtil;
import com.ddf.boot.common.api.model.common.DefaultWeightProportion;
import com.ddf.boot.common.api.model.common.WeightProportion;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>随机工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/09/21 16:31
 */
public class RandomExtUtil {

    final static DateTimeFormatter YMD_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    public static String randomOrderNo(int maxLength) {
        return randomOrderNo("-", maxLength);
    }

    /**
     * 规则比较简单，会暴露当前时间的生成订单号，适合内部或者小型系统，不存在暴露订单量的担心时使用
     *
     * @return
     */
    public static String randomOrderNo(String separator, int maxLength) {
        final String format = YMD_FORMATTER.format(LocalDateTime.now());
        if (maxLength < format.length() + separator.length()) {
            throw new IllegalArgumentException("length长度不支持");
        }
        return String.join(separator, format, RandomUtil.randomNumbers(maxLength - format.length() - separator.length()));
    }

    /**
     * 百分比概率命中判定
     *
     * @param proportion
     * @return
     */
    public static boolean hitPercent(int proportion) {
        return RandomUtil.randomInt(100) < proportion;
    }

    /**
     * 百分比概率命中判定
     *
     * @param proportion
     * @return
     */
    public static boolean hitPercent(double proportion) {
        return RandomUtil.randomDouble(100) < proportion;
    }

    /**
     * 基于权重的中奖概率判定
     *
     * @return
     */
    public static <T extends WeightProportion> T hitWeightProportion(List<T> sources) {
        // 先求出这批数据的总权重
        final double totalWeight = sources.stream()
                .mapToDouble(WeightProportion::getWeight)
                .sum();
        // 先随机出一个数值
        double randomNum = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (T source : sources) {
            if ((randomNum -= source.getWeight()) < 0) {
                return source;
            }
        }
        // 这里的话，肯定数值错误了
        return null;
    }

    /**
     * 根据权重次数重新生成数据，生成后的数据长度等于数据的权重之和，用以一些权重规则上的数据列表生成。
     * 注意这种情况下的权重只支持整形，如果存在小数，自己等比例放大
     *
     * 比如
     * list[0] 权重10
     * list[1] 权重5
     *
     * 则最终会生成15条数据， 生成的顺序根据权重来判定，每次生成后当前权重减少1
     *
     * @return
     */
    public static <T extends WeightProportion> List<T> generateAllByWeight(List<T> sources, Class<T> clazz) {
        // 使用一个默认实现来拷贝属性， 不影响到原对象数据
        List<T> tempList = BeanCopierUtils.copy(sources, clazz);
        List<T> rtnList = new ArrayList<>();
        // 先求出这批数据的总权重，这种情况下的数据只支持整形
        final int totalWeight = tempList.stream()
                .mapToInt(obj -> obj.getWeight().intValue())
                .sum();
        int randomNum;
        // 将所有的数据都随机出来，总权重即是总次数
        for (int i = totalWeight; i > 0; i--) {
            // 先随机出一个数值
            randomNum = ThreadLocalRandom.current().nextInt(i);
            for (T source : tempList) {
                if ((randomNum -= source.getWeight()) < 0) {
                    // 每中奖一次自己的权重就减少1次
                    source.changeOriginWeight(source.getWeight() - 1);
                    rtnList.add(source);
                    break;
                }
            }
        }
        return rtnList;
    }

    /**
     * 平均分包算法， 比如100块的红包，要发10份，保证每份最少8块， 不考虑重复问题, 这个更多的考虑的是在一定范围内的随机,因此平均也不那么平均,
     * 但是保持在控制范围内的随机
     *
     * @param totalValue 总金额
     * @param packSize   分包数量
     * @param fixedValue 每个分包保底数值
     * @param distinct   是否金额去重
     * @return
     */
    public static int[] averagePack(int totalValue, int packSize, int fixedValue, boolean distinct) {
        // 可以用来随机的金额，保底要去除掉， 这部分不参与随机
        int randomAmount = totalValue - fixedValue * packSize;
        // 最终红包金额
        int[] amount = new int[10];
        if (randomAmount < 0) {
            return amount;
        }
        if (randomAmount == 0) {
            for (int i = 0; i < packSize; i++) {
                amount[i] = fixedValue;
            }
            return amount;
        }
        // 平均分布的数值区间
        int[] segment = new int[10];
        // 随机出来红包大小数量-1数值间隙， 注意，这里并不是最终红包金额，只是数字的平均分布
        for (int i = 0; i < packSize - 1; i++) {
            segment[i] = RandomUtil.randomInt(1, randomAmount + 1);
        }
        // 最后一个数字用最大值
        segment[9] = randomAmount;

        // 必须从小到大对数值排序
        Arrays.sort(segment);
        Set<Integer> distinctAmountSet = new HashSet<>();
        // 根据计算出的数值分布区间的差值 + 保底的金额计算出来红包的最终金额
        for (int i = 0, temp = 0; i < segment.length; i++) {
            amount[i] = (segment[i] - temp + fixedValue);
            temp = segment[i];
            distinctAmountSet.add(amount[i]);
        }
        if (distinct && distinctAmountSet.size() != packSize) {
            return averagePack(totalValue, packSize, fixedValue, distinct);
        }
        return amount;
    }


    /**
     * 平均分包算法， 比如100块的红包，要发10份，保证每份最少8块， 不考虑重复问题
     *
     * @param totalValue 总金额
     * @param packSize    分包数量
     * @param fixedValue 每个分包保底数值
     * @return
     */
    public static int[] averagePack(int totalValue, int packSize, int fixedValue) {
        return averagePack(totalValue, packSize, fixedValue, false);
    }

    /**
     * 也是一种平均分包，但多了一些近似绝对平均的属性在里面
     *
     * @param totalAmount
     * @param packSize
     * @param fixedAmount
     * @return
     */
    public static int[] averageApproximatelyAbsolute(int totalAmount, int packSize, int fixedAmount) {
        int[] average = new int[10];
        // 绝对平均值
        int absoluteAverage = totalAmount / packSize;
        // 已分配的值
        int assignment = 0;
        for (int i = 0; i < packSize; i++) {
            average[i] = RandomUtil.randomInt(fixedAmount, absoluteAverage + 1);
            assignment += average[i];
        }
        int remaining = totalAmount - assignment;
        for (int i = 0; i < remaining; i++) {
            average[i % packSize] = average[Math.max(0, i % packSize)] + 1;
        }
        return average;
    }

    public static void main(String[] args) {
        final List<DefaultWeightProportion> proportions = Lists.newArrayList(
                DefaultWeightProportion.of("1", 10d),
                DefaultWeightProportion.of("2", 20d),
                DefaultWeightProportion.of("3", 30d),
                DefaultWeightProportion.of("4", 40d)
        );
        int count1 = 0, count2 = 0, count3 = 0, count4 = 0;
        WeightProportion temp;
        for (int i = 0; i < 1000; i++) {
            temp = hitWeightProportion(proportions);
            if (Objects.equal("1", temp.getKey())) {
                count1 ++;
            } else if (Objects.equal("2", temp.getKey())) {
                count2 ++;
            } else if (Objects.equal("3", temp.getKey())) {
                count3 ++;
            } else if (Objects.equal("4", temp.getKey())) {
                count4 ++;
            }
        }
        System.out.println("count1 = " + count1);
        System.out.println("count2 = " + count2);
        System.out.println("count3 = " + count3);
        System.out.println("count4 = " + count4);
    }
}
