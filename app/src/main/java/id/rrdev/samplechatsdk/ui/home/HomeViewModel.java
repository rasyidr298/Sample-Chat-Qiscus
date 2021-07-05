package id.rrdev.samplechatsdk.ui.home;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

import id.rrdev.samplechatsdk.data.repository.ChatRepository;
import id.rrdev.samplechatsdk.data.repository.UserRepository;

public class HomeViewModel extends ViewModel {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final View view;

    public HomeViewModel(View view, Context context) {
        chatRepository = new ChatRepository();
        userRepository = new UserRepository(context);
        this.view = view;
    }

    public LiveData<List<QiscusChatRoom>> getAllchat(){
        return chatRepository.getAllChat(
                throwable -> {view.showErrorMessage(throwable.getMessage()); });
    }

    public void logout() {
        userRepository.logout();
    }

    public interface View {
        void showErrorMessage(String errorMessage);
    }
}
