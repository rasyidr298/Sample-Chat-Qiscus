package id.rrdev.samplechatsdk.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.databinding.ActivityHomeBinding;
import id.rrdev.samplechatsdk.ui.adapter.ChatRoomAdapter;
import id.rrdev.samplechatsdk.ui.adapter.OnItemClickListener;
import id.rrdev.samplechatsdk.ui.chatRoom.ChatRoomActivity;
import id.rrdev.samplechatsdk.ui.contact.ContactActivity;
import id.rrdev.samplechatsdk.ui.login.LoginActivity;

public class HomeActivity extends AppCompatActivity implements HomeViewModel.View, View.OnClickListener, OnItemClickListener {
    private HomeViewModel homeViewModel;
    private ActivityHomeBinding binding;
    private ChatRoomAdapter chatRoomAdapter;
    private List<QiscusChatRoom> chatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setupView();
    }

    private void init(){
        homeViewModel = new HomeViewModel(this, this);

        //init recycler
        chatRoomAdapter = new ChatRoomAdapter(this);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(chatRoomAdapter);

        //init click
        chatRoomAdapter.setOnItemClickListener(this);
        binding.fabAdd.setOnClickListener(this);
        binding.llLogout.setOnClickListener(this);
    }

    private void setupView(){
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .dontAnimate())
                .load(QiscusCore.getQiscusAccount().getAvatar())
                .into(binding.ivAvatar);
    }

    private void observeRoomChat(){
        homeViewModel.getAllchat().observe(this, qiscusChatRooms -> {
            if (qiscusChatRooms != null) {
                this.chatRoom = qiscusChatRooms;
                chatRoomAdapter.addOrUpdate(qiscusChatRooms);
            }
        });
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        //observe chatRoom
        observeRoomChat();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        //observe chatRoom
        observeRoomChat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.llLogout :
                homeViewModel.logout();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case R.id.fabAdd :
                startActivity(new Intent(this, ContactActivity.class));
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
            startActivity(ChatRoomActivity.generateIntent(this, chatRoom.get(position)));
    }
}