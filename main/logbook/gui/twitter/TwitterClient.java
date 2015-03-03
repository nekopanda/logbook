/**
 * 
 */
package logbook.gui.twitter;

import java.io.File;

import logbook.config.AppConfig;
import logbook.gui.WindowBase;

import org.apache.commons.lang3.StringUtils;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * @author Nekopanda
 *
 */
public class TwitterClient {

    /** twitter認証状況 */
    private AccessToken accessToken;
    private User user;
    private Twitter twitter;

    private static TwitterClient instance;

    public static TwitterClient getInstance() throws TwitterException {
        if (instance == null) {
            instance = new TwitterClient();
        }
        return instance;
    }

    TwitterClient() throws TwitterException {
        this.twitter = createInstance();
    }

    private static Twitter createInstance() {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(
                "d7OxSWHq0fAsDHc0V7WA7mBmw", "YflGWTXRTM9oi4mnYRd6ssq67DBzIRDZAjF2KASHHyy7KkmYEx");
        return twitter;
    }

    public boolean prepareAccessToken(WindowBase parent) {
        if (this.accessToken != null) {
            return true;
        }
        String token = AppConfig.get().getTwitterToken();
        String tokenSecret = AppConfig.get().getTwitterTokenSecret();
        if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(tokenSecret)) {
            try {
                // AccessTokenが有効かチェック
                AccessToken accessToken = new AccessToken(token, tokenSecret);
                this.twitter.setOAuthAccessToken(accessToken);
                this.user = this.twitter.verifyCredentials();
                return true;
            } catch (TwitterException e) {
                // 一度 setOAuthAccessToken したtwitterインスタンスは再利用できないので作り直す
                this.twitter = createInstance();
            }
        }
        OauthDialog oauthDialog = new OauthDialog(parent, this.twitter);
        oauthDialog.open();
        this.accessToken = oauthDialog.getAccessToken();
        this.user = oauthDialog.getUser();
        if (this.accessToken != null) {
            AppConfig.get().setTwitterToken(this.accessToken.getToken());
            AppConfig.get().setTwitterTokenSecret(this.accessToken.getTokenSecret());
            return true;
        }
        return false;
    }

    public void tweet(WindowBase parent, String text, File imageFile) throws TwitterException {
        try {
            this.twitter.updateStatus(new StatusUpdate(text).media(imageFile));
        } catch (TwitterException e) {
            if (e.getStatusCode() == 401) {
                // 認証エラー -> アクセストークンを更新してリトライする
                this.accessToken = null;
                this.twitter = createInstance();
                if (this.prepareAccessToken(parent)) {
                    this.twitter.updateStatus(new StatusUpdate(text).media(imageFile));
                    return;
                }
            }
            throw e;
        }
    }

    /**
     * @return screenName
     */
    public User getUser() {
        return this.user;
    }

}
