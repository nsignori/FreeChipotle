package tests.com.nsignori.FreeChipotle;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static final String CONSUMER_KEY = System.getenv("CONSUMER_KEY");
    static final String CONSUMER_SECRET = System.getenv("CONSUMER_SECRET");
    static final String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
    static final String ACCESS_TOKEN_SECRET = System.getenv("ACCESS_TOKEN_SECRET");

    public static void main(String[] args) {
        Twitter twitter = getTwitterInstance();
        try {
            boolean found = false;
            for(int i = 1; i < 10 && !found; i++) {
                List<Status> statuses = twitter.getUserTimeline("ChipotleTweets", new Paging(i, 80));

                for (Status status : statuses) {
                    String fmt = status.getText();
                    Pattern pattern = Pattern.compile("\\b\\w*(FREE)\\w*\\b");
                    Matcher matcher = pattern.matcher(fmt);
                    if (matcher.find()) {
                        System.out.println(matcher.group(0));
                        found = true;
                        break;
                    }
//                System.out.println(fmt);
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public static Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    private static void showHomeTimeline(Twitter twitter) {

        List<Status> statuses = null;
        try {
            statuses = twitter.getHomeTimeline();

            System.out.println("Showing home timeline.");

            for (Status status : statuses) {
                System.out.println(status.getUser().getName() + ":" + status.getText());
                String url= "https://twitter.com/" + status.getUser().getScreenName() + "/status/"
                        + status.getId();
                System.out.println("Above tweet URL : " + url);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}
