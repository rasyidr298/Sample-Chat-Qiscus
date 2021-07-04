package id.rrdev.samplechatsdk.ui.chatRoom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

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

        //get comment
        binding.recyclerView.setAdapter(commentsAdapter);
        chatRoomViewModel.getChatRoom(100, chatRoom).observe(this, qiscusComments -> {
            if (qiscusComments != null){
                commentsAdapter.addOrUpdate(qiscusComments);
            }
        });
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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
                            binding.etFieldMessage.getText().clear();
                        }
                    });
                }
                break;

            case R.id.ivBack :
                onBackPressed();
                break;
        }
    }
}