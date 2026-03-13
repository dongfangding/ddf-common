package com.ddf.boot.common.governance.util;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.governance.mail.MailService;
import java.io.File;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Mail helper utility.
 *
 * @author dongfang.ding
 */
@Slf4j
public class MailUtil {

    public static void sendMimeMail(String[] sendTo, String[] cc, String subject, String content,
            Map<String, File> attachment) {
        try {
            final MailService mailService = SpringContextHolder.getBean(MailService.class);
            mailService.sendMimeMail(sendTo, cc, subject, content, attachment);
        } catch (Exception e) {
            log.error("mail send failed because governance mail service is unavailable", e);
            throw new ServerErrorException(BaseErrorCallbackCode.MAIL_SEND_FAILURE);
        }
    }

    public static void sendMimeMail(String[] sendTo, String subject, String content) {
        sendMimeMail(sendTo, null, subject, content, null);
    }

    public static void sendMimeMail(String[] sendTo, String[] cc, String subject, String content) {
        sendMimeMail(sendTo, cc, subject, content, null);
    }
}
