package id.rrdev.samplechatsdk.ui.contact;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

import id.rrdev.samplechatsdk.data.model.User;
import id.rrdev.samplechatsdk.data.repository.ChatRepository;
import id.rrdev.samplechatsdk.data.repository.UserRepository;

public class ContactViewModel extends ViewModel {
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private View view;
    private LiveData<List<User>> getAllUser;

    public ContactViewModel(View view, Context context){
        userRepository = new UserRepository(context);
        chatRepository = new ChatRepository();
        this.view = view;
    }

    public LiveData<List<User>> getAllUser(long page, int limit, String query){
        return this.getAllUser = userRepository.getUsers(page, limit, query,
                throwable -> {view.showErrorMessage(throwable.getMessage()); });
    }

    public LiveData<QiscusChatRoom> createRoom(User contact){
        return chatRepository.getChatRoom(contact,
                throwable -> {view.showErrorMessage(throwable.getMessage());
        });
    }

    public interface View {
        void showErrorMessage(String errorMessage);
    }
}
