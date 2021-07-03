package id.rrdev.samplechatsdk.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import id.rrdev.samplechatsdk.databinding.ActivityLoginBinding;
import id.rrdev.samplechatsdk.ui.home.HomeActivity;

public class LoginActivity extends AppCompatActivity implements LoginViewModel.View {
    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setupView();
    }

    private void init(){
        loginViewModel =  new LoginViewModel(this,this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait!");
    }

    private void setupView(){

        binding.etUserId.setText("qwerty");
        binding.etPassword.setText("qwerty");
        binding.etDisplayName.setText("rass");

        binding.btnLogin.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.etUserId.getText().toString())) {
                binding.tInputNama.setError("Must not empty!");
            } else if (TextUtils.isEmpty(binding.etPassword.getText().toString())) {
                binding.tInputKapasitas.setError("Must not empty!");
            } else if (TextUtils.isEmpty(binding.etDisplayName.getText().toString())) {
                binding.tInputFasilitas.setError("Must not empty!");
            } else {
                loginViewModel.login(
                        binding.etUserId.getText().toString(),
                        binding.etPassword.getText().toString(),
                        binding.etDisplayName.getText().toString()
                );
            }

        });
    }

    @Override
    public void onStarted() {
        progressDialog.show();
    }

    @Override
    public void onSucces() {
        progressDialog.dismiss();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public void onFailure(String message) {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}