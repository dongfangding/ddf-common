package com.ddf.boot.common.core.util;

import cn.hutool.core.util.URLUtil;
import com.ddf.boot.common.core.model.IndexComparatorElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/03/15 18:05
 */
public class ComparatorUtil {


    /**
     * 比较两个以逗号分隔的图片字符串，找出新增和删除项（默认不处理替换）
     */
    public static List<IndexComparatorElement> compareDifference(String oldArrStr, String newArrStr) {
        String[] oldArr = StringUtils.isBlank(oldArrStr) ? new String[0] : oldArrStr.split(",");
        String[] newArr = StringUtils.isBlank(newArrStr) ? new String[0] : newArrStr.split(",");

        Set<String> oldSet = new HashSet<>(Arrays.asList(oldArr));
        Set<String> newSet = new HashSet<>(Arrays.asList(newArr));

        List<IndexComparatorElement> result = new ArrayList<>();

        // 新增项
        for (int i = 0; i < newArr.length; i++) {
            String img = newArr[i];
            if (!oldSet.contains(img)) {
                result.add(IndexComparatorElement.of(i, img, 1, null));
            }
        }

        // 删除项
        for (int i = 0; i < oldArr.length; i++) {
            String img = oldArr[i];
            if (!newSet.contains(img)) {
                result.add(IndexComparatorElement.of(i, img, 3, null));
            }
        }

        return result;
    }

    /**
     * 比较两个以逗号分隔的图片字符串，找出新增和删除项（默认不处理替换）, 因为传过来的参数没有强制索引，所以并不严谨。如下
     * System.out.println(compareWithWeakReplaceLast("a,b,c,q,x,y,z", "d,b,e,f,z"));
     * 输出
     * [第0位: 'a' 被替换为 'd', 第2位: 'c' 被替换为 'e', 第3位: 'q' 被替换为 'f', 第4位: 'x' (删除), 第5位: 'y' (删除)]
     * 最终逻辑流程如下：
     * 先处理新增/删除：按内容差集来判断。
     * 最后尝试找出成对的一增一删且索引相近的 → 视为替换
     * 替换 = 删除 + 新增（成对）
     * 位置越近，替换可信度越高
     * 替换的那对从新增/删除列表中移除，统一归入“替换”结果
     *
     * @param oldArrStr 参数
     * @param newArrStr 参数
     */
    public static List<IndexComparatorElement> compareWithWeakReplaceLast(String oldArrStr, String newArrStr) {
        String[] oldArr = StringUtils.isBlank(oldArrStr) ? new String[0] : oldArrStr.split(",");
        String[] newArr = StringUtils.isBlank(newArrStr) ? new String[0] : newArrStr.split(",");

        List<IndexComparatorElement> result = new ArrayList<>();
        List<IndexComparatorElement> addList = new ArrayList<>();
        List<IndexComparatorElement> delList = new ArrayList<>();

        Set<String> oldSet = new HashSet<>(Arrays.asList(oldArr));
        Set<String> newSet = new HashSet<>(Arrays.asList(newArr));

        // 1. 内容差异：新增
        for (int i = 0; i < newArr.length; i++) {
            String val = newArr[i];
            if (!oldSet.contains(val)) {
                addList.add(IndexComparatorElement.of(i, val, 1, null));
            }
        }

        // 2. 内容差异：删除
        for (int i = 0; i < oldArr.length; i++) {
            String val = oldArr[i];
            if (!newSet.contains(val)) {
                delList.add(IndexComparatorElement.of(i, val, 3, null));
            }
        }

        // 3. 尝试识别为替换（成对的一删一增，位置接近）
        List<IndexComparatorElement> handledAdd = new ArrayList<>();
        List<IndexComparatorElement> handledDel = new ArrayList<>();

        for (IndexComparatorElement del : delList) {
            for (IndexComparatorElement add : addList) {
                if (!handledAdd.contains(add) && Math.abs(del.getIndex() - add.getIndex()) <= 1) {
                    // 位置接近，判断为替换
                    result.add(IndexComparatorElement.of(add.getIndex(), add.getValue(), 2, del.getValue()));
                    handledAdd.add(add);
                    handledDel.add(del);
                    break;
                }
            }
        }

        // 剩下的新增/删除
        for (IndexComparatorElement add : addList) {
            if (!handledAdd.contains(add)) {
                result.add(add);
            }
        }
        for (IndexComparatorElement del : delList) {
            if (!handledDel.contains(del)) {
                result.add(del);
            }
        }

        return result;
    }



    /**
     * 用逗号分隔的字符串
     * 当一个元素匹配分隔后的数组中的元素时，就替换。
     * 最后返回最新的替换后的字符串
     *
     * @param originStr originSTR参数
     * @param matchStr 匹配字符串
     * @param replaceStr replaceSTR参数
     */
    public static String replaceSplitValueIfMatch(String originStr, String matchStr, String replaceStr) {
        if (StringUtils.isBlank(originStr)) {
            return replaceStr;
        }
        final String[] originArr = originStr.split(",");
        String[] newArray = Arrays.copyOf(originArr, originArr.length);
        for (int i = 0; i < newArray.length; i++) {
            if (newArray[i].contains(matchStr)) {
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
     * @param originStr originSTR参数
     * @param matchStr 匹配字符串
     */
    public static String removeSplitValueIfMatch(String originStr, String matchStr) {
        return removeSplitValueIfMatch(originStr, Set.of(matchStr));
    }


    /**
     * 批量删除匹配的字符串
     *
     * @param originStr originSTR参数
     * @param toRemove TOremove参数
     */
    public static String removeSplitValueIfMatch(String originStr, Set<String> toRemove) {
        if (StringUtils.isBlank(originStr)) {
            return "";
        }
        toRemove = toRemove.stream().map(item -> URLUtil.url(item).getPath()).collect(Collectors.toSet());
        Set<String> finalToRemove = toRemove;
        return Arrays.stream(originStr.split(","))
                .filter(s -> !finalToRemove.contains(URLUtil.url(s).getPath()))
                .collect(Collectors.joining(","));
    }



    public static void test() {

        System.out.println(compareWithWeakReplaceLast("a,b,c,d", "d,b,c,a"));


        System.out.println(compareWithWeakReplaceLast("A,B", "B"));
        System.out.println(compareWithWeakReplaceLast("1,2,3,4", "5"));

        // 测试用例 1：完全空字符串
        System.out.println(compareWithWeakReplaceLast("", ""));

        // 测试用例 2：一个字符串为空
        System.out.println(compareWithWeakReplaceLast("", "d,b,e,f"));

        // 测试用例 3：完全相同的字符串
        System.out.println(compareWithWeakReplaceLast("a,b,c", "a,b,c"));

        // 测试用例 4：完全不相同的字符串
        System.out.println(compareWithWeakReplaceLast("a,b,c", "d,e,f"));

        // 测试用例 5：部分相同的字符串
        System.out.println(compareWithWeakReplaceLast("a,b,c", "a,x,c,q"));

        // 测试用例 6：新增、替换、删除混合情况
        System.out.println(compareWithWeakReplaceLast("a,b,c,q,x,y,z", "d,b,e,f,z"));


        System.out.println("\n=== 测试 replaceElements 方法 ===");
        String originStr = "a,b,c,d";
        String replacedString = replaceSplitValueIfMatch(originStr, "b", "z");
        System.out.println("替换后数组: " + replacedString);

        System.out.println("\n=== 测试 deleteElements 方法 ===");
        String deletedString = removeSplitValueIfMatch(originStr, "b");
        System.out.println("删除后数组: " + deletedString);
    }

    /**
     * @param args 参数
     */
    public static void main(String[] args) {
        test();
    }
}
