package com.the_pathfinders.verification;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "the.pathfinders.dev@gmail.com";
    private static final String APP_PASSWORD = "uaqs kqnu lrrz calf";
    private static final String APP_NAME = "shelter_of_mind";
    private static final boolean EMAIL_ENABLED = true; // Set to false to disable email sending (testing mode)

    public static void sendVerificationEmail(String toEmail, String soulId, String verificationToken) {
        if (!EMAIL_ENABLED) {
            System.out.println("[EMAIL DISABLED] Would send verification to: " + toEmail);
            System.out.println("[EMAIL DISABLED] Verification link: http://localhost:8080/verify?token=" + verificationToken + "&soul_id=" + soulId);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("shelter_of_mind email Verification");

            String verificationLink = "http://localhost:8080/verify?token=" + verificationToken + "&soul_id=" + soulId;
            
            String emailBody = String.format(
                "Hello there, our good soul \"%s\"!\n\n" +
                "Use the following link to verify your email:\n\n" +
                "%s\n\n" +
                "Have a good day!\n\n" +
                "Regards,\n" +
                "The keepers of your soul\n" +
                "shelter_of_mind",
                soulId, verificationLink
            );

            message.setText(emailBody);

            System.out.println("Sending verification email to: " + toEmail);
            Transport.send(message);
            System.out.println("Verification email sent successfully!");

        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public static void sendKeeperVerificationEmail(String toEmail, String keeperId, String verificationToken) {
        if (!EMAIL_ENABLED) {
            System.out.println("[EMAIL DISABLED] Would send keeper verification to: " + toEmail);
            System.out.println("[EMAIL DISABLED] Verification link: http://localhost:8080/verify-keeper?token=" + verificationToken + "&keeper_id=" + keeperId);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("shelter_of_mind - Keeper Account Email Verification");

            String verificationLink = "http://localhost:8080/verify-keeper?token=" + verificationToken + "&keeper_id=" + keeperId;
            
            String emailBody = String.format(
                "Hello there, aspiring keeper \"%s\"!\n\n" +
                "Thank you for your interest in becoming a keeper at shelter_of_mind.\n\n" +
                "Please verify your email address by clicking the link below:\n\n" +
                "%s\n\n" +
                "After email verification, existing keepers will review your request and " +
                "you will receive a follow-up email once your account is approved.\n\n" +
                "Have a good day!\n\n" +
                "Regards,\n" +
                "The keepers of souls\n" +
                "shelter_of_mind",
                keeperId, verificationLink
            );

            message.setText(emailBody);

            System.out.println("Sending keeper verification email to: " + toEmail);
            Transport.send(message);
            System.out.println("Keeper verification email sent successfully!");

        } catch (Exception e) {
            System.err.println("Failed to send keeper verification email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send keeper verification email", e);
        }
    }
    
    public static void sendKeeperApprovalNotification(String toEmail, String keeperId) {
        if (!EMAIL_ENABLED) {
            System.out.println("[EMAIL DISABLED] Would send approval notification to: " + toEmail);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("shelter_of_mind - Keeper Account Approved! 🎉");

            String emailBody = String.format(
                "Congratulations, keeper \"%s\"!\n\n" +
                "Your keeper account has been approved by our existing keepers.\n\n" +
                "You can now log in to shelter_of_mind using your keeper credentials and " +
                "start helping souls on their journey to mental wellness.\n\n" +
                "Welcome to the team!\n\n" +
                "Regards,\n" +
                "The keepers of souls\n" +
                "shelter_of_mind",
                keeperId
            );

            message.setText(emailBody);

            System.out.println("Sending keeper approval notification to: " + toEmail);
            Transport.send(message);
            System.out.println("Keeper approval notification sent successfully!");

        } catch (Exception e) {
            System.err.println("Failed to send keeper approval notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void sendKeeperRejectionNotification(String toEmail, String keeperId) {
        if (!EMAIL_ENABLED) {
            System.out.println("[EMAIL DISABLED] Would send rejection notification to: " + toEmail);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("shelter_of_mind - Keeper Application Update");

            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Thank you for your interest in becoming a keeper at shelter_of_mind.\n\n" +
                "After careful review, we regret to inform you that your keeper account request " +
                "has not been approved at this time.\n\n" +
                "However, you may reapply after 48 hours if you wish to try again. " +
                "Please ensure you meet all the requirements when reapplying.\n\n" +
                "If you have any questions or concerns, please don't hesitate to contact us.\n\n" +
                "Thank you for your understanding.\n\n" +
                "Regards,\n" +
                "The keepers of souls\n" +
                "shelter_of_mind",
                keeperId
            );

            message.setText(emailBody);

            System.out.println("Sending keeper rejection notification to: " + toEmail);
            Transport.send(message);
            System.out.println("Keeper rejection notification sent successfully!");

        } catch (Exception e) {
            System.err.println("Failed to send keeper rejection notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void notifyExistingKeepersOfNewSignup(String newKeeperEmail, String newKeeperId) {
        // For now, just log it
        System.out.println("New keeper signup pending approval: " + newKeeperId + " (" + newKeeperEmail + ")");
        System.out.println("Existing keepers should be notified through the admin dashboard.");
    }

    public static String generateVerificationToken(String soulId) {
        // Generate a secure random token
        return java.util.UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    
    public static void sendPasswordResetEmail(String toEmail, String keeperId, String resetToken) {
        if (!EMAIL_ENABLED) {
            System.out.println("[EMAIL DISABLED] Would send password reset to: " + toEmail);
            System.out.println("[EMAIL DISABLED] Reset link: http://localhost:8081/reset-password?token=" + resetToken);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("shelter_of_mind - Password Reset Request");

            String resetLink = "http://localhost:8081/reset-password?token=" + resetToken;
            
            String emailBody = String.format(
                "Hello keeper \"%s\",\n\n" +
                "We received a request to reset your password for your shelter_of_mind keeper account.\n\n" +
                "Click the link below to reset your password:\n\n" +
                "%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request a password reset, please ignore this email. " +
                "Your password will remain unchanged.\n\n" +
                "Regards,\n" +
                "The keepers of souls\n" +
                "shelter_of_mind",
                keeperId, resetLink
            );

            message.setText(emailBody);

            System.out.println("Sending password reset email to: " + toEmail);
            Transport.send(message);
            System.out.println("Password reset email sent successfully!");

        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
