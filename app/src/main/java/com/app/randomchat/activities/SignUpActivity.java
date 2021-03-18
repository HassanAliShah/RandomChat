package com.app.randomchat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements Info {

    TextInputEditText etFirstName;
    TextInputEditText etLastName;
    TextInputEditText etEmail;
    TextInputEditText etPassword;

    String strEtFirstName = "";
    String strEtLastName = "";
    String strEtEmail = "";
    String strEtPassword = "";

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        progressBar = findViewById(R.id.pb_sign_up);

    }

    public void signUp(View view) {

        progressBar.setVisibility(View.VISIBLE);

        strEtFirstName = Objects.requireNonNull(etFirstName.getText()).toString();
        strEtLastName = Objects.requireNonNull(etLastName.getText()).toString();
        strEtEmail = Objects.requireNonNull(etEmail.getText()).toString();
        strEtPassword = Objects.requireNonNull(etPassword.getText()).toString();
        if (!isEverythingValid()) {
            Toast.makeText(this, "Invalid argument", Toast.LENGTH_SHORT).show();
            return;
        }
        initSignUp();
    }

    private void initSignUp() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(strEtEmail, strEtPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                writeDataToFirebase();
            }
        });
    }

    private void writeDataToFirebase() {
        String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        User user = new User(id, strEtFirstName, strEtLastName, strEtEmail, strEtPassword);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USERS).child(id);
        myRef.setValue(user).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful())
                finish();
            else
                Toast.makeText(SignUpActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
        });

    }

    private boolean isEverythingValid() {
        if (strEtFirstName.equals("")) {
            etFirstName.setError("Field empty");
            return false;
        }
        if (strEtLastName.equals("")) {
            etLastName.setError("Field empty");
            return false;
        }
        if (strEtEmail.equals("") | !strEtEmail.contains("@")) {
            etEmail.setError("Invalid argument");
            return false;
        }
        return true;
    }
}