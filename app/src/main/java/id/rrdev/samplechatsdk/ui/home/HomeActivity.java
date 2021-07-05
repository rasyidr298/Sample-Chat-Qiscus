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

import java.util.List;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.databinding.ActivityHomeBinding;
import id.rrdev.samplechatsdk.ui.adapter.ChatRoomAdapter;
import id.rrdev.samplechatsdk.ui.chatRoom.ChatRoomActivity;
import id.rrdev.samplechatsdk.ui.contact.ContactActivity;
import id.rrdev.samplechatsdk.ui.login.LoginActivity;

public class HomeActivity extends AppCompatActivity implements HomeViewModel.View, View.OnClickListener {
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
        chatRoomAdapter = new ChatRoomAdapter();
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        //init click
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


        //onClick adapter
        chatRoomAdapter.setOnItemClickListener((view, qiscusChatRoom, position) -> {
            startActivity(ChatRoomActivity.generateIntent(this, qiscusChatRoom));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //observe chatRoom
        binding.recyclerview.setAdapter(chatRoomAdapter);
        homeViewModel.getAllchat().observe(this, qiscusChatRooms -> {
            if (qiscusChatRooms != null) {
                this.chatRoom = qiscusChatRooms;
                chatRoomAdapter.submitList(qiscusChatRooms);
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
}