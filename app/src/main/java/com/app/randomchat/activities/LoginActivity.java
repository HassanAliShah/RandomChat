package com.app.randomchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.randomchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail;
    TextInputEditText etPassword;
    String strEtEmail = "";
    String strEtPassword = "";

    Activity loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginActivity = this;

        startActivity(new Intent(this, SplashActivity.class));

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

    }

    public void login(View view) {
        strEtEmail = Objects.requireNonNull(etEmail.getText()).toString();
        strEtPassword = Objects.requireNonNull(etPassword.getText()).toString();
        if (!isEverythingValid()) {
            Toast.makeText(this, "Invalid arguments", Toast.LENGTH_SHORT).show();
            return;
        }
        signIn();


    }

    private void signIn() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(strEtEmail, strEtPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
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