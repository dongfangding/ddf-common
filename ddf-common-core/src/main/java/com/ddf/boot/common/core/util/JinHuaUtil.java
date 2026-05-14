package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.model.dto.JinHuaCard;
import com.ddf.boot.common.api.model.dto.JinHuaCardTypeEnum;
import com.ddf.boot.common.api.model.dto.PokerCard;
import com.ddf.boot.common.api.model.dto.PokerCardColorEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class JinHuaUtil {
    // 定义常量，总牌量，一副牌，13张牌，每张牌4个花色，排除大小王
    static final int CARD_TOTAL = 52;
    // 黑桃、红桃、梅花、方块（菱形）
    static final String[] SUIT_LIST = PokerCardColorEnum.getColorArray();
    static final int[] CARD_ID_ARR = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    static final String[] CARD_VALUE1_ARR = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    public static void main(String[] args) {
        // 使用固定牌组进行测试
        System.out.println("\n========== 固定牌组测试 ==========");

        final List<JinHuaCard> fixedHandList = randomCard(15);
        for (JinHuaCard card : fixedHandList) {
            System.out.println();
            final JinHuaCardTypeEnum resolve = JinHuaCardTypeEnum.resolve(card.getCardType());
            System.out.printf("cardType: %s, cardScore: %s%n",
                    "%s:(%s)".formatted(resolve.getType(), resolve.getDesc()), card.getCardScore());
            System.out.println("具体牌型如下: ");
            final List<PokerCard> cards = card.getCards();
            for (PokerCard pokerCard : cards) {
                System.out.println(
                        "  " + getSuitSymbol(pokerCard.getCardColor()) + " " + pokerCard.getCardValue() + " " + " | "
                                + pokerCard.getCardColor() + " " + pokerCard.getOriginCardId() + " "
                                + pokerCard.getCardId());
            }
            System.out.println();
        }

        //        fixedCard();
    }

    /**
     * @param count 数量
     */
    public static List<JinHuaCard> randomCard(int count) {
        //随机创建牌组
        List<JinHuaCard> fixedHandList = createCardByPlayersTotal(count);
        fixedHandList.sort(Comparator.comparing(JinHuaCard::getCardScore).reversed());
        return fixedHandList;
    }

    /**
     * 创建固定的牌组用于测试
     *
     * @return 固定的手牌列表
     */
    private static List<List<Integer>> createFixedHandList() {
        List<List<Integer>> handList = new ArrayList<>();

        // 玩家1：同花顺 - Q, K, A of spades (基本上最大的同花顺)
        List<Integer> hand1 = new ArrayList<>();
        hand1.add(getCardId("Q", "spades"));
        hand1.add(getCardId("K", "spades"));
        hand1.add(getCardId("A", "spades"));

        // 玩家2：豹子 - 三个K (基本上第二大的牌)
        List<Integer> hand2 = new ArrayList<>();
        hand2.add(getCardId("K", "spades"));
        hand2.add(getCardId("K", "hearts"));
        hand2.add(getCardId("K", "clubs"));

        // 玩家3：同花 - 3, 8, J of hearts
        List<Integer> hand3 = new ArrayList<>();
        hand3.add(getCardId("3", "hearts"));
        hand3.add(getCardId("8", "hearts"));
        hand3.add(getCardId("J", "hearts"));

        // 玩家4：顺子 - 6, 7, 8 of mixed suits
        List<Integer> hand4 = new ArrayList<>();
        hand4.add(getCardId("6", "spades"));
        hand4.add(getCardId("7", "hearts"));
        hand4.add(getCardId("8", "clubs"));

        // 玩家5：对子 - A pair + 9
        List<Integer> hand5 = new ArrayList<>();
        hand5.add(getCardId("A", "hearts"));
        hand5.add(getCardId("A", "diamonds"));
        hand5.add(getCardId("9", "clubs"));

        // 玩家6：高牌 - A, 10, 8 of mixed suits
        List<Integer> hand6 = new ArrayList<>();
        hand6.add(getCardId("A", "clubs"));
        hand6.add(getCardId("2", "diamonds"));
        hand6.add(getCardId("4", "spades"));
        // 玩家6：高牌 - A, 10, 8 of mixed suits
        List<Integer> hand7 = new ArrayList<>();
        hand7.add(getCardId("K", "clubs"));
        hand7.add(getCardId("Q", "diamonds"));
        hand7.add(getCardId("10", "spades"));

        // 将所有玩家的手牌添加到列表中
        handList.add(hand1);
        handList.add(hand2);
        handList.add(hand3);
        handList.add(hand4);
        handList.add(hand5);
        handList.add(hand6);
        handList.add(hand7);

        return handList;
    }

    /**
     * 根据牌面和花色获取牌ID
     *
     * @param rank 牌面 ("2"-"10", "J", "Q", "K", "A")
     * @param suit 花色 ("spades", "hearts", "clubs", "diamonds")
     * @return 牌ID (1-52)
     */
    private static int getCardId(String rank, String suit) {
        int suitIndex = -1;
        for (int i = 0; i < SUIT_LIST.length; i++) {
            if (SUIT_LIST[i].equals(suit)) {
                suitIndex = i;
                break;
            }
        }

        int rankIndex = -1;
        for (int i = 0; i < CARD_VALUE1_ARR.length; i++) {
            if (CARD_VALUE1_ARR[i].equals(rank)) {
                rankIndex = i;
                break;
            }
        }

        if (suitIndex == -1 || rankIndex == -1) {
            throw new IllegalArgumentException("Invalid card: " + rank + " of " + suit);
        }

        return suitIndex * 13 + rankIndex + 1;
    }

    /**
     * 根据玩家数量创建并分配手牌
     *
     * @param total 玩家数量
     * @return 所有玩家的手牌列表
     */
    private static List<JinHuaCard> createCardByPlayersTotal(int total) {
        if (total * 3 > CARD_TOTAL) {
            System.err.println("玩家数超过最大值");
            // 返回空列表表示出错
            return new ArrayList<>();
        }

        int[] idArr = getShuffledIdArr();
        // 获得手牌
        List<List<PokerCard>> handList = getHandCardList(total, idArr);
        List<JinHuaCard> rtnList = new ArrayList<>();
        for (List<PokerCard> cards : handList) {
            rtnList.add(evaluateHand(cards));
        }
        return rtnList;
    }

    /**
     * 创建并洗牌
     *
     * @return 洗好的牌数组
     */
    private static int[] getShuffledIdArr() {
        int[] idArr = new int[CARD_TOTAL];
        for (int i = 0; i < CARD_TOTAL; i++) {
            idArr[i] = i + 1;
        }

        // Fisher-Yates 洗牌算法
        Random random = new Random();
        for (int i = CARD_TOTAL - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);
            // 交换位置
            int temp = idArr[index];
            idArr[index] = idArr[i];
            idArr[i] = temp;
        }

        return idArr;
    }



    /**
     * 从洗好的牌中分发手牌
     *
     * @param total 玩家数量
     * @param cardArr 洗好的牌数组
     * @return 所有玩家的手牌列表
     */
    private static List<List<PokerCard>> getHandCardList(int total, int[] cardArr) {
        List<List<PokerCard>> handList = new ArrayList<>();

        // 直接从洗好的牌中依次抽取
        for (int i = 0; i < total; i++) {
            List<PokerCard> hand = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                // 原始card_id
                final int originCardId = cardArr[i * 3 + j] - 1;
                // 卡牌的id
                int cardId = CARD_ID_ARR[originCardId % 13];
                // 卡牌的面值
                String cardValue = CARD_VALUE1_ARR[originCardId % 13];
                // 花色
                String cardColor = SUIT_LIST[originCardId / 13];
                hand.add(PokerCard.of(originCardId, cardId, cardValue, cardColor));
            }
            handList.add(hand);
        }
        System.out.println("所有玩家手牌: " + handList);
        return handList;
    }

    // 是否是豹子

    /**
     * @param rank 参数
     */
    private static boolean isThreeOfAKind(String[] rank) {
        return rank[0].equals(rank[1]) && rank[1].equals(rank[2]);
    }

    // 是否是同花顺

    /**
     * @param suit 参数
     * @param rankValues 参数
     */
    private static boolean isStraightFlush(String[] suit, int[] rankValues) {
        return isFlush(suit) && isStraight(rankValues);
    }

    // 是否是同花

    /**
     * @param suit 参数
     */
    private static boolean isFlush(String[] suit) {
        return suit[0].equals(suit[1]) && suit[1].equals(suit[2]);
    }

    // 是否是顺子

    /**
     * @param rankValues 参数
     */
    private static boolean isStraight(int[] rankValues) {
        // 复制并排序
        int[] sorted = Arrays.copyOf(rankValues, rankValues.length);
        Arrays.sort(sorted);

        // 检查普通顺子
        if (sorted[0] + 1 == sorted[1] && sorted[1] + 1 == sorted[2]) {
            return true;
        }

        // 特殊情况: A-2-3 顺子
        // A的值是14，需要特殊处理
        if (sorted[0] == 2 && sorted[1] == 3 && sorted[2] == 14) {
            return true;
        }

        return false;
    }

    // 是否是对子

    /**
     * @param rank 参数
     */
    private static boolean isPair(String[] rank) {
        return rank[0].equals(rank[1]) || rank[0].equals(rank[2]) || rank[1].equals(rank[2]);
    }


    /**
     * 根据牌型返回对应的表情符号
     *
     * @param handType 参数
     */
    private static String getTypeEmoji(JinHuaCardTypeEnum handType) {
        return switch (handType) {
            case TYPE_THREE_OF_A_KIND -> "🏆"; // 豹子
            case TYPE_STRAIGHT_FLUSH -> "👑"; // 同花顺
            case TYPE_FLUSH -> "🌸"; // 同花
            case TYPE_STRAIGHT -> "📈"; // 顺子
            case TYPE_PAIR -> "👯"; // 对子
            case TYPE_HIGH_CARD -> "🃏"; // 高牌
            default -> "";
        };
    }

    /**
     * 评估单个玩家手牌的得分
     *
     * @param hand 玩家手牌
     * @return 手牌得分
     */
    private static JinHuaCard evaluateHand(List<PokerCard> hand) {
        // 解析手牌
        String[] suitArr = new String[3];
        String[] rankArr = new String[3];
        int[] rankValues = new int[3];

        for (int j = 0; j < hand.size(); j++) {
            final PokerCard tmpCard = hand.get(j);
            String color = tmpCard.getCardColor();
            int rankIndex = tmpCard.getCardId();
            String rank = tmpCard.getCardValue();
            suitArr[j] = color;
            rankArr[j] = rank;
            rankValues[j] = rankIndex; // 2-14 表示 2-A
        }
        final JinHuaCard card = new JinHuaCard();
        card.setCards(hand);
        // 判断牌型
        boolean isThreeOfAKind = isThreeOfAKind(rankArr);
        boolean isStraightFlush = isStraightFlush(suitArr, rankValues);
        boolean isFlush = isFlush(suitArr);
        boolean isStraight = isStraight(rankValues);
        boolean isPair = isPair(rankArr);

        // 牌型基础分 - 使用更大的基础分差距
        int typeScore;
        if (isThreeOfAKind) {
            typeScore = 9000000; // 豹子
            card.setCardType(JinHuaCardTypeEnum.TYPE_THREE_OF_A_KIND.getType());
        } else if (isStraightFlush) {
            typeScore = 8000000; // 同花顺
            card.setCardType(JinHuaCardTypeEnum.TYPE_STRAIGHT_FLUSH.getType());
        } else if (isFlush) {
            typeScore = 7000000; // 同花
            card.setCardType(JinHuaCardTypeEnum.TYPE_FLUSH.getType());
        } else if (isStraight) {
            typeScore = 6000000; // 顺子
            card.setCardType(JinHuaCardTypeEnum.TYPE_STRAIGHT.getType());
        } else if (isPair) {
            typeScore = 5000000; // 对子
            card.setCardType(JinHuaCardTypeEnum.TYPE_PAIR.getType());
        } else {
            typeScore = 4000000; // 高牌
            card.setCardType(JinHuaCardTypeEnum.TYPE_HIGH_CARD.getType());
        }

        // 附加分 - 用于同种牌型的大小比较
        int additionalScore = 0;

        if (isThreeOfAKind) {
            // 豹子的大小比较（A最大，2最小）
            additionalScore = getRankValue(rankArr[0]);
        } else if (isStraightFlush || isStraight) {
            additionalScore = calculateStraightValue(rankValues);
        } else if (isFlush || !isPair) { // 同花或高牌
            additionalScore = calculateHighCardValue(rankValues);
        } else if (isPair) {
            // 找出对子的牌值和单张的牌值
            int pairValue = 0;
            int kicker = 0;

            if (rankArr[0].equals(rankArr[1])) {
                pairValue = getRankValue(rankArr[0]);
                kicker = getRankValue(rankArr[2]);
            } else if (rankArr[0].equals(rankArr[2])) {
                pairValue = getRankValue(rankArr[0]);
                kicker = getRankValue(rankArr[1]);
            } else { // rankArr[1].equals(rankArr[2])
                pairValue = getRankValue(rankArr[1]);
                kicker = getRankValue(rankArr[0]);
            }

            // 对子的牌值*15 + 单张的牌值，确保对子的大小是主要因素
            additionalScore = pairValue * 15 + kicker;
        }
        card.setCardScore(typeScore + additionalScore);
        return card;
    }

    /**
     * 获取单个牌面的数值（2-14，其中A=14,K=13,Q=12,J=11）
     *
     * @param rank 参数
     */
    private static int getRankValue(String rank) {
        switch (rank) {
            case "A":
                return 14;
            case "K":
                return 13;
            case "Q":
                return 12;
            case "J":
                return 11;
            default:
                return Integer.parseInt(rank);
        }
    }

    /**
     * 计算顺子的价值（基于最高牌，特殊处理A-2-3）
     *
     * @param rankValues 参数
     */
    private static int calculateStraightValue(int[] rankValues) {
        // 复制并排序
        int[] sorted = Arrays.copyOf(rankValues, rankValues.length);
        Arrays.sort(sorted);

        // 检查是否是A-2-3顺子（这是最小的顺子）
        if (sorted[0] == 2 && sorted[1] == 3 && sorted[2] == 14) {
            return 3; // A-2-3顺子的值为3
        }

        // 普通顺子，返回最高牌的值，第三张牌
        return sorted[2];
    }

    /**
     * 计算高牌组合的价值
     * 02
     * 14
     * 1300
     * 1200
     * 2500
     *
     * @param rankValues 参数
     */
    private static int calculateHighCardValue(int[] rankValues) {
        // 复制并排序（从小到大）
        int[] sorted = Arrays.copyOf(rankValues, rankValues.length);
        Arrays.sort(sorted);

        // 最高牌权重最大，次高牌次之，最小牌权重最小
        // 核心是每个人的手牌是三张牌，每张牌的大小是随机在2~14之间，所以如果转换成数字的话，每张牌就是两位数，六张牌最大就是就是6位数。
        // 前两位数，就是六位数的高位部分， 所以拿当前数字 * 10000，如果当前数字是9，那就是9万，如果是14，那就是140000
        // 中间的两位数定位到六位数的第三位和第四位，如果是第三位，那么他其实就是千分位，因为最大两位数，所以乘100就可能达到千分位，所以第二位数*100
        // 最后两位/一位数，保持不变，直接定位到个位数或者十位数。
        // 所以如果牌面是8,6,5对应的数字就是80605
        // 牌面如果是KJ2,对应的数字就是131102

        // 更简单的算法是，根据当前最大值决定补位数，核心都是最后一位不变,而对一个数字补位数最简单的方法*对应位数，如果补两个0，就是*100
        // 如果是3个0， 最后一位不变，比如xxx，3个0补给比它大的一张牌，所以这张牌yyy000，再把这张牌补给更上面一张牌，那就是6个0，结合起来就是zzz000000.
        // 最后的牌型就是，zzzyyyxxx

        return sorted[2] * 10000 + sorted[1] * 100 + sorted[0];
    }

    /**
     * @param suit 参数
     */
    private static String getSuitSymbol(String suit) {
        switch (suit) {
            case "SPADES":
                return "♠";
            case "HEARTS":
                return "♥";
            case "CLUBS":
                return "♣";
            case "DIAMONDS":
                return "♦";
            default:
                return "";
        }
    }

}
