package id.rrdev.samplechatsdk.ui.chatRoom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import java.util.List;

import id.rrdev.samplechatsdk.data.repository.ChatRepository;

public class ChatRoomViewModel extends ViewModel {
    private ChatRepository chatRepository;
    private View view;
    private QiscusChatRoom room;

    public ChatRoomViewModel(View view, QiscusChatRoom room){
        chatRepository = new ChatRepository();
        this.view = view;
        this.room = room;
    }

    public LiveData<QiscusComment> sendComment(String content) {
        QiscusComment qiscusComment = QiscusComment.generateMessage(room.getId(), content);
        return chatRepository.sendComment(
                qiscusComment,
                throwable -> {view.showErrorMessage(throwable.getMessage());
                });
    }

    public LiveData<List<QiscusComment>> getChatRoom(int count, QiscusChatRoom room){
        return chatRepository.loadComments(
                count,
                room,
                throwable -> {
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    public interface View {
        void showErrorMessage(String errorMessage);
    }
}
