package id.rrdev.samplechatsdk.ui.chatRoom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent;
import com.qiscus.sdk.chat.core.util.QiscusDateUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.databinding.ActivityRoomChatBinding;
import id.rrdev.samplechatsdk.ui.adapter.CommentsAdapter;

public class ChatRoomActivity extends AppCompatActivity implements ChatRoomViewModel.View, View.OnClickListener {
    private static final String TAG = ChatRoomActivity.class.getSimpleName();
    private static final String CHAT_ROOM_KEY = "extra_chat_room";
    private ActivityRoomChatBinding binding;
    private ChatRoomViewModel chatRoomViewModel;
    private QiscusChatRoom chatRoom;
    private CommentsAdapter commentsAdapter;

    public static Intent generateIntent(Context context, QiscusChatRoom chatRoom) {
        Intent intent = new Intent(context, ChatRoomActivity.class);
        intent.putExtra(CHAT_ROOM_KEY, chatRoom);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setupView();
    }

    private void init(){
        chatRoom = getIntent().getParcelableExtra(CHAT_ROOM_KEY);
        chatRoomViewModel = new ChatRoomViewModel(this, chatRoom);

        binding.ivBack.setOnClickListener(this);
        binding.btnSend.setOnClickListener(this);

        commentsAdapter = new CommentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(commentsAdapter);
    }

    private void setupView(){
        if (chatRoom != null) {
            Log.d(TAG,"chat room user "+chatRoom.toString());
        }else{
            finish();
            return;
        }

        Glide.with(this).load(chatRoom.getAvatarUrl()).into(binding.ivAvatar);
        binding.tvNama.setText(chatRoom.getName());
    }

    private void notifyLatestRead() {
        QiscusComment comment = commentsAdapter.getLatestSentComment();
        if (comment != null) {
            QiscusPusherApi.getInstance()
                    .markAsRead(chatRoom.getId(), comment.getId());
        }
    }

    private void checkTyping(){
        binding.etFieldMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                notifyServerTyping(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void notifyServerTyping(boolean typing) {
        QiscusPusherApi.getInstance().publishTyping(chatRoom.getId(), typing);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        //get comment
        chatRoomViewModel.getChatRoom(100, chatRoom).observe(this, qiscusComments -> {
            if (qiscusComments != null){
                commentsAdapter.addOrUpdate(qiscusComments);
                notifyLatestRead();
            }
        });
        QiscusCacheManager.getInstance().setLastChatActivity(true, chatRoom.getId());
        checkTyping();
        Runnable stopTypingNotifyTask = () -> {
            notifyServerTyping(false);
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        QiscusCacheManager.getInstance().setLastChatActivity(false, chatRoom.getId());
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifyLatestRead();
        chatRoomViewModel.detachView();
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailedSendComment(QiscusComment qiscusComment) {
        commentsAdapter.addOrUpdate(qiscusComment);
    }

    @Subscribe
    public void onUserStatusChanged(QiscusUserStatusEvent event) {
        String last = QiscusDateUtil.getRelativeTimeDiff(event.getLastActive());
        Log.w("test","onUserStatusChanged: "+ event.isOnline());
        binding.subtitle.setText(event.isOnline() ? "Online" : "Last seen " + last);
    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        binding.subtitle.setText(typing ? "Typing..." : "Online");
    }

    @Override
    public void updateLastDeliveredComment(long lastDeliveredCommentId) {
        commentsAdapter.updateLastDeliveredComment(lastDeliveredCommentId);
    }

    @Override
    public void updateLastReadComment(long lastReadCommentId) {
        commentsAdapter.updateLastReadComment(lastReadCommentId);
    }

    @Override
    public void onNewComment(QiscusComment comment) {
        commentsAdapter.addOrUpdate(comment);
        if (((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition() <= 2) {
            binding.recyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnSend :
                //create comment
                binding.recyclerView.setAdapter(commentsAdapter);
                if (!TextUtils.isEmpty(binding.etFieldMessage.getText())){
                    chatRoomViewModel.sendComment(binding.etFieldMessage.getText().toString()).observe(this, qiscusComment -> {
                        if (qiscusComment != null){
                            commentsAdapter.addOrUpdate(qiscusComment);
                        }
                    });
                    binding.etFieldMessage.getText().clear();
                }
                break;

            case R.id.ivBack :
                onBackPressed();
                break;
        }
    }
}