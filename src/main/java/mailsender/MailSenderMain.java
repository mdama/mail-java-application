package mailsender;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class MailSenderMain {
    private static Logger logger = new Logger();

    public static void main(String[] args) {

        logger.info("args:" + Arrays.toString(args));

        try {
            MailParams mailParams = prepareMailParamFromArgs(args);
            send(mailParams);
        } catch (Exception e) {
            logger.error("An unhandled error occured.", e);
            System.exit(5);
        }
    }

    private static MailParams prepareMailParamFromArgs(String[] args) {
        Map<String, String> argsMap = new HashMap<>();

        for (String arg : args) {
            if (!arg.contains("=")) {
                logger.error("Argument format must be arg=value Arg: " + arg + " is wrong format");
                System.exit(3);
            }
            String[] argKeyValue = arg.split("=");
            argsMap.put(argKeyValue[0], argKeyValue[1]);
        }


        MailParams mailParams = new MailParams();
        mailParams.subject = argsMap.get("subject");
        mailParams.content = argsMap.get("content");
        mailParams.toEmails = Arrays.asList(argsMap.get("to-emails").split(","));
        if (StringUtils.isNotEmpty(argsMap.get("attachments")))
            mailParams.attachmentFullPaths = Arrays.asList(argsMap.get("attachments").split(","));

        if (StringUtils.isNotEmpty(argsMap.get("from")))
            mailParams.from = argsMap.get("from");

        if (StringUtils.isNotEmpty(argsMap.get("auth")))
            mailParams.auth = argsMap.get("auth");

        if (StringUtils.isNotEmpty(argsMap.get("starttls")))
            mailParams.starttls = argsMap.get("starttls");

        mailParams.host = argsMap.get("host");
        mailParams.port = Integer.parseInt(argsMap.get("port"));
        mailParams.username = argsMap.get("username");
        mailParams.password = argsMap.get("password");

        if (StringUtils.isNotEmpty(argsMap.get("html")))
            mailParams.html = Boolean.parseBoolean(argsMap.get("html"));

        return mailParams;
    }

    public static void send(MailParams mailParam) throws MessagingException {

        if (mailParam.toEmails == null || mailParam.toEmails.isEmpty()) {
            logger.error("Email List Can not be empty. Exiting!");
            System.exit(2);
            return;
        }

        Properties prop = new Properties();

        prop.put("mail.smtp.auth", mailParam.auth);
        prop.put("mail.smtp.starttls.enable", mailParam.starttls /*"true"*/);
        prop.put("mail.smtp.host", mailParam.host);
        prop.put("mail.smtp.port", mailParam.port);
        prop.put("mail.smtp.ssl.trust", mailParam.host);

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailParam.username, mailParam.password);
            }
        });

        MimeMessage message = new MimeMessage(session);

        if (StringUtils.isEmpty(mailParam.from)) {
            message.setFrom(new InternetAddress(mailParam.username));
        } else {
            message.setFrom(new InternetAddress(mailParam.from));
        }
        Address[] receipientsArr = new Address[mailParam.toEmails.size()];

        for (int i = 0; i < mailParam.toEmails.size(); i++) {
            receipientsArr[i] = new InternetAddress(mailParam.toEmails.get(i));
        }
        message.setRecipients(Message.RecipientType.TO, receipientsArr);
        message.setSubject(mailParam.subject, "utf-8");

        Multipart multipart = new MimeMultipart();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();

        if (mailParam.html)
            mimeBodyPart.setContent(mailParam.content, "text/html; charset=UTF-8");
        else
            mimeBodyPart.setText(mailParam.content);

        if (!CollectionUtils.isEmpty(mailParam.attachmentFullPaths)) {

            for (String attachmentFullPath : mailParam.attachmentFullPaths) {
                addAttachment(multipart, attachmentFullPath);
            }
        }

        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);
        Transport.send(message);
        logger.info("Mail sent. Subject : " + mailParam.subject + ", To_Emails : " + String.join(", ", mailParam.toEmails));
        System.exit(0);
    }

    private static void addAttachment(Multipart multipart, String filename) throws MessagingException {
        DataSource source = new FileDataSource(filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        String fileNameWithoutPath = new File(filename).getName();
        messageBodyPart.setFileName(fileNameWithoutPath);
        multipart.addBodyPart(messageBodyPart);
    }


}
