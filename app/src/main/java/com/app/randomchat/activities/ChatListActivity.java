package com.app.randomchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.app.randomchat.adapters.TypeRecyclerViewAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements Info {

    public static User toUser;
    RecyclerView rvChatList;
    List<Super> messageList;
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
    boolean isFirst = true;
    SimpleDraweeView ivUserProfile;
    List<String> priorityList;
    List<String> genderList;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        ibChats = findViewById(R.id.ib_chats);
        initDrawerConfig();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initChatList();

        initCurrentUser();

    }

    private void initDrawerConfig() {
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ibChats.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        View hView = navigationView.getHeaderView(0);

        TextView tvBack = hView.findViewById(R.id.tv_back);
        tvBack.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        ivUserProfile = hView.findViewById(R.id.iv_user_profile);
        spPriority = hView.findViewById(R.id.spinner);
        spGender = hView.findViewById(R.id.sp_gender);

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
    }

    private void initCurrentUser() {
        myRef.child(USERS).child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Log.i(TAG, "onDataChange: " + currentUser);
                initOnlineStatus();
                initUserImage();

            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void initOnlineStatus() {
        Log.i(TAG, "initOnlineStatus: ");
        OnlineUser onlineUser = new OnlineUser(currentUserId, currentUser.getGender());
        myRef.child(ONLINE_USERS).child(currentUser.getGender()).child(currentUserId).setValue(onlineUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.child(ONLINE_USERS).child(currentUserId).removeValue();
        FirebaseAuth.getInstance().signOut();
    }

    private void initChatList() {
        rvChatList = findViewById(R.id.rv_chats);
        messageList = new ArrayList<>();
        usersFromList = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USER_CON_HISTORY).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                List<UserConHistory> userConHistories = new ArrayList<>();
                for (DataSnapshot sss : dataSnapshot.getChildren()) {
                    UserConHistory conHistory = sss.getValue(UserConHistory.class);
                    userConHistories.add(conHistory);
                }

                messageList.addAll(userConHistories);

                TypeRecyclerViewAdapter typeRecyclerViewAdapter = new
                        TypeRecyclerViewAdapter(ChatListActivity.this, messageList, TYPE_MESSAGE);
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

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    userList.add(childSnapshot.getValue(OnlineUser.class));

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
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.getUserId()))
                continue;

            initTargetUser(user.getUserId());

            intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra(KEY_TARGET_USER_ID, user.getUserId());
            startActivity(intent);
            break;
        }
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
                List<OnlineUser> femaleUserList = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    femaleUserList.add(childSnapshot.getValue(OnlineUser.class));
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