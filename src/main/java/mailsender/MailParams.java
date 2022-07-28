package mailsender;


import java.util.List;

public class MailParams {
    List<String> toEmails;
    String from;
    String subject;
    boolean html = true;
    String content;
    List<String> attachmentFullPaths;

    String auth="true";
    String starttls="true";
    String host;
    int port= 587;
    String username;
    String password;

}
