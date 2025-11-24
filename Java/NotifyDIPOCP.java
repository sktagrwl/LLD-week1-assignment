class SmtpMailer implements Notification{

    @Override
    public void sendNotification(String templ, String to, String body) {
        System.out.println("[SMTP] template=" + templ + " to=" + to + " body=" + body);
    }
}

class TwilioClient implements OtpService{
    @Override
    public void sendOTP(String phone, String code) {
        System.out.println("[Twilio] OTP " + code + " -> " + phone);
    }
}

interface OtpService{
    void sendOTP(String phone, String code);
}

class User {
    String email;
    String phone;
    User(String email, String phone) {
        this.email = email; this.phone = phone;
    }
}

interface Notification{
    void sendNotification(String templ, String to, String body);
}

interface iSignUpService {
    boolean signUp(User u);
}

class SignUpService implements iSignUpService{

    private final Notification notification;
    private final OtpService otpService;

    SignUpService(Notification notification, OtpService otpService) {
        this.notification = notification;
        this.otpService = otpService;
    }

    @Override
    public boolean signUp(User u){
        if (u.email == null || u.email.isEmpty()) return false;
        // pretend DB save here…
        notification.sendNotification("welcome", u.email, "Welcome!");
        otpService.sendOTP(u.phone, "123456");
        return true;
    }
}

public class NotifyDIPOCP {

    private final iSignUpService signUpService;
    public NotifyDIPOCP(iSignUpService signUpService) {
        this.signUpService = signUpService;
    }
    public static void main(String[] args) {
        Notification smtp = new SmtpMailer();
        OtpService twilio = new TwilioClient();

        iSignUpService signUp = new SignUpService(smtp, twilio);

        signUp.signUp(new User("user@example.com", "+15550001111"));
    }
}
