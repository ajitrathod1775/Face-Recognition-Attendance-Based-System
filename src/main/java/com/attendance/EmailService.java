package com.attendance;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * EmailService - Automated Alert System
 * ✅ Supports TLS 1.2
 * ✅ Error Handling for Invalid Emails
 * ✅ Optimized for Gmail SMTP
 */
public class EmailService {

    // ⚠️ तुमचे स्वतःचे डिटेल्स इथे टाका
    // टीप: गुगलचा 'App Password' वापरणे अनिवार्य आहे.
    private static final String SENDER_EMAIL = "your-email@gmail.com";
    private static final String APP_PASSWORD = "your-app-password";

    public static void sendAbsenceEmail(String parentEmail, String studentName, String date) {

        // १. ईमेल आयडी चेक करा
        if (parentEmail == null || parentEmail.trim().isEmpty() || !parentEmail.contains("@")) {
            System.err.println("❌ Invalid Email Address: " + parentEmail + " for " + studentName);
            return;
        }

        // २. SMTP सर्व्हर सेटिंग्स (Gmail साठी)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "5000"); // ५ सेकंदात कनेक्शन नाही झाले तर थांबा
        props.put("mail.smtp.timeout", "5000");

        // ३. ऑथेंटिकेशन आणि सेशन तयार करा
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        // ४. ईमेल तयार करा आणि पाठवा
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(parentEmail));

            // विषय (Subject)
            message.setSubject("Attendance Alert: " + studentName + " is Absent");

            // मजकूर (Body)
            String content = "Dear Parent,\n\n" +
                    "This is an automated notification to inform you that your ward, " + studentName +
                    ", was marked ABSENT for the class on " + date + ".\n\n" +
                    "If you have any queries regarding this absence, please contact the college administration.\n\n" +
                    "Regards,\n" +
                    "Smart Attendance System Team";

            message.setText(content);

            // ५. प्रत्यक्षात ईमेल पाठवा (हा भाग नवीन थ्रेडमध्ये करणे कधीही चांगले)
            Transport.send(message);
            System.out.println("✅ Email notification successfully sent to: " + parentEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Messaging Error: " + e.getMessage());
            // ई-मेल सिस्टिम फेल झाली तरी मेन प्रोग्राम थांबू नये म्हणून फक्त प्रिंट करा
        }
    }
}