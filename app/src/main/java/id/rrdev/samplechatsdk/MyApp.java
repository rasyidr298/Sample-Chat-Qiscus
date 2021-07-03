package id.rrdev.samplechatsdk;

import android.app.Application;

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;

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

//        Jupuk.init(this);

    }
}
