package id.rrdev.samplechatsdk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

import id.rrdev.samplechatsdk.data.repository.ChatRepository;

public class HomeViewModel extends ViewModel {
    private final ChatRepository chatRepository;
    private final View view;
    private LiveData<List<QiscusChatRoom>> getAllChat;

    public HomeViewModel(View view) {
        chatRepository = new ChatRepository();
        this.view = view;
    }

    public LiveData<List<QiscusChatRoom>> getAllchat(){
        return this.getAllChat = chatRepository.getChatRooms(
                throwable -> {view.showErrorMessage(throwable.getMessage()); });
    }

    public interface View {
        void showErrorMessage(String errorMessage);
    }
}
