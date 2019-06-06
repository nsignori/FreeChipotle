package tests.com.nsignori.FreeChipotle;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String CONSUMER_KEY = System.getenv("CONSUMER_KEY");
    private static final String CONSUMER_SECRET = System.getenv("CONSUMER_SECRET");
    private static final String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
    private static final String ACCESS_TOKEN_SECRET = System.getenv("ACCESS_TOKEN_SECRET");
    private static ArrayList<String> emails = new ArrayList<String>();


    public static void main(String[] args) {
        Scanner scan;
        try {
            scan = new Scanner(new File("src/main/resources/emails.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        while (scan.hasNextLine()) {
            String email = scan.nextLine();
            if(!email.contains("//")) {
                emails.add(email);
                System.out.println(email);
            }
        }


        Twitter twitter = getTwitterInstance();

        String previousCode = "";
        while(true) {
            String code = getLatestFreeCode(twitter);
            if(!code.equals(previousCode)) {
                sendEmail(emails, code);
                previousCode = code;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    private static String getLatestFreeCode(Twitter twitter) {
        try {
            for(int i = 1; i < 20; i++) {
                List<Status> statuses = twitter.getUserTimeline("ChipotleTweets", new Paging(i, 20));

                for (Status status : statuses) {
                    String fmt = status.getText();
                    Pattern pattern = Pattern.compile("\\b\\w*(FREE)\\w*\\b");
                    Matcher matcher = pattern.matcher(fmt);
                    if (matcher.find()) {
                        System.out.println("Page: " + i + " Code: " + matcher.group(0));
                        return matcher.group(0);
                    }
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static void sendEmail(ArrayList<String> emails, String code) {
        for(String email : emails) {
            //Get the session object
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.host", System.getenv("EMAIL_HOST"));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
            props.put("mail.debug", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(System.getenv("FROM_EMAIL"), System.getenv("FROM_EMAIL_PASSWORD"));
                        }
                    });

            //Compose the message
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(System.getenv("FROM_EMAIL")));
                message.addRecipient(Message.RecipientType.TO,new InternetAddress(email));
                message.setText(code);

                //send the message
                Transport.send(message);

                System.out.println("message sent successfully to: " + email);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}
