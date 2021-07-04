package id.rrdev.samplechatsdk.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.databinding.ActivityHomeBinding;
import id.rrdev.samplechatsdk.ui.adapter.HomeAdapter;
import id.rrdev.samplechatsdk.ui.chatRoom.ChatRoomActivity;
import id.rrdev.samplechatsdk.ui.contact.ContactActivity;

public class HomeActivity extends AppCompatActivity implements HomeViewModel.View {
    private HomeViewModel homeViewModel;
    private ActivityHomeBinding binding;
    private HomeAdapter homeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setupView();
    }

    private void init(){
        homeViewModel = new HomeViewModel(this);

        //init recycler
        homeAdapter = new HomeAdapter();
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupView(){
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .dontAnimate())
                .load(QiscusCore.getQiscusAccount().getAvatar())
                .into(binding.ivAvatar);

        //observe chatRoom
        binding.recyclerview.setAdapter(homeAdapter);
        homeViewModel.getAllchat().observe(this, qiscusChatRooms -> {
            if (qiscusChatRooms != null) {
                homeAdapter.submitList(qiscusChatRooms);
            }
        });

        //onClick
        homeAdapter.setOnItemClickListener((view, qiscusChatRoom, position) -> {
            startActivity(ChatRoomActivity.generateIntent(this, qiscusChatRoom));
        });

        //longClick
        homeAdapter.setOnItemLongClickListener((view, qiscusChatRoom, position) -> {
            Toast.makeText(this, qiscusChatRoom.getName(), Toast.LENGTH_SHORT).show();
            return true;
        });

        //addChat
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, ContactActivity.class));
        });
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}