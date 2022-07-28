package mailsender;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class MailSenderMain {
    private static Logger logger = new Logger();

    public static void main(String[] args) {

        //logger.info("args:" + Arrays.toString(args));

        try {
            MailParams mailParams = prepareMailParamFromArgs(args);
            send(mailParams);
        } catch (Exception e) {
            logger.error("An unhandled error occured.", e);
            System.exit(5);
        }
    }

    private static MailParams prepareMailParamFromArgs(String[] args) throws IOException {

        Map<String, String> argsMapFromCmd = argsArrToMap(args);
        logger.info("Commandline Args:" + Arrays.toString(args));

        //argsmap içinde param-file varsa, dosyadan line line args arrayine oradan da mape aktarılır.
        String paramFileFullPath=argsMapFromCmd.get("param-file");
        if(StringUtils.isNotEmpty(paramFileFullPath) ) {
            List<String> lines = FileUtils.readLines(new File(paramFileFullPath), StandardCharsets.UTF_8);
            logger.info("File Args:" + Arrays.toString(lines.toArray(new String[0])));
            Map<String, String> argsMapFromFile = argsArrToMap(lines.toArray(new String[0]));

            argsMapFromFile.putAll(argsMapFromCmd);
            argsMapFromCmd=argsMapFromFile;
        }


        MailParams mailParams = new MailParams();
        mailParams.subject = argsMapFromCmd.get("subject");
        mailParams.content = argsMapFromCmd.get("content");
        mailParams.toEmails = Arrays.asList(argsMapFromCmd.get("to-emails").split(","));
        if (StringUtils.isNotEmpty(argsMapFromCmd.get("attachments")))
            mailParams.attachmentFullPaths = Arrays.asList(argsMapFromCmd.get("attachments").split(","));

        if (StringUtils.isNotEmpty(argsMapFromCmd.get("from")))
            mailParams.from = argsMapFromCmd.get("from");

        if (StringUtils.isNotEmpty(argsMapFromCmd.get("auth")))
            mailParams.auth = argsMapFromCmd.get("auth");

        if (StringUtils.isNotEmpty(argsMapFromCmd.get("starttls")))
            mailParams.starttls = argsMapFromCmd.get("starttls");

        mailParams.host = argsMapFromCmd.get("host");
        mailParams.port = Integer.parseInt(argsMapFromCmd.get("port"));
        mailParams.username = argsMapFromCmd.get("username");
        mailParams.password = argsMapFromCmd.get("password");

        if (StringUtils.isNotEmpty(argsMapFromCmd.get("html")))
            mailParams.html = Boolean.parseBoolean(argsMapFromCmd.get("html"));

        return mailParams;
    }

    private static Map<String, String> argsArrToMap(String[] args) {
        Map<String, String> argsMap = new HashMap<>();

        for (String arg : args) {
            if (!arg.contains("=")) {
                logger.error("Argument format must be arg=value Arg: " + arg + " is wrong format");
                System.exit(3);
            }
            String[] argKeyValue = arg.split("=",2);
            String value=argKeyValue[1].trim();
            if(value.startsWith("\"") && value.endsWith("\""))
                value= value.substring(1, value.length()-1);
            argsMap.put(argKeyValue[0],value );
        }
        return argsMap;
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
