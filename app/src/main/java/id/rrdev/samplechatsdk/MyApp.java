package id.rrdev.samplechatsdk;

import android.app.Application;

import com.qiscus.jupuk.Jupuk;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;

import id.rrdev.samplechatsdk.util.PushNotificationUtil;

public class MyApp extends Application {
    private static MyApp instance;

    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Nirmana.init(this);

        QiscusCore.setup(this, "sdksample");

        QiscusCore.getChatConfig()
                .enableDebugMode(true)
                .setNotificationListener(PushNotificationUtil::showNotification)
                .setEnableFcmPushNotification(true);

        Jupuk.init(this);

    }
}
