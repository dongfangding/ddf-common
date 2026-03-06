package com.ddf.boot.common.api.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;


/**
 * slot连消结果简单模型, 原始对象{@code SymbolMatrixResult} , 这个模型是最基础的一个图案的属性， 不包含连消的集合
 * <p>
 * 用来导入到excel中作为自身图案和赠送图案的数据，里面的totalMultiple，不是最终的总倍数，而是自身节点的倍数
 *
 * @author snowball
 */
@Data
public class CascadeSymbolMatrixMetadata {

    /**
     * 本次基础图案倍数和
     */
    private int normalSymbolMultiple;

    /**
     * 本次魔法图案倍数和， 只有最后一个图案，不存在连消时才计算
     */
    private int magicSymbolMultiple;

    /**
     * 当前图案的免费次数
     */
    private int freeSpins;

    /**
     * 是否免费
     */
    private Boolean isFree = Boolean.TRUE;

    /**
     * 一组图案矩阵id集合， 即将原始二维数组转换为一维数组。
     * <p>
     * 如果存在多个集合，说明一定存在连消的情况，否则只有一个元素，不存在连消。
     * 当存在连消时，会生成下一组图案， 消除的图案往下坠落，然后补充对应消除数量的新图案，其它坠落的图案保持不变，只是可能位置会下坠。
     * 实际解析，如果元素没有变化，那就将ids置为-1， 标识没有变化，然后补充的图案id用来替换上一组消除图案的角，即整体未消除的图案角标不会产生变化。
     * 让前端根据数据解析去处理最终哪些要消除，以及下落消除规则。
     * 简单举例，第一组图案a,b,a,e,a。 如果1触发了消除，第二组补充3个图案id加入为d,e,f。那么ids集合的内容则如下
     * 0: a,b,a,c,a
     * 1: d,-1,e,-1,f
     */
    private List<Integer> ids;

    /**
     * 图案下落重组后的角标集合， 基于ids的索引。
     */
    private List<Integer> shiftIds;

    /**
     * 这个是处理好移位后的id集合， 即allIds数据为-1 3 -1 2 -1，
     * shiftAllIds则为处理了下落逻辑之后，即连消后的位置的id索引， 即-1 -1 -1 3 2
     * <p>
     * 所有的符号id数组，将原来的二维数组转换为一维数组集合
     */
    private List<Integer> shiftSymbolMatrixIds = new ArrayList<>();

    /**
     * 点亮的图案id角标, 基于图案下落重组前的位置数的索引。
     */
    private Set<Integer> lightIndex;

    /**
     * 匹配到的魔石的额外信息补充
     * 由于ids只包含图案的id， 如果要补充其它属性信息， 就用这个字段。用string特殊格式拼接，减少传输数据
     * 格式：index:multiple#symbolId;index:multiple#symbolId
     */
    private String magicSymbolMatchedInfo;

    /**
     * 匹配到的普通图案的额外信息补充
     * <p>
     * 由于ids只包含图案的id， 如果要补充其它属性信息， 就用这个字段。用string特殊格式拼接，减少传输数据
     * 格式：id:对应倍数;id:对应倍数
     */
    private String normalSymbolMatchedInfo;


    /**
     * 点亮的普通图标名称
     * 格式：name;name
     */
    private String lightSymbolNameStr;

}
