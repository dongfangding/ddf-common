package com.ddf.boot.common.core.util;

import cn.hutool.core.util.RandomUtil;
import com.ddf.boot.common.api.model.common.dto.DefaultWeightProportion;
import com.ddf.boot.common.api.model.common.dto.ObjectKeyValuePair;
import com.ddf.boot.common.api.model.common.dto.WeightProportion;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>随机工具类</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2022/09/21 16:31
 */
public class RandomExtUtil {

    /**
     * 用于随机选的数字
     */
    public static final String BASE_NUMBER = "0123456789";
    /**
     * 用于随机选的字符
     */
    public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyz";

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
        return String.join(
            separator, format, RandomUtil.randomNumbers(maxLength - format.length() - separator.length()));
    }

    /**
     * 百分比概率命中判定
     *
     * @param proportion
     * @return
     */
    public static boolean hitPercent(int proportion) {
        return RandomUtil.randomInt(0, 100) < proportion;
    }

    /**
     * 百分比概率命中判定
     *
     * @param proportion
     * @return
     */
    public static boolean hitPercent(double proportion) {
        return RandomUtil.randomDouble(0, 100) < proportion;
    }

    /**
     * 0~1概率判定
     *
     * @param proportion
     * @return
     */
    public static boolean hitProbability(double proportion) {
        return RandomUtil.randomDouble(0, 1) < proportion;
    }

    /**
     * 百分比概率命中判定， 同时返回随机到的概率值
     *
     * @param proportion
     * @return
     */
    public static ObjectKeyValuePair<Double, Boolean> hitPercentWithProbability(double proportion) {
        final double randomDouble = RandomUtil.randomDouble(0, 100);
        return ObjectKeyValuePair.of(randomDouble, randomDouble < proportion);
    }

    /**
     * 基于权重的中奖概率判定
     *
     * @return
     */
    public static <T extends WeightProportion> T hitWeightProportion(List<T> sources) {
        // 先求出这批数据的总权重
        final double totalWeight = sources
            .stream()
            .mapToDouble(WeightProportion::getWeightValue)
            .sum();
        // 先随机出一个数值
        double randomNum = ThreadLocalRandom
            .current()
            .nextDouble(totalWeight);
        for (T source : sources) {
            if ((randomNum -= source.getWeightValue()) < 0) {
                return source;
            }
        }
        // 这里的话，肯定数值错误了
        return null;
    }

    /**
     * 根据权重次数重新生成数据，生成后的数据长度等于数据的权重之和，用以一些权重规则上的数据列表生成。
     * 注意这种情况下的权重只支持整形，如果存在小数，自己等比例放大
     * <p>
     * 比如
     * list[0] 权重10
     * list[1] 权重5
     * <p>
     * 则最终会生成15条数据， 生成的顺序根据权重来判定，每次生成后当前权重减少1
     *
     * @return
     */
    public static <T extends WeightProportion> List<T> generateAllByWeight(List<T> sources, Class<T> clazz) {
        // 使用一个默认实现来拷贝属性， 不影响到原对象数据
        List<T> tempList = BeanCopierUtils.copy(sources, clazz);
        List<T> rtnList = new ArrayList<>();
        // 先求出这批数据的总权重，这种情况下的数据只支持整形
        final int totalWeight = tempList
            .stream()
            .mapToInt(obj -> obj
                .getWeightValue()
                .intValue())
            .sum();
        int randomNum;
        // 将所有的数据都随机出来，总权重即是总次数
        for (int i = totalWeight; i > 0; i--) {
            // 先随机出一个数值
            randomNum = ThreadLocalRandom
                .current()
                .nextInt(i);
            for (T source : tempList) {
                if ((randomNum -= source.getWeightValue()) < 0) {
                    // 每中奖一次自己的权重就减少1次
                    source.changeOriginWeight(source.getWeightValue() - 1);
                    rtnList.add(source);
                    break;
                }
            }
        }
        return rtnList;
    }


    /**
     * 这个的实现更加简单且高效，但效果应该是等同于{@link #generateAllByWeight}效果的。
     * 权重即使重复次数，将数据重复生成后，最后打乱，其实和所谓的权重效果是一样的。
     *
     * @param sources
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends WeightProportion> List<T> generateAllByShuffle(List<T> sources, Class<T> clazz) {
        // 使用一个默认实现来拷贝属性， 不影响到原对象数据
        List<T> tempList = BeanCopierUtils.copy(sources, clazz);
        List<T> rtnList = new ArrayList<>();
        for (T t : tempList) {
            for (int i = 0; i < t
                .getWeightValue()
                .intValue(); i++) {
                rtnList.add(t);
            }
        }
        Collections.shuffle(rtnList);
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
     * @param packSize   分包数量
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


    /**
     * 用来计算每singValue获得一次奖励机会， 通过这种方式可以不需要保存上次发放奖励的分数也能算出来当前能够获得多少机会
     * 缺点：
     * 1. 如果某一次数值用掉了也获得了机会，但是业务奖励给失败了，这里再算一次，用之前的分数就会丢失
     * 2. 如果用户的积分在不同区间给的奖励不一样，虽然次数相同，但是奖励不同，那也不行，这里只会以最后的分值来返回次数而已
     *
     * @param afterValue  最后数值
     * @param beforeValue 之前的数值
     * @param singleValue 每获得一次机会需要的数值
     * @return
     */
    public Long calcRewardTimes(Long afterValue, Long beforeValue, Long singleValue) {
        // 每5关获得一次奖励（注意，如果真的出现这种情况，只会以最后一次排名发放奖励）
        return (afterValue - beforeValue / singleValue * singleValue) / singleValue;
    }

    /**
     * 根据时间戳生成小数位数值， 时间戳越小，值越大
     *
     * @param time
     * @return
     */
    public static BigDecimal calcPointScoreByTime(long time) {
        final BigDecimal decimal = new BigDecimal(time * Math.pow(
            10, Math.negateExact(String
                .valueOf(time)
                .length())
        ));
        return new BigDecimal("1.0").subtract(decimal);
    }

    public static void main(String[] args) {
        final List<DefaultWeightProportion> proportions = Lists.newArrayList(
            DefaultWeightProportion.of("1", 10d),
            DefaultWeightProportion.of("2", 20d), DefaultWeightProportion.of("3", 30d),
            DefaultWeightProportion.of("4", 40d)
        );
        int count1 = 0, count2 = 0, count3 = 0, count4 = 0;
        WeightProportion temp;
        for (int i = 0; i < 1000; i++) {
            temp = hitWeightProportion(proportions);
            if (Objects.equal("1", temp.getKey())) {
                count1++;
            } else if (Objects.equal("2", temp.getKey())) {
                count2++;
            } else if (Objects.equal("3", temp.getKey())) {
                count3++;
            } else if (Objects.equal("4", temp.getKey())) {
                count4++;
            }
        }
        System.out.println("count1 = " + count1);
        System.out.println("count2 = " + count2);
        System.out.println("count3 = " + count3);
        System.out.println("count4 = " + count4);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong roundId = new AtomicLong(0);
        final AtomicLong startSeconds = new AtomicLong(0);
        final AtomicBoolean isLuck = new AtomicBoolean(false);
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> round(roundId, startSeconds, isLuck), 5, 5, TimeUnit.SECONDS);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void round(AtomicLong roundId, AtomicLong startSeconds, AtomicBoolean isLuck) {
        final long currentSeconds = System.currentTimeMillis() / 1000;
        long pastTime = currentSeconds - startSeconds.get();
        if (roundId.get() == 0) {
            startRound(roundId, startSeconds, isLuck, currentSeconds);
        } else {
            if (pastTime >= 15 + (isLuck.get() ? 10 : 5) + 5) {
                System.out.printf(
                    "%s: 结算完成, 开始下一轮, roundId = %s\n\n", new Date(currentSeconds * 1000), roundId.get());
                startRound(roundId, startSeconds, isLuck, currentSeconds);
            } else if (pastTime >= 15 + (isLuck.get() ? 10 : 5)) {
                System.out.printf(
                    "%s: 战斗结束，开始结算, roundId = %s\n", new Date(currentSeconds * 1000), roundId.get());
            } else if (pastTime >= 15) {
                System.out.printf(
                    "%s: 投注结束，开始战斗, roundId = %s\n", new Date(currentSeconds * 1000), roundId.get());
            }
        }
    }

    private static void startRound(AtomicLong roundId, AtomicLong startSeconds, AtomicBoolean isLuck,
        long currentSeconds) {
        roundId.incrementAndGet();
        startSeconds.set(currentSeconds);
        System.out.printf("%s: 开启新场次, roundId = %s\n", new Date(currentSeconds * 1000), roundId.get());
        isLuck.set(RandomUtil.randomInt(10) % 2 == 0);
    }


    /**
     * 随机字母数字
     *
     * @param length
     * @return
     */
    public static String randomLetters(int length) {
        return RandomUtil.randomString(BASE_CHAR, length);
    }

    /**
     * 更符合项目中的随机int， 如果前后区间数值一样，直接返回当前数值，而不是报错
     *
     * @param start
     * @param end
     * @return
     */
    public static Integer randomInt(int start, int end) {
        if (start == end) {
            return start;
        }
        final ThreadLocalRandom localRandom = ThreadLocalRandom.current();
        return localRandom.nextInt(start, end);
    }
}
