package com.ddf.boot.common.websocket.helper;

import com.ddf.boot.common.exception.GlobalCustomizeException;
import com.ddf.boot.common.util.JsonUtil;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.boot.common.websocket.model.ws.ParseContent;
import com.ddf.boot.common.websocket.model.ws.TemplateConditionEl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 银行短信解析工具类
 *


 */
@Slf4j
public class SmsParseUtil {
    /**
     * 短信模板左边的标识位
     */
    private static final String LEFT = "${";
    /**
     * 短信模板右边的标识位
     */
    private static final String RIGHT = "}";

    /**
     * 解析短信模板数据
     * <p>
     * String description = "您尾号为9982的银行卡于28日16时12分入账1.00元";
     * String template = "您尾号为${尾号}的银行卡于${时间}入账${金额}元";
     *  return {尾号=9982, 时间=28日16时12分, 金额=1.00}
     *
     *  为了后端能够取到值，因此，key是固定的，让配置模板的时候自己选择这几个key,但是值必须是固定的，
     *  这一块配置表的时候让前端选择展示的汉字，存入的是code，对应关系如下
     *  银行卡号 => bankCardNo
     *  交易时间 => orderTimeStr
     *  入账金额 => amount
     *  付款人   => payName
     *  付款卡号 => payNo
     *  银行名   => payBankName
     *
     * @param content
     * @param template
     * @return
     */
    public static Map<String, String> parse(String content, String template) {
        if (StringUtils.isAnyBlank(content, template)) {
            throw new GlobalCustomizeException("template不能为空!");
        }
        Map<String, String> rtnMap = new HashMap<>();
        int pointLeft, pointRight;
        String leftVal, rightVal;
        int contentLeft, contentRight = 0;
        String tempStr, parseVal, key, parseContent = template;
        // 一直找到字符串中没有需要解析的标识位数据
        while (template.contains(LEFT) && template.contains(RIGHT)) {
            // 获取第一个左标识位左边第一个元素的角标，即第一个${左边的第一个元素的位置
            pointLeft = template.indexOf(LEFT) - 1;
            // 获取第一个右标识位右边第一个元素的角标，即第一个}右边的第一个元素的位置
            pointRight = template.indexOf(RIGHT) + 1;
            // 获取key,位于大括号之间，您尾号为${尾号}的,pointLeft的值是`为`的角标，pointRight是`的`角标
            key = template.substring(pointLeft + 3, pointRight - 1);

            // 获取左边第一个元素的值
            leftVal = template.substring(0, pointLeft + 1);
            // 舍弃右边元素左边的所有数据
            tempStr = template.substring(pointRight);


            // 获取模板中左边元素内容对应在原始数据中的角标
            contentLeft = content.indexOf(leftVal) + leftVal.length();

            // 判断}右边元素的内容，一直到下个${，如果没有，则右边元素的内容为剩余所有字符
            if (tempStr.contains(LEFT)) {
                rightVal = template.substring(pointRight, pointRight + tempStr.indexOf(LEFT));
                // 获取模板中右边元素内容对应在原始数据中的角标
                contentRight = content.indexOf(rightVal, contentLeft);
                // 截取左右两个临界点，获取中间值，即为解析内容
                parseVal = content.substring(contentLeft, contentRight);
            } else if ("".equals(tempStr)) {
                // 如果是以变量结尾的，说明已经解析到最后，变量的内容就是剩余部分
                parseVal = content.substring(contentLeft);
            } else {
                rightVal = template.substring(pointRight);
                // 获取模板中右边元素内容对应在原始数据中的角标
                contentRight = content.indexOf(rightVal, contentLeft);
                // 截取左右两个临界点，获取中间值，即为解析内容
                parseVal = content.substring(contentLeft, contentRight);
            }


            // 将解析后的内容填充到模板中，最后将填充后的完整值返回，方便判断是否和content一致
            rtnMap.put(key, fixedValue(key, parseVal));
            parseContent = parseContent.replace(LEFT + key + RIGHT, parseVal);
            rtnMap.put("parseContent", parseContent);

            if ("".equals(tempStr)) {
                break;
            }
            // 每次解析之后，要把之前已经解析过字符串从原始字符中截取掉，否则indexOf无法处理
            content = content.substring(contentRight);
            template = template.substring(pointRight);
        }
        return rtnMap;
    }


    /**
     * 处理特殊字段，有的金额使用的是千分位，因此直接当数字使用是有问题的
     * @param key
     * @param value
     * @return
     */
    private static String fixedValue(String key, String value) {
        if (!"amount".equals(key) && !"balance".equals(key)) {
            return value;
        }
        return value.replaceAll(",", "");
    }

    /**
     *
     *
     * 处理特殊逻辑，一个连续的字符中包含多个变量的值但是却没有分隔符，因此通过定义多个变量无法解析出来，只能
     * 根据规律固定逻辑写死
     * 比如 张飞09月12  包含了两个信息 人物和时间
     * @param parseContent
     */
    private static void fixedNotNormalVariable(ParseContent parseContent) {
        // 以下逻辑是一段完全写死的逻辑
        String payNameContactMonth = parseContent.getPayNameContactMonth();
        String minuteContactPayType = parseContent.getMinuteContactPayType();
        boolean isFixed = payNameContactMonth != null || minuteContactPayType != null;
        if (!isFixed) {
            return;
        }
        String bankName = parseContent.getBankName();
        if (StringUtils.isNotBlank(payNameContactMonth)) {
            if ("建设银行".equals(bankName)) {
                for (int i = 0; i < payNameContactMonth.length(); i++) {
                    if (Character.isDigit(payNameContactMonth.charAt(i))) {
                        parseContent.setTargetAccountName(payNameContactMonth.substring(0, i));
                        parseContent.setMonth(payNameContactMonth.substring(i));
                        break;
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(minuteContactPayType)) {
            if ("平安银行".equals(bankName) || "招商银行".equals(bankName) || "工商银行".equals(bankName)
                    || "交通银行".equals(bankName)) {
                for (int i = 0; i < minuteContactPayType.length(); i++) {
                    if (!Character.isDigit(minuteContactPayType.charAt(i))) {
                        parseContent.setMinute(minuteContactPayType.substring(0, i));
                        parseContent.setPayType(minuteContactPayType.substring(i));
                        break;
                    }
                }
            }
        }
    }


    /**
     * 将解析后的短信内容转换为对象，固定格式
     *
     * @param content
     * @param template
     * @return
     */
    public static ParseContent parseToObj(String content, String template) {
        Map<String, String> parse = parse(content, template);
        ParseContent parseContent = JsonUtil.toBean(JsonUtil.asString(parse), ParseContent.class);
        fixedNotNormalVariable(parseContent);
        return parseContent;
    }

    /**
     * 条件表达式，先做一个简单的实现，后续看时间将规则补充进来
     * NOT_CONTAINS 不包含
     * CONTAINS 包含
     *
     * 实际保存数据{"condition": "NOT_CONTAINS", "value": "[\"银行\", \"元\"]"}
     * @param templates
     * @param content
     * @return
     */
    public static boolean isGarbage(List<PlatformMessageTemplate> templates, String content) {
        if (templates == null || templates.isEmpty()) {
            return false;
        }
        String templateContext;
        TemplateConditionEl.ConditionKey conditionKey;
        for (PlatformMessageTemplate template : templates) {
            templateContext = template.getTemplateContext();
            if (StringUtils.isBlank(templateContext)) {
                continue;
            }
            TemplateConditionEl conditionEl;
            try {
                conditionEl = JsonUtil.toBean(templateContext, TemplateConditionEl.class);
                if (conditionEl == null) {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
            conditionKey = conditionEl.getCondition();
            if (conditionKey == null) {
                continue;
            }
            String[] conditionValue;
            switch (conditionKey) {
                case CONTAINS:
                    conditionValue = conditionEl.getValue().split("[,，]");
                    if (conditionValue.length == 0) {
                        return false;
                    }
                    for (String value : conditionValue) {
                        if (content.contains(value)) {
                            return true;
                        }
                    }
                    break;
                case NOT_CONTAINS:
                    conditionValue = conditionEl.getValue().split("[,，]");
                    if (conditionValue.length == 0) {
                        return false;
                    }
                    for (String value : conditionValue) {
                        if (!content.contains(value)) {
                            return true;
                        }
                    }
                    break;
                case EQUALS:
                    String conditionValueString = conditionEl.getValue();
                    if (Objects.equals(conditionValueString, content)) {
                        return true;
                    }
                    break;
                default: return false;
            }
        }
        return false;
    }



    public static void main(String[] args) {
        String content = "您的借记卡账户2134，于12月12日POS收入人民币10.86元，交易后余额58.37";
        String template = "您的借记卡账户${bankCardNo}，于${month}月${day}日POS收入人民币${amount}元，交易后余额${balance}";
        ParseContent parseContent = parseToObj(content, template);
        System.out.println("parseContent = " + parseContent);
        System.out.println(content.equals(parseContent.getParseContent()));


        parseInAmount();
        parseOutAmount();
    }

    private static void parseInAmount() {
        List<String> templateList = new ArrayList<>();
        // 光大银行 ：95595
        templateList.add("尊敬的客户：您尾号${bankCardNo}账户${hour}:${minute}存入${amount}元，余额${balance}元，摘要:${payType}—付款方姓名:${payName},付款方账号后四位:${payNo}。[${payBankName}]");
        // 广发银行 95508
        templateList.add("【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}收入人民币${amount}元（${payType}-${payName}）。");
        // 上海浦东发展银行 ：95528
        templateList.add("您尾号${bankCardNo}卡人民币活期${hour}:${minute}存入${amount}[${payType}:${payName}]，可用余额${balance}。【${bankName}】");
        // 中信银行 9555801
        templateList.add("【${bankName}】您尾号${bankCardNo}的中信卡于${month}月${day}日${hour}:${minute}，${payType}存入人民币${amount}元，当前余额为人民币${balance}元。");
        // 民生银行 95568
        templateList.add("账户${bankCardNo}于${month}月${day}日${hour}:${minute}存入￥${amount}元，可用余额${balance}元。${payType}。【${bankName}】");
        // 平安银行 ：106927995511
        templateList.add("您尾号${bankCardNo}的账户于${month}月${day}日${hour}:${minuteContactPayType}转入人民币${amount}元。【${bankName}】");
        templateList.add("您存款账户${bankCardNo}于${month}月${day}日${hour}:${minuteContactPayType}转入人民币${amount}元，详见 pingan.com/foMI【${bankName}】");
        // 招商银行 ：95555
        templateList.add("您账户${bankCardNo}于${month}月${day}日${hour}:${minuteContactPayType}（${payName}），人民币${amount}元[${bankName}]");
        templateList.add("您账户${bankCardNo}于${month}月${day}日${hour}:${minute}收到${payType}人民币${amount}，付方${payName}，账号尾号${payNo}，备注：转账[${bankName}]");
        // 华夏银行 ：95577
        templateList.add("您的账户${bankCardNo}于${month}月${day}日${hour}:${minute}收入人民币${amount}元，余额${balance}元。${payType}，付款方${payName}。【${bankName}】");
        // 中国邮政 ：95580 【邮储银行】19年09月08日11:01您尾号888账户银联入账金额1.00元，余额25.03元
        templateList.add("【${bankName}】${year}年${month}月${day}日${hour}:${minute}您尾号${bankCardNo}账户${payType}金额${amount}元，余额${balance}元。");
        // 工商银行 95588
        templateList.add("您尾号${bankCardNo}卡${month}月${day}日${hour}:${minuteContactPayType})${amount}元，余额${balance}元。【${bankName}】");
        // 交通银行 ：95559
        templateList.add("您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minuteContactPayType}转入${amount}元,交易后余额为${balance}元。【${bankName}】");
        // 建设银行 95533
        templateList.add("${payNameContactMonth}月${day}日${hour}时${minute}分向您尾号${bankCardNo}的储蓄卡账户${payType}收入人民币${amount}元,活期余额${balance}元。[${bankName}]");
        templateList.add("您尾号${bankCardNo}的储蓄卡账户${month}月${day}日${hour}时${minute}分${payType}收入人民币${amount}元,活期余额${balance}元。[${bankName}]");




        List<String> contentList = new ArrayList<>();
        contentList.add("尊敬的客户：您尾号2989账户20:57存入1元，余额12.93元，摘要:银联入账—付款方姓名:何露,付款方账号后四位:5232。[光大银行]");
        contentList.add("【广发银行】您尾号2531卡10日11:51收入人民币1.33元（银联入账-魏世兴）。");
        contentList.add("您尾号3252卡人民币活期14:39存入1.00[银联入账:何露]，可用余额5.09。【浦发银行】");
        contentList.add("【中信银行】您尾号4924的中信卡于10月10日17:36，转账存入人民币1.00元，当前余额为人民币3.42元。");
        contentList.add("【中信银行】您尾号4924的中信卡于10月11日09:59，ATM行外转账转入存入人民币1.00元，当前余额为人民币3.42元。");
        contentList.add("账户*0936于07月29日09:54存入￥0.01元，可用余额5.01元。银联入账。【民生银行】");
        contentList.add("账户*0936于07月29日09:54存入￥100.00元，可用余额100.01元。银联入账：业务组。【民生银行】");
        contentList.add("您尾号3104的账户于7月29日11:28银联入账转入人民币0.01元。【平安银行】");
        contentList.add("您存款账户3104于12月29日11:29银联入账转入人民币300元，详见 pingan.com/foMI【平安银行】");
        contentList.add("您账户0537于10月11日11:53二维码收款（何露/业务组），人民币1.27元[招商银行]");
        contentList.add("您账户0537于08月02日08:54二维码收款（何露），人民币0.01元[招商银行]");
        contentList.add("您账户2102于10月12日收到本行转入人民币5000.12，付方何露，账号尾号0537，备注：转账[招商银行]");
        contentList.add("您的账户2931于07月29日11:29收入人民币0.01元，余额0.86元。银联入账，付款方魏世兴。【华夏银行】");
        contentList.add("【邮储银行】19年09月08日11:01您尾号888账户银联入账金额1.00元，余额25.03元。");
        contentList.add("您尾号2631卡9月8日11:26网上银行收入(银联入账)1元，余额4.77元。【工商银行】");
        contentList.add("您尾号*1923的卡于09月08日11:04网络支付转入30.00元,交易后余额为31.47元。【交通银行】");

        contentList.add("魏世兴10月11日10时25分向您尾号7149的储蓄卡账户银联入账收入人民币15.10元,活期余额26.98元。[建设银行]");
        contentList.add("魏世兴10月12日11时53分向您尾号7149的储蓄卡账户转账存入收入人民币11.10元,活期余额23.98元。[建设银行]");
        contentList.add("魏世兴10月13日13时35分向您尾号7149的储蓄卡账户电子汇入收入人民币21.10元,活期余额21.98元。[建设银行]");
        contentList.add("您尾号5232的储蓄卡账户9月29日17时6分银联入账收入人民币1.21元,活期余额1054.40元。[建设银行]");
        contentList.add("您尾号5232的储蓄卡账户9月29日16时15分银联入账收入人民币1.30元,活期余额1051.22元。[建设银行]");

        long now = System.currentTimeMillis();


        List<String> failureList = new ArrayList<>();
        for (String s : contentList) {
            boolean isSuccess = false;
            ParseContent parseContent = null;
            for (String template : templateList) {
                try {
                    parseContent = parseToObj(s, template);
                    isSuccess = true;
                } catch (Exception e) {
                }
            }
            System.out.println("原始数据： " + s);
            if (isSuccess) {
                parseContent.fixedOrderTime(1526832000000L);
                System.out.println(parseContent);
                System.out.println(parseContent.getOrderTimeStr());
                System.out.println(parseContent.getOrderTime());
                System.out.println("\n\n");
            } else {
                System.out.println("解析失败\n\n\n");
                failureList.add(s);
            }
        }

        System.out.println("解析失败的数据");
        for (String s : failureList) {
            System.out.println(s);
        }
        System.out.println("共耗时: " + (System.currentTimeMillis() - now));
    }



    private static void parseOutAmount() {
        List<String> templateList = new ArrayList<>();
        // 光大银行 ：95595
        templateList.add("尾号${bankCardNo}账户${hour}:${minute}支出${amount}元，余额${balance}元，摘要:${payType} 二维码快速收款码专用。[${bankName}]");
        // 广发银行 95508
        templateList.add("【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}支出人民币${amount}元(${payType})。");
        // 上海浦东发展银行 ：95528
        templateList.add("您尾号${bankCardNo}卡${hour}:${minute}消费${amount}[${payType}],可用余额${balance}【${bankName}】");
        // 中信银行 9555801
        templateList.add("【${bankName}】您尾号${bankCardNo}的中信卡于${month}月${day}日${hour}:${minute}，${payType}人民币${amount}元，当前余额为人民币${balance}元。");
        // 民生银行 95568
        templateList.add("账户${bankCardNo}于${month}月${day}日${hour}:${minute}支出￥${amount}元，可用余额${balance}元。${payType}。【${bankName}】");
        // 平安银行 ：106927995511
        templateList.add("您尾号${bankCardNo}的账户于${month}月${day}日${hour}:${minuteContactPayType}转出人民币${amount}元。【${bankName}】");
        // 招商银行 ：95555
        templateList.add("您账户${bankCardNo}于${month}月${day}日${hour}:${minute}向${payName}做${payType}，人民币${amount}元[${bankName}]");
        // 华夏银行 ：95577
        templateList.add("您的账户${bankCardNo}于${month}月${day}日${hour}:${minute}支出人民币${amount}元，余额${balance}元。${payType}。【${bankName}】");
        // 中国邮政 ：95580
        templateList.add("【${bankName}】${year}年${month}月${day}日${hour}:${minute}您尾号${bankCardNo}账户消费金额${amount}元，余额${balance}元。");
        // 工商银行 95588
        templateList.add("您尾号${bankCardNo}卡${month}月${day}日${hour}:${minute}工商银行支出(${payType})${amount}元，余额${balance}元。【${bankName}】");
        // 交通银行 ：95559
        templateList.add("您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minute}网络支付异地消费${amount}元,交易后余额为${balance}元。【${bankName}】");
        templateList.add("您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minute}网络支付转出${amount}元,交易后余额为${balance}元。【${bankName}】");
        // 建设银行 95533
        templateList.add("您尾号${bankCardNo}的储蓄卡账户${month}月${day}日${hour}时${minute}分向${payType}支出人民币${amount}元,活期余额${balance}元。[建设银行]");


        List<String> contentList = new ArrayList<>();
        contentList.add("尾号2989账户21:00支出1元，余额11.93元，摘要:二维码消费 二维码快速收款码专用。[光大银行]");
        contentList.add("【广发银行】您尾号2531卡10日14:37支出人民币1.00元(消费)。");
        contentList.add("您尾号3252卡14:59消费1.00[消费:银联扫码转账],可用余额4.09【浦发银行】");
        contentList.add("【中信银行】您尾号4924的中信卡于10月10日17:38，转账支出人民币1.00元，当前余额为人民币2.42元。");
        contentList.add("账户*0936于07月29日10:22支出￥5.00元，可用余额0.01元。跨行消费:银联扫码转账。【民生银行】");
        contentList.add("您尾号3104的账户于7月29日10:01扫码支付转出人民币1.00元。【平安银行】");
        contentList.add("您账户9593于07月29日11:53向*世兴做二维码付款，人民币0.01元[招商银行]");
        contentList.add("您的账户2931于07月29日11:28支出人民币0.30元，余额0.85元。二维码付款。【华夏银行】");
        contentList.add("【邮储银行】19年09月08日11:20您尾号888账户消费金额1.00元，余额24.03元。");
        contentList.add("您尾号2631卡10月11日11:48工商银行支出(银联)1元，余额1.58元。【工商银行】");
        contentList.add("您尾号*1923的卡于09月08日11:05网络支付异地消费2.13元,交易后余额为29.34元。【交通银行】");
        contentList.add("您尾号*1923的卡于10月10日19:54网络支付转出1.18元,交易后余额为7.07元。【交通银行】");
        contentList.add("您尾号5232的储蓄卡账户9月8日17时38分向银联扫码转账跨行其他渠道消费支出人民币1.00元,活期余额2.48元。[建设银行]");

        long now = System.currentTimeMillis();
        List<String> failureList = new ArrayList<>();
        for (String s : contentList) {
            boolean isSuccess = false;
            ParseContent parseContent = null;
            for (String template : templateList) {
                try {
                    parseContent = parseToObj(s, template);
                    isSuccess = true;
                } catch (Exception e) {
                }
            }
            System.out.println("原始数据： " + s);
            if (isSuccess) {
                parseContent.fixedOrderTime(1526832000000L);
                System.out.println(parseContent);
                System.out.println(parseContent.getOrderTimeStr());
                System.out.println(parseContent.getOrderTime());
                System.out.println("\n\n");
            } else {
                System.out.println("解析失败\n\n\n");
                failureList.add(s);
            }
        }
        System.out.println("解析失败的数据");
        for (String s : failureList) {
            System.out.println("s = " + s);
        }
        System.out.println("共耗时: " + (System.currentTimeMillis() - now));
    }
}
