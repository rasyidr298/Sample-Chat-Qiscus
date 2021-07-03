package id.rrdev.samplechatsdk.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.util.List;

import id.rrdev.samplechatsdk.data.model.User;
import id.rrdev.samplechatsdk.ui.login.LoginViewModel;
import id.rrdev.samplechatsdk.util.Action;
import id.rrdev.samplechatsdk.util.AvatarUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserRepository {
    private static final String TAG = UserRepository.class.getSimpleName();
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public UserRepository(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("samplechat_user", Context.MODE_PRIVATE);
        gson = new  GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    public void loginUser(String email, String password, String name, Action<User> onSuccess, Action<Throwable> onError){
        QiscusCore.setUser(email, password)
                .withUsername(name)
                .withAvatarUrl(AvatarUtil.generateAvatar(name))
                .save()
                .map(this::mapFromQiscusAccount)
                .doOnNext(this::setCurrentUser)
                .subscribeOn(Schedulers.io())
                .subscribe(user -> {
                    Log.d(TAG, user.toString());
                    onSuccess.call(user);
                },throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    onError.call(throwable);
                });
    }

    public LiveData<List<User>> getUsers(long page, int limit, String searchUsername, Action<Throwable> onError) {
        final MutableLiveData<List<User>> data = new MutableLiveData<>();

        QiscusApi.getInstance().getUsers(searchUsername, page, limit)
                .flatMap(Observable::from)
                .filter(user -> !user.equals(getCurrentUser()))
                .filter(user -> !user.getUsername().equals(""))
                .map(this::mapFromQiscusAccount)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    Log.d(TAG,"users "+users);
                    Log.d(TAG,"users size "+users.size());
                    data.setValue(users);
                }, throwable -> {
                    Log.d(TAG,"throw "+throwable.getMessage());
                    onError.call(throwable);
                });

        return data;

    }

    private User getCurrentUser() {
        return gson.fromJson(sharedPreferences.getString("current_user", ""), User.class);
    }

    private void setCurrentUser(User user){
        sharedPreferences.edit()
                .putString("current_user",gson.toJson(user))
                .apply();
    }

    private User mapFromQiscusAccount(QiscusAccount qiscusAccount){
        User user = new User();
        user.setId(qiscusAccount.getEmail());
        user.setName(qiscusAccount.getUsername());
        user.setAvatarUrl(qiscusAccount.getAvatar());
        return user;
    }
}
