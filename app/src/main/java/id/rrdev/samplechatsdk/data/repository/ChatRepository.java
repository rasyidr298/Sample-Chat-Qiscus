package id.rrdev.samplechatsdk.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.util.List;

import id.rrdev.samplechatsdk.util.Action;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChatRepository {
    private static final String TAG = ChatRepository.class.getSimpleName();

    public LiveData<List<QiscusChatRoom>> getChatRooms(Action<Throwable> onError) {
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
                    Log.d(TAG,"chat "+qiscusChatRooms);
                },throwable -> {
                    Log.d(TAG,"throw "+throwable.getMessage());
                });

        return data;
    }
}
