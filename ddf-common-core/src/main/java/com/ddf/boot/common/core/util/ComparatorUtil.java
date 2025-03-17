package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.model.IndexComparatorElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/03/15 18:05
 */
public class ComparatorUtil {


    /**
     * 不包含删除的判定！！！！！
     * 比较两个数组元素之间的差异，比对每一个位置，判断后面这个字符串每个位置的元素和前面相比是相同还是新增还是替换等
     *
     * @param oldArrStr
     * @param newArrStr
     * @return
     */
    public static List<IndexComparatorElement> compareSplitArrElementDifference(String oldArrStr, String newArrStr) {
        final String[] oldArr = oldArrStr.isEmpty() ? new String[0] : oldArrStr.split(",");
        final String[] newArr = newArrStr.isEmpty() ? new String[0] : newArrStr.split(",");
        int maxLength = Math.max(oldArr.length, newArr.length);
        List<IndexComparatorElement> result = new ArrayList<>();
        Set<String> newSet = new HashSet<>(Arrays.asList(newArr));
        for (int i = 0; i < maxLength; i++) {
            // 处理可能的替换
            if (i < oldArr.length && i < newArr.length) {
                if (!oldArr[i].equals(newArr[i])) {
                    result.add(IndexComparatorElement.of(i, newArr[i], 2, oldArr[i]));
                }
            }
            // 处理新增
            else if (i < newArr.length) {
                result.add(IndexComparatorElement.of(i, newArr[i], 1, null));
            }
            // 处理删除
            else if (!newSet.contains(oldArr[i])) {
                result.add(IndexComparatorElement.of(i, oldArr[i], 3, null));
            }
        }
        return result;
    }


    /**
     * 用逗号分隔的字符串
     * 当一个元素匹配分隔后的数组中的元素时，就替换。
     * 最后返回最新的替换后的字符串
     *
     * @param originStr
     * @param matchStr
     * @param replaceStr
     * @return
     */
    public static String replaceSplitValueIfMatch(String originStr, String matchStr, String replaceStr) {
        if (StringUtils.isBlank(originStr)) {
            return replaceStr;
        }
        final String[] originArr = originStr.split(",");
        String[] newArray = Arrays.copyOf(originArr, originArr.length);
        for (int i = 0; i < newArray.length; i++) {
            if (newArray[i].equals(matchStr)) {
                newArray[i] = replaceStr;
            }
        }
        return String.join(",", newArray);
    }


    /**
     * 用逗号分隔的字符串
     * 当一个元素匹配分隔后的数组中的元素时，删除元素。
     * 最后返回最新的替换后的字符串
     *
     * @param originStr
     * @param matchStr
     * @return
     */
    public static String removeSplitValueIfMatch(String originStr, String matchStr) {
        if (StringUtils.isBlank(originStr)) {
            return "";
        }
        final String[] originArr = originStr.split(",");
        List<String> list = new ArrayList<>(Arrays.asList(originArr));
        list.removeIf(element -> element.equals(matchStr));
        return String.join(",", list);
    }



    public static void test() {

        // 测试用例 1：完全空字符串
        System.out.println(compareSplitArrElementDifference("", ""));

        // 测试用例 2：一个字符串为空
        System.out.println(compareSplitArrElementDifference("", "d,b,e,f"));

        // 测试用例 3：完全相同的字符串
        System.out.println(compareSplitArrElementDifference("a,b,c", "a,b,c"));

        // 测试用例 4：完全不相同的字符串
        System.out.println(compareSplitArrElementDifference("a,b,c", "d,e,f"));

        // 测试用例 5：部分相同的字符串
        System.out.println(compareSplitArrElementDifference("a,b,c", "a,x,c,q"));

        // 测试用例 6：新增、替换、删除混合情况
        System.out.println(compareSplitArrElementDifference("a,b,c,q,x", "d,b,e,f"));

        System.out.println("\n=== 测试 replaceElements 方法 ===");
        String originStr = "a,b,c,d";
        String replacedString = replaceSplitValueIfMatch(originStr, "b", "z");
        System.out.println("替换后数组: " + replacedString);

        System.out.println("\n=== 测试 deleteElements 方法 ===");
        String deletedString = removeSplitValueIfMatch(originStr, "b");
        System.out.println("删除后数组: " + deletedString);

    }

    public static void main(String[] args) {
        test();
    }
}
