package id.rrdev.samplechatsdk.ui.login;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import id.rrdev.samplechatsdk.data.repository.UserRepository;

public class LoginViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final View view;

    public LoginViewModel(View view, Context context) {
        userRepository = new UserRepository(context);
        this.view = view;
    }

    public void login(String email, String password, String name){
        view.onStarted();
        userRepository.loginUser(email, password, name,
                user -> {
                    view.onSucces();
                },
                throwable -> {
                    view.onFailure(throwable.getMessage());
                });
    }

    public interface View{
        void onStarted();
        void onSucces();
        void onFailure(String message);
    }
}
