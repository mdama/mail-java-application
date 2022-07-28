# JAVA Based Mail Client Application

Designed for sending emails via standalone jar application. All mail parameters can be passed to built jar. 

Example usage:

```java mail-java.jar -jar to-emails=email1@hotmail.com,email2@gmail.com subject="Sample Subject" content="Sample Content" host="mail.server.com" port=587 from=sender@example.com username="sender@example.com" password="123456" attachments="D:\Absolute\Path\test.pdf" starttls=false http=true```

There are points may be improved . But this application was developed my personnel need by allocating limited amount of time. If anyone needs additional feature, can request or can create Pull Request.

## Parameter File Alternative-1

Sample Parameter File Content;
```
host="mail.server.com"
port=587
from=sender@example.com
username="sender@example.com"
password="123456"
attachments="D:\Absolute\Path\test.pdf"
starttls=false
http=true
```

Calling file parameters with additional command line parameters. Command line parameters overrides file parameters.
```bash
java -jar mail-java.jar param-file=/home/xyz/mail-java.params to-emails=email1@hotmail.com,email2@gmail.com subject="Sample Subject" content="Sample Content" 
```

## Parameter File Alternative-2
If you want to read parameters from file on linux, you can use

```bash
cat mail-java.params | xargs java -jar mail-java.jar
```


