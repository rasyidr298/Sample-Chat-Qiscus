package id.rrdev.samplechatsdk.ui.contact;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import id.rrdev.samplechatsdk.databinding.ActivityContactBinding;
import id.rrdev.samplechatsdk.ui.adapter.ContactAdapter;
import id.rrdev.samplechatsdk.ui.chatRoom.ChatRoomActivity;

public class ContactActivity extends AppCompatActivity implements ContactViewModel.View {
    private ActivityContactBinding binding;
    private ContactViewModel contactViewModel;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setupView();
    }

    private void init(){
        contactViewModel = new ContactViewModel(this, this);

        //init adapter
        contactAdapter = new ContactAdapter();
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupView(){

        //observe all user
        binding.recyclerview.setAdapter(contactAdapter);
        contactViewModel.getAllUser(1,100,"").observe(this, users -> {
            if (users != null){
                contactAdapter.submitList(users);
            }
        });

        contactAdapter.setOnItemClickListener((view, user, position) -> {
            contactViewModel.createRoom(user).observe(this,qiscusChatRoom -> {
                if (qiscusChatRoom != null){
                    startActivity(ChatRoomActivity.generateIntent(this, qiscusChatRoom));
                }
            });
        });
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}