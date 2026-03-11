package com.ddf.boot.common.governance.mail;

import java.io.File;
import java.util.Map;

/**
 * Governance mail service abstraction.
 *
 * @author dongfang.ding
 * @since 2026/3/11
 */
public interface MailService {

    void sendMimeMail(String[] sendTo, String[] cc, String subject, String content, Map<String, File> attachment);

    default void sendMimeMail(String[] sendTo, String subject, String content) {
        sendMimeMail(sendTo, null, subject, content, null);
    }

    default void sendMimeMail(String[] sendTo, String[] cc, String subject, String content) {
        sendMimeMail(sendTo, cc, subject, content, null);
    }
}
