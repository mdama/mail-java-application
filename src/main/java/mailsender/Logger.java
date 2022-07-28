package mailsender;

public class Logger {
    public void info(String msg) {
        System.out.println(msg);
    }

    public void warn(String msg) {
        System.out.println(msg);
    }

    public void error(String msg) {
        this.error(msg,null);
    }

    public void error(String msg, Exception e) {
        System.err.println(msg);
        if(e!=null)
            e.printStackTrace();
    }
}
