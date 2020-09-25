package com.ddf.boot.common.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

/**
 *
 * 邮件发送工具类,邮件发送全部采取异步处理，暂时不考虑由使用人员决定是否异步，后续有需求，会在修改；
 *
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
 * @author dongfang.ding on 2018/5/31
 */
@Component
public class MailUtil {

    private final Logger log = LoggerFactory.getLogger(MailUtil.class);

    @Autowired(required = false)
    private JavaMailSenderImpl mailSender;
    @Autowired(required = false)
    private MailProperties mailProperties;

    /**
     * 发送带附件的和支持html格式内容的邮件内容
     * @param sendTo 收件人
     * @param subject 主题
     * @param content 内容
     * @param attachment 附件
     * @throws MessagingException
     */
    @Async
    public void sendMimeMail(String[] sendTo, String[] cc, String subject, String content, Map<String, File> attachment)
            throws MessagingException {
        log.debug("sendSimpleMail.....to [{}]...............", String.join(",", sendTo));
        //1、创建一个复杂的消息邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        //邮件设置
        helper.setSubject(subject);
        helper.setText(content,true);
        helper.setTo(sendTo);
        if (cc != null && cc.length > 0) {
            helper.setBcc(cc);
        }
        // 经过测试，这个必须要写，而且必须要和配置的邮箱认证的用户名一致
        helper.setFrom(mailProperties.getUsername());

        //上传文件
        if (attachment != null && !attachment.isEmpty()) {
            attachment.forEach((attachmentFilename, file) -> {
                try {
                    helper.addAttachment(attachmentFilename, file);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
        }
        mailSender.send(mimeMessage);
    }

    public void sendMimeMail(String[] sendTo, String subject, String content) throws MessagingException {
        sendMimeMail(sendTo, null, subject, content, null);
    }


    public void sendMimeMail(String[] sendTo, String [] cc, String subject, String content) throws MessagingException {
        sendMimeMail(sendTo, cc, subject, content, null);
    }

}
