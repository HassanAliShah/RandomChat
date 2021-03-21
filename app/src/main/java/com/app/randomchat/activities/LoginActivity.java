package com.app.randomchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.randomchat.Info.Info;
import com.app.randomchat.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements Info {

    TextInputEditText etEmail;
    TextInputEditText etPassword;

    String strEtEmail = "";
    String strEtPassword = "";

    ProgressBar progressBar;

    Activity loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        FirebaseAuth.getInstance().signOut();

        progressBar = findViewById(R.id.pb_log_in);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        loginActivity = this;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, ChatListActivity.class));
            finish();
        }

    }

    public void login(View view) {
        strEtEmail = Objects.requireNonNull(etEmail.getText()).toString();
        strEtPassword = Objects.requireNonNull(etPassword.getText()).toString();
        if (!isEverythingValid()) {
            Toast.makeText(this, "Invalid arguments", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        signIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, ChatListActivity.class));
            finish();
        }
    }

    private void signIn() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(strEtEmail, strEtPassword).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isEverythingValid() {
        if (strEtEmail.equals("")) {
            etEmail.setError("Field empty");
            return false;
        }
        if (!strEtEmail.contains("@")) {
            etEmail.setError("Incorrect format");
            return false;
        }

        if (strEtPassword.equals("")) {
            etPassword.setError("Field empty");
            return false;
        }
        return true;
    }

    public void createAccount(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }
}