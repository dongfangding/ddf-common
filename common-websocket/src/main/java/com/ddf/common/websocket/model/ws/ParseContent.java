package com.ddf.common.websocket.model.ws;

import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.websocket.enumerate.BillTypeEnum;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.enumerate.OutQRCodeTypeEnum;
import com.ddf.common.websocket.exception.InvalidFutureTimeException;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.entity.PlatformMessageTemplate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * 存入解析后的短信内容
 *
 * @author dongfang.ding
 * @date 2019/9/6 18:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class ParseContent {

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("0.00");

    /**
     * 付款人
     */
    private String payName;

    /**
     * 付款卡号
     */
    private String payNo;

    /**
     * 入账方式
     * 如 银联入账、转账存入
     */
    private String payType;

    /**
     * 付款银行
     */
    private String payBankName;

    /**
     * 收款银行名称
     */
    private String bankName;

    /**
     * 收款银行卡号
     */
    private String bankCardNo;

    /**
     * 收款人
     */
    private String receiverName;

    /**
     * 收款金额
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    private BigDecimal amount;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 由于两个变量在元数据中是放在一起且没有分隔符的，所以定义成一个变量按照原数据格式固定解析
     * 到账时间与入账方式连接在一起的数据，定义成一个变量然后解析
     */
    private String payNameContactMonth;


    /**
     * 分连接付款方式的变量合体
     */
    private String minuteContactPayType;

    /**
     * 收款时间
     */
    private String orderTimeStr;

    /**
     * 收件时间，到账时间
     */
    private Date orderTime;

    /**
     * 订单号或其它的数据唯一标识符，用以过滤重复数据避免重复插入
     */
    private String tradeNo;

    /**
     * 发件银行电话
     */
    private String bankTel;

    /**
     * 备注
     */
    private String mark;

    /**
     * 将解析后的数据填入模板最终返回的字符串
     */
    private String parseContent;

    /**
     * 验证码
     */
    private String verifyCode;

    /**
     * 模板
     */
    private PlatformMessageTemplate platformMessageTemplate;

    /**
     * 是否垃圾短信
     */
    private boolean garbage;

    /**
     * 指令码
     */
    private CmdEnum cmd;

    /**
     * 数据来源
     * @see MerchantMessageInfo#getSourceType()
     */
    private Byte sourceType;


    private String year;

    private String month;

    private String day;

    private String hour;

    private String minute;

    private String seconds;

    /**
     * 收款还是转账
     */
    private BillTypeEnum billTypeEnum;

    /**
     * 将短信解析到解析对象中
     *
     * @param smsContent
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 18:15
     */
    public ParseContent byBankSms(SmsContent smsContent, CmdEnum cmd) {
        this.setTradeNo(smsContent.getPrimaryKey()).setCmd(cmd);
        fixedOrderTime(smsContent.getReceiveTime());
        if (PlatformMessageTemplate.Type.BANK_INCOME_SMS.getValue().equals(this.getPlatformMessageTemplate().getType())) {
            this.setSourceType(MerchantMessageInfo.SOURCE_TYPE_INCOME_BANK_SMS);
            this.billTypeEnum = BillTypeEnum.INCOME;
        } else if (PlatformMessageTemplate.Type.BANK_PAY_SMS.getValue().equals(this.getPlatformMessageTemplate().getType())) {
            this.setSourceType(MerchantMessageInfo.SOURCE_TYPE_PAY_BANK_SMS);
            this.billTypeEnum = BillTypeEnum.PAY;
        }
        return this;
    }

    /**
     * 解析云闪付账单构建匹配订单业务类
     *
     * @param uPayMessage
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 14:19
     */
    public void byUPayMessage(UPayMessage uPayMessage, CmdEnum cmd) {
        this.setTradeNo(uPayMessage.getOrderId()).setCmd(cmd).setOrderTime(new Date(uPayMessage.getOrderTime()));
        if (PlatformMessageTemplate.Type.UNION_PAY_NORMAL_INCOME_MESSAGE.getValue().equals(platformMessageTemplate.getType())) {
            setSourceType(MerchantMessageInfo.SOURCE_TYPE_NORMAL_INCOME_UPAY_MESSAGE);
            setBillTypeEnum(BillTypeEnum.INCOME);
        } else if (PlatformMessageTemplate.Type.UNION_PAY_MERCHANT_INCOME_MESSAGE.getValue().equals(platformMessageTemplate.getType())) {
            setSourceType(MerchantMessageInfo.SOURCE_TYPE_MERCHANT_INCOME_UPAY_MESSAGE);
            setBillTypeEnum(BillTypeEnum.INCOME);
        } else if (PlatformMessageTemplate.Type.UNION_PAY_PAY_MESSAGE.getValue().equals(platformMessageTemplate.getType())) {
            setSourceType(MerchantMessageInfo.SOURCE_TYPE_PAY_UPAY_MESSAGE);
            setBillTypeEnum(BillTypeEnum.PAY);
        }
    }

    /**
     * 根据账单构建解析对象
     *
     * @param uPayBill
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 14:24
     */
    public static ParseContent byUPayBill(UPayBill uPayBill, CmdEnum cmd) {
        ParseContent parseContent = new ParseContent();
        parseContent.setTradeNo(uPayBill.getTradeNo()).setPayNo(uPayBill.getPayNo()).setPayName(uPayBill
                .getPayName()).setMark(uPayBill.getMark()).setAmount(uPayBill.getAmount()).setCmd(cmd)
                .setParseContent(uPayBill.toString()).setBillTypeEnum(uPayBill.getBillType());
        if (BillTypeEnum.INCOME.equals(uPayBill.getBillType())) {
            if (OutQRCodeTypeEnum.NORMAL_QRCODE.equals(uPayBill.getQrCodeType())) {
                parseContent.setSourceType(MerchantMessageInfo.SOURCE_TYPE_NORMAL_INCOME_UPAY_BILL_ORDER);
            } else if (OutQRCodeTypeEnum.MERCHANT_QRCODE.equals(uPayBill.getQrCodeType())) {
                parseContent.setSourceType(MerchantMessageInfo.SOURCE_TYPE_MERCHANT_INCOME_UPAY_BILL_ORDER);
            }
        } else if (BillTypeEnum.PAY.equals(uPayBill.getBillType())) {
            parseContent.setSourceType(MerchantMessageInfo.SOURCE_TYPE_PAY_UPAY_BILL_ORDER);
        }
        return parseContent;
    }

    /**
     * 校验必须包含信息
     */
    public void checkRequired() {
        if (cmd == null) {
            throw new GlobalCustomizeException("解析后需要将指令码放入解析对象！");
        }
        if (StringUtils.isBlank(tradeNo)) {
            throw new GlobalCustomizeException("数据主键tradeNo不能为空！");
        }

        if (CmdEnum.UPAY_MESSAGE.equals(cmd) || CmdEnum.BANK_SMS.equals(cmd)) {
            if (platformMessageTemplate == null) {
                throw new GlobalCustomizeException("解析过程未将模板返回！");
            }
        }

        if (sourceType == null) {
            throw new GlobalCustomizeException("数据来源不能为空！");
        }
    }

    /**
     * 修正订单时间
     * 由于从短信中取出的时间格式不一致，因此进行修正，将缺失的部分用当前时间填充。
     * 但是如果短信解析如果跨天跨月跨年而内容本身又没有这个时间部分的数据时，使用当前时间填充就会出现问题。
     * 因为再使用客户端传过来的数据进行验证，如果自己填充的时间存在跨天跨月跨年，则使用客户端传送的时间
     */
    public void fixedOrderTime(Long receiverTime) {
        if (receiverTime == null) {
            throw new GlobalCustomizeException("收件时间为空，不能修正时间！");
        }
        Calendar receiverCalendar = new GregorianCalendar();
        receiverCalendar.setTimeInMillis(receiverTime);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        if (getYear() == null) {
            if (receiverCalendar.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) {
                setYear(receiverCalendar.get(Calendar.YEAR) + "");
            } else {
                setYear(calendar.get(Calendar.YEAR) + "");
            }
        }
        if (getMonth() == null) {
            if (receiverCalendar.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
                setMonth(receiverCalendar.get(Calendar.MONTH) + 1 + "");
            } else {
                setMonth(calendar.get(Calendar.MONTH) + 1 + "");
            }
        }
        if (getDay() == null) {
            if (receiverCalendar.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)) {
                setDay(receiverCalendar.get(Calendar.DAY_OF_MONTH) + "");
            } else {
                setDay(calendar.get(Calendar.DAY_OF_MONTH) + "");
            }
        }
        if (getHour() == null) {
            if (receiverCalendar.get(Calendar.HOUR_OF_DAY) != calendar.get(Calendar.HOUR_OF_DAY)) {
                setHour(receiverCalendar.get(Calendar.HOUR_OF_DAY) + "");
            } else {
                setHour(calendar.get(Calendar.HOUR_OF_DAY) + "");
            }
        }
        if (getMinute() == null) {
            if (receiverCalendar.get(Calendar.MINUTE) != calendar.get(Calendar.MINUTE)) {
                setMinute(receiverCalendar.get(Calendar.MINUTE) + "");
            } else {
                setMinute(calendar.get(Calendar.MINUTE) + "");
            }
        }
        // FIXME 时间存在误差，订单的创建时间晚于付款时间。短信中没有秒，客户端传的秒又晚于订单。。。
        setSeconds("59");
/*        if (getSeconds() == null) {
            if (receiverCalendar.get(Calendar.SECOND) != calendar.get(Calendar.SECOND)) {
                setSeconds(receiverCalendar.get(Calendar.SECOND) + "");
            } else {
                setSeconds(calendar.get(Calendar.SECOND) + "");
            }
        }*/
        String timeStr = getYear() + "-" + getMonth() + "-" + getDay() + " " + getHour() + ":" + getMinute()
                + ":" + getSeconds();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            setOrderTimeStr(timeStr);
            setOrderTime(simpleDateFormat.parse(timeStr));
        } catch (Exception e) {
            log.error("时间解析转换失败！timeStr=>{}", timeStr);
            throw new GlobalCustomizeException("时间解析失败！");
        }
        // 给定一个误差值
        long currTime = System.currentTimeMillis();
        if (getOrderTime().getTime() - currTime > 59 * 1000) {
            throw new InvalidFutureTimeException(String.format("时间解析出错，出现未来时间！当前时间: %s, " +
                    "解析后的时间: %s", currTime, getOrderTime().getTime()));
        }
    }

    /**
     * 拼接到账消息， 包含云闪付到账消息，账单消息， 短信消息
     * 
     * @author dongfang.ding
     * @date 2019/9/7 14:25 
     */
    public String buildMessage() {
        if (StringUtils.isNotBlank(parseContent)) {
            return parseContent;
        }
        StringBuilder stringBuilder = new StringBuilder(30);
        stringBuilder.append("您的账号");
        if (StringUtils.isNotBlank(bankCardNo)) {
            stringBuilder.append("[").append(bankCardNo).append("]");
        }
        stringBuilder.append("于").append(orderTimeStr).append("收入金额").append(amount).append("元 ");
        if (StringUtils.isNotBlank(payName)) {
            stringBuilder.append("付款人").append(payName).append(" ");
        }
        if (StringUtils.isNotBlank(payNo)) {
            stringBuilder.append("付款卡号").append(payNo).append(" ");
        }
        if (StringUtils.isNotBlank(payBankName)) {
            stringBuilder.append("付款银行").append(payBankName).append(" ");
        }
        if (StringUtils.isNotBlank(mark)) {
            stringBuilder.append("备注[").append(mark).append("]");
        }
        return stringBuilder.toString();
    }
}