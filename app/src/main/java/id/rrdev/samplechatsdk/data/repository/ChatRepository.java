package id.rrdev.samplechatsdk.data.repository;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import org.json.JSONException;

import java.util.List;

import id.rrdev.samplechatsdk.data.model.User;
import id.rrdev.samplechatsdk.util.Action;
import retrofit2.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class ChatRepository{
    private static final String TAG = ChatRepository.class.getSimpleName();
    private final Func2<QiscusComment, QiscusComment, Integer> commentComparator = (lhs, rhs) -> rhs.getTime().compareTo(lhs.getTime());

    //get all chat
    public LiveData<List<QiscusChatRoom>> getAllChat(Action<Throwable> onError) {
        final MutableLiveData<List<QiscusChatRoom>> data = new MutableLiveData<>();

        Observable.from(QiscusCore.getDataStore().getChatRooms(100))
                .filter(chatRoom -> chatRoom.getLastComment() != null)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data::setValue, onError::call);

        QiscusApi.getInstance()
                .getAllChatRooms(true, false, true, 1, 100)
                .flatMap(Observable::from)
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom))
                .filter(chatRoom -> chatRoom.getLastComment().getId() != 0)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusChatRooms -> {
                    data.setValue(qiscusChatRooms);
                    Log.d(TAG,"chat all : "+qiscusChatRooms);
                    Log.d(TAG,"chat all size : "+qiscusChatRooms.size());
                },throwable -> {
                    Log.d(TAG,"throw all throw : "+throwable.getMessage());
                });

        return data;
    }

    //get chat room
    public LiveData<QiscusChatRoom> getChatRoom(User user, Action<Throwable> onError) {
        final MutableLiveData<QiscusChatRoom> data = new MutableLiveData<>();

        QiscusChatRoom savedChatRoom = QiscusCore.getDataStore().getChatRoom(user.getId());
        if (savedChatRoom != null) {
            data.setValue(savedChatRoom);
        }

        QiscusApi.getInstance()
                .chatUser(user.getId(), null)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusChatRoom -> {
                    Log.d(TAG,"chat room : "+qiscusChatRoom.toString());
                    data.setValue(qiscusChatRoom);
                }, throwable -> {
                    Log.d(TAG,"chat room throw : "+throwable.getMessage());
                    onError.call(throwable);
                });

        return data;
    }

    //create comment
    public LiveData<QiscusComment> sendComment(QiscusComment qiscusComment, Action<Throwable> onError) {
        final MutableLiveData<QiscusComment> data = new MutableLiveData<>();

        QiscusApi.getInstance().sendMessage(qiscusComment)
                .doOnSubscribe(() -> QiscusCore.getDataStore().addOrUpdate(qiscusComment))
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .compose(bindToLifecycle())
                .subscribe(qiscusComment1 -> {
                    Log.d(TAG,"create comment : "+qiscusComment1.toString());
                    data.setValue(qiscusComment1);
                }, throwable -> {
                    Log.d(TAG,"create comment throw : "+throwable.getMessage());
                    onError.call(throwable);
                });
        return data;
    }

    //get chat user
    public LiveData<List<QiscusComment>> loadComments(int count, QiscusChatRoom room, Action<Throwable> onError) {
        final MutableLiveData<List<QiscusComment>> data = new MutableLiveData<>();

        Observable.merge(getInitRoomData(room), getLocalComments(count, room)
                .map(comments -> Pair.create(room, comments)))
                .filter(qiscusChatRoomListPair -> qiscusChatRoomListPair != null)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
//                .compose(bindToLifecycle())
                .subscribe(roomData -> {
                    Log.d(TAG,"chat user first : "+roomData.first.toString());
                    Log.d(TAG,"chat user second : "+roomData.second.toString());
                    Log.d(TAG,"chat user second size : "+roomData.second.size());
                    data.setValue(roomData.second);
                }, throwable -> {
                    onError.call(throwable);
                    Log.d(TAG,"chat user throw : "+throwable.getMessage());
                });

        return data;
    }

    private void commentSuccess(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
        QiscusComment savedQiscusComment = QiscusCore.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
            qiscusComment.setState(savedQiscusComment.getState());
        }
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
    }

    private void commentFail(Throwable throwable, QiscusComment qiscusComment) {
        if (!QiscusCore.getDataStore().isContains(qiscusComment)) {
            return;
        }

        int state = QiscusComment.STATE_PENDING;
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.setDownloading(false);
            state = QiscusComment.STATE_FAILED;
        }

        QiscusComment savedQiscusComment = QiscusCore.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > QiscusComment.STATE_SENDING) {
            return;
        }

        qiscusComment.setState(state);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
    }

    private boolean mustFailed(Throwable throwable, QiscusComment qiscusComment) {
        return ((throwable instanceof HttpException && ((HttpException) throwable).code() >= 400) ||
                (throwable instanceof JSONException) ||
                qiscusComment.isAttachment());
    }

    private Observable<Pair<QiscusChatRoom, List<QiscusComment>>> getInitRoomData(QiscusChatRoom room) {
        return QiscusApi.getInstance().getChatRoomWithMessages(room.getId())
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> null);
    }

    private Observable<List<QiscusComment>> getLocalComments(int count, QiscusChatRoom room) {
        return QiscusCore.getDataStore().getObservableComments(room.getId(), 2 * count)
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() > count) {
                        return comments.subList(0, count);
                    }
                    return comments;
                })
                .subscribeOn(Schedulers.io());
    }

}
