package com.app.randomchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements Info {

    TextInputEditText etEmail;
    TextInputEditText etPassword;

    String strEtEmail = "";
    String strEtPassword = "";

    ProgressBar progressBar;

    Activity loginActivity;

    FirebaseAuth mAuth;
    CallbackManager callbackManager;
    LoginButton loginButton;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initAnimation();

        progressBar = findViewById(R.id.pb_log_in);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        loginActivity = this;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, ChatListActivity.class));
            finish();
            return;
        }
        initGoogleSignIn();

    }

    private void initAnimation() {
        LottieAnimationView animationView = findViewById(R.id.lottie_animation);
        animationView.addValueCallback(
                new KeyPath("Dialog 1", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        );

        animationView.addValueCallback(
                new KeyPath("Dialog 2", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(getColor(R.color.orange), PorterDuff.Mode.SRC_ATOP)
        );
        animationView.addValueCallback(
                new KeyPath("Text 1", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(getColor(R.color.quantum_grey400), PorterDuff.Mode.SRC_ATOP)
        );
        animationView.addValueCallback(
                new KeyPath("Text 2", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(getColor(R.color.quantum_grey400), PorterDuff.Mode.SRC_ATOP)
        );

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

    private void initGoogleSignIn() {

        Log.i(TAG, "initGoogleSignIn: ");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

    }

    public void continueWithGoogle(View view) {
        // Configure Google Sign In
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            return;
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            firebaseGoogleAuth(acc);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "handleSignInResult: " + e);
        }

    }

    private void firebaseGoogleAuth(GoogleSignInAccount acc) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Sign in Successful", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        writeDataToDatabase(user.getDisplayName(), user.getEmail());
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            writeDataToDatabase(user.getDisplayName(), user.getEmail());
                        }

                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(this, e -> {
            e.printStackTrace();
            Log.i(TAG, "onFailure: " + e.getMessage());
        });
    }

    private void writeDataToDatabase(String displayName, String email) {
        String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        Log.i(TAG, "writeDataToDatabase: ");

        User user = new User(id, displayName, "", email, strEtPassword, defaultImageUrl, MALE, BOTH, "18", "50", "18");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USERS).child(id);
        myRef.setValue(user).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onComplete: " + task.getException());
            }
        });
    }
}