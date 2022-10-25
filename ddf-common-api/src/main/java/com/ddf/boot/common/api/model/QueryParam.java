package com.ddf.boot.common.api.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding on 2018/12/16
 */
@Getter
@ToString
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class QueryParam<T> implements Serializable {

    private static final long serialVersionUID = -3340461020433145490L;

    /**
     * 查询字段
     */
    private String key;
    /**
     * 字段与属性的关系
     */
    private Op op;
    /**
     * 字段的属性值
     */
    private T value;
    /**
     * 该字段查询条件与其它字段的关系
     */
    private Relative relative;
    /**
     * 分组名称，相同的分组名称的查询条件会放在一个()里处理，方便可以and or 一起使用，但又与其它条件是and
     */
    private String groupName;

    /**
     * 不需要分组使用的查询条件构造函数
     */
    public QueryParam(String key, Op op, T value, Relative relative) {
        this.key = key;
        this.op = op;
        this.value = value;
        this.relative = relative;
    }

    /**
     * 条件相等关系为AND的简写
     *
     * @param key   字段
     * @param value 值
     */
    public QueryParam(String key, T value) {
        this.key = key;
        this.op = Op.EQ;
        this.value = value;
        this.relative = Relative.AND;
    }


    /**
     * 多个条件为AND关系的简写
     *
     * @param key   字段
     * @param value 值
     */
    public QueryParam(String key, Op op, T value) {
        this.key = key;
        this.op = op;
        this.value = value;
        this.relative = Relative.AND;
    }

    public enum Relative {
        /**
         * 与其它条件为and关系
         */
        AND("AND"),
        /**
         * 与其它关系为or关系
         */
        OR("OR");
        private String value;

        Relative(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public enum Op {

        /**
         * 相等
         */
        EQ("="),
        /**
         * 相似
         */
        LIKE("LIKE"),
        /**
         * 大于等于
         */
        GE(">="),
        /**
         * 大于
         */
        GT(">"),
        /**
         * 小于等于
         */
        LE("<="),
        /**
         * 小于
         */
        LT("<"),
        /**
         * IN
         */
        IN("IN"),
        /**
         * 不相似
         */
        NK("NOT LIKE"),
        /**
         * 不等于
         */
        NE("<>"),
        /**
         * IS NOT NULL
         */
        NN("IS NOT NULL"),
        /**
         * IS NULL
         */
        NI("IS NULL");

        private String value;

        Op(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
