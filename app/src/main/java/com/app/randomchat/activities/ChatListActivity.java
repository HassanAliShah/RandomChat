package com.app.randomchat.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.OnlineUser;
import com.app.randomchat.Pojo.Super;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.Pojo.UserConHistory;
import com.app.randomchat.R;
import com.app.randomchat.Utils.ImagePicker;
import com.app.randomchat.adapters.TypeRecyclerViewAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ChatListActivity extends AppCompatActivity implements Info {

    public static User toUser;
    RecyclerView rvChatList;
    List<Super> historyList;
    List<String> usersFromList;
    String currentUserId;
    User currentUser;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Spinner spPriority;
    Spinner spGender;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ImageButton ibChats;
    SimpleDraweeView ivUserProfile;
    List<String> priorityList;
    List<String> genderList;
    Intent intent;
    TextInputEditText etUserName;
    TextInputEditText etAge;
    TextInputEditText etAgeLower;
    TextInputEditText etAgeUpper;


    ProgressBar pbLoading;
    View hView;
    ProgressDialog dialog;
    Bitmap bmpUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        initAds();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        ibChats = findViewById(R.id.ib_chats);
        pbLoading = findViewById(R.id.pb_load);
        pbLoading.setVisibility(View.VISIBLE);
        initDrawerConfig();

        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        initChatList();

        initCurrentUser();

    }

    private void initAds() {

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }
                });
            }
        }, 0, 5 * 60 * 1000);

    }

    private void initDrawerConfig() {
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ibChats.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        hView = navigationView.getHeaderView(0);
        etUserName = hView.findViewById(R.id.et_username);
        etAge = hView.findViewById(R.id.et_age);
        etAgeLower = hView.findViewById(R.id.et_age_low);
        etAgeUpper = hView.findViewById(R.id.et_age_up);
        ImageButton tvBack = hView.findViewById(R.id.tv_back);
        tvBack.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        ivUserProfile = hView.findViewById(R.id.iv_user_profile);
        spPriority = hView.findViewById(R.id.spinner);
        spGender = hView.findViewById(R.id.sp_gender);

        ivUserProfile.setOnClickListener(v -> openGalleryWindow());

        priorityList = new ArrayList<>();
        priorityList.add(BOTH);
        priorityList.add(FEMALE);
        priorityList.add(MALE);

        genderList = new ArrayList<>();
        genderList.add(MALE);
        genderList.add(FEMALE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, R.layout.tv_text,
                        priorityList);
        adapter.setDropDownViewResource(R.layout
                .tv_text_checked);

        ArrayAdapter<String> adapterGender = new ArrayAdapter<>
                (this, R.layout.tv_text,
                        genderList);

        adapterGender.setDropDownViewResource(R.layout
                .tv_text_checked);
        spGender.setAdapter(adapterGender);

        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected: " + position);

                if (currentUser == null)
                    return;

                currentUser.setGender(genderList.get(position));
                myRef.child(USERS).child(currentUserId).setValue(currentUser);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected: " + position);

                if (currentUser == null)
                    return;

                currentUser.setPriority(priorityList.get(position));
                myRef.child(USERS).child(currentUserId).setValue(currentUser);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spPriority.setAdapter(adapter);


    }

    private void openGalleryWindow() {
        initGalleryAccess();
    }

    public void initGalleryAccess() {
        if (isStoragePermissionGranted()) {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                final Uri imageUri = data.getData();
                if (imageUri != null) {
                    ivUserProfile.setImageURI(imageUri);
                    bmpUserImage = ImagePicker.getImageFromResult(this, resultCode, data);
                    uploadImage();
                }
            }
            Log.i(TAG, "onActivityResult: " + data);

        }
    }

    private void uploadImage() {
        uploadImage(bmpUserImage);
    }

    private void uploadImage(Bitmap bitmap) {
        Log.i(TAG, "uploadImage: ");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        bitmap.recycle();
        this.bmpUserImage.recycle();

        final StorageReference ref;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        ref = FirebaseStorage.getInstance().getReference("Images").child(user.getUid());
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading....");
        pd.show();
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            new Handler().postDelayed(pd::dismiss, 500);
            Task<Uri> result = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl();
            result.addOnSuccessListener(uri -> {
                String urlToImage = uri.toString();
                Log.i(TAG, "uploadImage: " + urlToImage);
                currentUser.setUserImageUrl(urlToImage);
                FirebaseDatabase.getInstance().getReference(USERS).child(currentUser.getId()).setValue(currentUser);
            });
            Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(getApplication(), "Uploading failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            pd.setMessage("Uploaded - " + (int) progress + "%");
        });

    }


    public boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted");
            return true;
        } else {
            Log.v(TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            finish();
    }

    private void initUserImage() {
        ivUserProfile.setImageURI(currentUser.getUserImageUrl());
        spPriority.setSelection(priorityList.indexOf(currentUser.getPriority()));
        spGender.setSelection(genderList.indexOf(currentUser.getGender()));
        String username = currentUser.getFirstName() + " " + currentUser.getLastName();
        etUserName.setText(username);
        etAge.setText(currentUser.getAge());

        etAgeLower.setText(currentUser.getAgeLower());
        etAgeUpper.setText(currentUser.getAgeUpper());

        etAgeLower.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etAgeLower.getText().toString().length() == 2) {
                    etAgeLower.clearFocus();
                    etAgeUpper.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etUserName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                currentUser.setFirstName(Objects.requireNonNull(etUserName.getText()).toString());
                currentUser.setLastName("");
                FirebaseDatabase.getInstance().getReference(USERS)
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .setValue(currentUser);
                etUserName.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hView.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        etAgeUpper.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String strEtAgeUpper = etAgeUpper.getText().toString();
                String strEtAgeLower = etAgeLower.getText().toString();
                try {
                    int ageLower = Integer.parseInt(strEtAgeLower);
                    if (ageLower < 18) {
                        etAgeUpper.setError("Invalid");
                        return true;
                    }
                    int ageUpper = Integer.parseInt(strEtAgeLower);
                    if (ageLower > ageUpper) {
                        Toast.makeText(this, "Upper age must be greater than lower",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } catch (Exception e) {
                    etAge.setError("Invalid argument");
                    return true;
                }

                currentUser.setAgeUpper(strEtAgeUpper);
                currentUser.setAgeLower(strEtAgeLower);
                FirebaseDatabase.getInstance().getReference(USERS)
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .setValue(currentUser);
                etAgeUpper.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hView.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        etAgeLower.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etAgeLower.clearFocus();
                etAgeUpper.requestFocus();
//                String strEtAge = etAgeLower.getText().toString();
//                try {
//                    int age = Integer.parseInt(strEtAge);
//                    if (age < 18) {
//                        Toast.makeText(this, "Cannot be lower than 18", Toast.LENGTH_SHORT).show();
//                        etAgeLower.setError("Invalid argument");
//                        return true;
//                    }
//                } catch (Exception e) {
//                    etAge.setError("Invalid argument");
//                    return true;
//                }
//                currentUser.setAgeLower(strEtAge);
//                FirebaseDatabase.getInstance().getReference(USERS)
//                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
//                        .setValue(currentUser);
//                etAgeLower.clearFocus();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(hView.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        etAge.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String strEtAge = etAge.getText().toString();
                try {
                    int age = Integer.parseInt(strEtAge);
                    if (age < 18) {
                        Toast.makeText(this, "Cannot be lower than 18", Toast.LENGTH_SHORT).show();
                        etAge.setError("Invalid argument");
                        return true;
                    }
                } catch (Exception e) {
                    etAge.setError("Invalid argument");
                    return true;
                }
                currentUser.setAge(strEtAge);
                FirebaseDatabase.getInstance().getReference(USERS)
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .setValue(currentUser);
                etAge.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hView.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void initCurrentUser() {
        myRef.child(USERS).child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Log.i(TAG, "onDataChange: " + currentUser);
                initOnlineStatus();
                initUserImage();
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void initOnlineStatus() {
        Log.i(TAG, "initOnlineStatus: ");
        OnlineUser onlineUser = new OnlineUser(currentUserId, currentUser.getAge());
        myRef.child(ONLINE_USERS).child(currentUser.getGender()).child(currentUserId).setValue(onlineUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        myRef.child(ONLINE_USERS).child(currentUserId).removeValue();
//        FirebaseAuth.getInstance().signOut();
    }

    private void initChatList() {
        rvChatList = findViewById(R.id.rv_chats);
        historyList = new ArrayList<>();
        usersFromList = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USER_CON_HISTORY).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyList.clear();
                List<UserConHistory> userConHistories = new ArrayList<>();
                for (DataSnapshot sss : dataSnapshot.getChildren()) {
                    UserConHistory conHistory = sss.getValue(UserConHistory.class);
                    userConHistories.add(conHistory);
                }

                historyList.addAll(userConHistories);

                if (historyList.isEmpty()) {
                    Toast.makeText(ChatListActivity.this, "click search to start a chat", Toast.LENGTH_SHORT).show();
                }

                TypeRecyclerViewAdapter typeRecyclerViewAdapter = new
                        TypeRecyclerViewAdapter(ChatListActivity.this, historyList, TYPE_MESSAGE);
                typeRecyclerViewAdapter.notifyDataSetChanged();
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatListActivity.this);
                rvChatList.setLayoutManager(linearLayoutManager);
                rvChatList.setAdapter(typeRecyclerViewAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void initMatching(View view) {
        String priority;
        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        dialog.show();

        if (currentUser == null)
            return;

        if (!currentUser.getPriority().equals(BOTH))
            priority = currentUser.getPriority();
        else
            priority = MALE;


        myRef.child(ONLINE_USERS).child(priority).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                List<OnlineUser> userList = new ArrayList<>();

                List<String> userStrings = new ArrayList<>();
                for (Super userConHistory : historyList) {
                    UserConHistory userConHistory1 = (UserConHistory) userConHistory;
                    userStrings.add(userConHistory1.getTargetUserId());
                }


                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    OnlineUser onlineUser = childSnapshot.getValue(OnlineUser.class);
                    assert onlineUser != null;
                    if (userStrings.contains(onlineUser.getUserId()))
                        continue;

                    try {
                        int targetUserAge = Integer.parseInt(onlineUser.getAge());
                        int userUpperAge = Integer.parseInt(currentUser.getAgeUpper());
                        int userLowerAge = Integer.parseInt(currentUser.getAgeLower());

                        if (targetUserAge < userLowerAge || targetUserAge > userUpperAge) {
                            continue;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    userList.add(childSnapshot.getValue(OnlineUser.class));
                }

                Log.i(TAG, "onDataChange: Male Users : " + userList);

                if (currentUser.getPriority().equals(BOTH))
                    initFemaleUsers(userList);
                else
                    initRandomSelection(userList);

            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private void initRandomSelection(List<OnlineUser> userList) {
        Collections.shuffle(userList);
        for (OnlineUser user : userList) {
            if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(user.getUserId()))
                continue;

            initTargetUser(user.getUserId());
            dialog.dismiss();
            intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra(KEY_TARGET_USER_ID, user.getUserId());
            startActivity(intent);
            return;
        }

        Toast.makeText(this, "No user currently online", Toast.LENGTH_SHORT).show();
        dialog.dismiss();


    }

    private void initTargetUser(String targetUserId) {
        myRef.child(USERS).child(targetUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                toUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private void initFemaleUsers(List<OnlineUser> userList) {

        myRef.child(ONLINE_USERS).child(FEMALE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                List<String> userStrings = new ArrayList<>();
                for (Super userConHistory : historyList) {
                    UserConHistory userConHistory1 = (UserConHistory) userConHistory;
                    userStrings.add(userConHistory1.getTargetUserId());
                }


                List<OnlineUser> femaleUserList = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    OnlineUser onlineUser = childSnapshot.getValue(OnlineUser.class);

                    assert onlineUser != null;
                    if (userStrings.contains(onlineUser.getUserId()))
                        continue;

                    userList.add(childSnapshot.getValue(OnlineUser.class));
                    femaleUserList.add(childSnapshot.getValue(OnlineUser.class));
                }

                Log.i(TAG, "onDataChange: Female Users : " + userList);

                userList.addAll(femaleUserList);
                initRandomSelection(userList);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}