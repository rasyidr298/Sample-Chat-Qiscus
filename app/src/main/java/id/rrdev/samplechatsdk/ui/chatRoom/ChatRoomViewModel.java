package id.rrdev.samplechatsdk.ui.chatRoom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.chat.core.presenter.QiscusChatRoomEventHandler;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import id.rrdev.samplechatsdk.data.repository.ChatRepository;

public class ChatRoomViewModel extends ViewModel implements QiscusChatRoomEventHandler.StateListener {
    private ChatRepository chatRepository;
    private View view;
    private QiscusChatRoom room;
    private QiscusChatRoomEventHandler roomEventHandler;
    private QiscusAccount qiscusAccount;

    public ChatRoomViewModel(View view, QiscusChatRoom room){
        chatRepository = new ChatRepository();
        this.view = view;

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.room = room;
        if (this.room.getMember().isEmpty()) {
            this.room = QiscusCore.getDataStore().getChatRoom(room.getId());
        }
        qiscusAccount = QiscusCore.getQiscusAccount();
        roomEventHandler = new QiscusChatRoomEventHandler(this.room, this);
    }

    public LiveData<QiscusComment> sendComment(String content) {
        QiscusComment qiscusComment = QiscusComment.generateMessage(room.getId(), content);
        return chatRepository.sendComment(
                qiscusComment,
                throwable -> {
                    view.onFailedSendComment(qiscusComment);
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    public LiveData<List<QiscusComment>> getChatRoom(int count, QiscusChatRoom room){
        return chatRepository.loadComments(
                count,
                room,
                roomFromComment -> {
                    this.room = roomFromComment;
                    roomEventHandler.setChatRoom(roomFromComment);
                },
                throwable -> {
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        if (event.getQiscusComment().getRoomId() == room.getId()) {
            onGotNewComment(event.getQiscusComment());
        }
    }

    private void onGotNewComment(QiscusComment qiscusComment) {
        if (qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail())) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> chatRepository.commentSuccess(qiscusComment));
        } else {
            roomEventHandler.onGotComment(qiscusComment);
        }

        if (qiscusComment.getRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> {
                if (!qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail())
                        && QiscusCacheManager.getInstance().getLastChatActivity().first) {
                    QiscusPusherApi.getInstance().markAsRead(room.getId(), qiscusComment.getId());
                }
            });
            view.onNewComment(qiscusComment);
        }
    }

    public void detachView() {
        roomEventHandler.detach();
        clearUnreadCount();
        room = null;
        view = null;
        EventBus.getDefault().unregister(this);
    }

    private void clearUnreadCount() {
        room.setUnreadCount(0);
        room.setLastComment(null);
        QiscusCore.getDataStore().addOrUpdate(room);
    }

    @Override
    public void onChatRoomNameChanged(String name) {
    }

    @Override
    public void onChatRoomMemberAdded(QiscusRoomMember qiscusRoomMember) {

    }

    @Override
    public void onChatRoomMemberRemoved(QiscusRoomMember qiscusRoomMember) {

    }

    @Override
    public void onUserTypng(String email, boolean typing) {
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.onUserTyping(email, typing);
            }
        });
    }

    @Override
    public void onChangeLastDelivered(long lastDeliveredCommentId) {
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.updateLastDeliveredComment(lastDeliveredCommentId);
            }
        });
    }

    @Override
    public void onChangeLastRead(long lastReadCommentId) {
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.updateLastReadComment(lastReadCommentId);
            }
        });
    }

    public interface View {
        void showErrorMessage(String errorMessage);
        void onFailedSendComment(QiscusComment qiscusComment);
        void onUserTyping(String user, boolean typing);
        void updateLastDeliveredComment(long lastDeliveredCommentId);
        void updateLastReadComment(long lastReadCommentId);
        void onNewComment(QiscusComment qiscusComment);
    }
}
