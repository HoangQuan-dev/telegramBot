package com.example.registerbot.Service;


import com.example.registerbot.Controller.UserController;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.util.*;

@Service
public class EmailVerificationService {
    private final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    public Timestamp expTimeInSec = calculateExpiryDate(100);
    public void sendHtmlEmail(String toEmail) throws MessagingException {
        System.out.println("Email is sending...");

        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setPort(587);
        javaMailSender.setUsername("psoftverify@gmail.com");
        javaMailSender.setPassword("nvzygqlbvibmgfvr");

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        javaMailSender.setJavaMailProperties(properties);

        String token = UUID.randomUUID().toString();
        String verificationLink = "http://localhost:8080/activation?email=" + toEmail + "&token=" + token;;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject("EMAIL VERIFICATION");
        helper.setText("Thank you for your registering. Please <a href=\"" + verificationLink + "\">click here</a> to verify your email \n" + "Hope you enjoy your time with us.", true);

        javaMailSender.send(message);
    }


    public void setExpTime(boolean isSetExpTime) {
        if (isSetExpTime)
        {
            expTimeInSec = calculateExpiryDate(100);
        }
        else
        {
            System.out.println("Error set expiry time!!!!!!!!!!!!!!!!");
        }
    }

    public Timestamp calculateExpiryDate(int expiryTimeInSecs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, expiryTimeInSecs);
        return new Timestamp(cal.getTime().getTime());
    }

}
