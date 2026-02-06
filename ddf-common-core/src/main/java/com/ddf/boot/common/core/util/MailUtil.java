package com.ddf.boot.common.core.util;



import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

/**
 * 邮件发送工具类
 *
 * @author dongfang.ding on 2018/5/31
 */
@Slf4j
public class MailUtil {

    private static final JavaMailSenderImpl mailSender;
    private static final MailProperties mailProperties;

    static {
        mailSender = SpringContextHolder.getBeanWithStatic(JavaMailSenderImpl.class);
        mailProperties = SpringContextHolder.getBeanWithStatic(MailProperties.class);
    }

    /**
     * 发送带附件的和支持html格式内容的邮件内容
     *
     * @param sendTo     收件人
     * @param subject    主题
     * @param content    内容
     * @param attachment 附件
     */
    public static void sendMimeMail(String[] sendTo, String[] cc, String subject, String content,
            Map<String, File> attachment) {
        Assert.notNull(mailSender, "未配置邮件相关参数");
        Assert.notNull(mailProperties, "未配置邮件相关参数");
        // 1、创建一个复杂的消息邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            // 邮件设置
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setTo(sendTo);
            if (cc != null && cc.length > 0) {
                helper.setBcc(cc);
            }
            final Map<String, String> properties = mailProperties.getProperties();
            // 经过测试，这个必须要写，而且必须要和配置的邮箱认证的用户名一致
            helper.setFrom(properties.get("from"));

            // 上传文件
            if (attachment != null && !attachment.isEmpty()) {
                attachment.forEach((attachmentFilename, file) -> {
                    try {
                        helper.addAttachment(attachmentFilename, file);
                    } catch (jakarta.mail.MessagingException e) {
                        log.error(
                                "邮件发送失败, sendTo = {}, cc = {}, subject = {}, content = {}",
                                Arrays.toString(sendTo), Arrays.toString(cc), subject, content
                        );
                    }
                });
            }
        } catch (Exception e) {
            log.error(
                    "邮件发送失败, sendTo = {}, cc = {}, subject = {}, content = {}", Arrays.toString(sendTo),
                    Arrays.toString(cc), subject, content
            );
            throw new ServerErrorException(BaseErrorCallbackCode.MAIL_SEND_FAILURE);
        }
        mailSender.send(mimeMessage);
    }

    public static void sendMimeMail(String[] sendTo, String subject, String content) {
        sendMimeMail(sendTo, null, subject, content, null);
    }

    public static void sendMimeMail(String[] sendTo, String[] cc, String subject, String content) {
        sendMimeMail(sendTo, cc, subject, content, null);
    }
}
