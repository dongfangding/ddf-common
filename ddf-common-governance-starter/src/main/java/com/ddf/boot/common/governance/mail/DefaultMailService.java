package com.ddf.boot.common.governance.mail;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default governance mail service.
 *
 * @author dongfang.ding
 * @since 2026/3/11
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMailService implements MailService {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Override
    public void sendMimeMail(String[] sendTo, String[] cc, String subject, String content,
            Map<String, File> attachment) {
        Assert.notNull(javaMailSender, "mail sender is not configured");
        Assert.notNull(mailProperties, "mail properties are not configured");
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setTo(sendTo);
            if (cc != null && cc.length > 0) {
                helper.setBcc(cc);
            }
            final String from = mailProperties.getProperties().get("from");
            if (StringUtils.hasText(from)) {
                helper.setFrom(from);
            }
            if (attachment != null && !attachment.isEmpty()) {
                attachment.forEach((attachmentFilename, file) -> {
                    try {
                        helper.addAttachment(attachmentFilename, file);
                    } catch (jakarta.mail.MessagingException e) {
                        log.error("mail attachment failed sendTo={}, cc={}, subject={}", Arrays.toString(sendTo),
                                Arrays.toString(cc), subject, e);
                    }
                });
            }
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("mail send failed sendTo={}, cc={}, subject={}", Arrays.toString(sendTo), Arrays.toString(cc),
                    subject, e);
            throw new ServerErrorException(BaseErrorCallbackCode.MAIL_SEND_FAILURE);
        }
    }
}
