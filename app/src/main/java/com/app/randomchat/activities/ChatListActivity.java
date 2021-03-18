package com.app.randomchat.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.FriendlyMessage;
import com.app.randomchat.Pojo.Super;
import com.app.randomchat.R;
import com.app.randomchat.adapters.TypeRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements Info {

    RecyclerView rvChatList;
    List<Super> messageList;
    String currentUserId;
    List<String> usersFromList;
    List<FriendlyMessage> friendlyMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initChatList();

    }

    private void initChatList() {
        rvChatList = findViewById(R.id.rv_chats);
        friendlyMessageList = new ArrayList<>();
        messageList = new ArrayList<>();
        usersFromList = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Conversations");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                friendlyMessageList.clear();
                for (DataSnapshot sss : dataSnapshot.getChildren()) {
                    boolean firstIteration = true;
                    for (DataSnapshot snapshot : sss.getChildren()) {
                        if (firstIteration) {
                            firstIteration = false;
                            continue;
                        }
                        FriendlyMessage message = snapshot.getValue(FriendlyMessage.class);
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (message.getFromUser().equals(currentUserId)) {
                            continue;
                        }


                        if (currentUserId.equals(message.getToUser()) | currentUserId.equals(message.getFromUser())) {
                            usersFromList.add(message.getFromUser());
                            friendlyMessageList.add(message);
                        }
                        break;
                    }
                }

                for (FriendlyMessage friendlyMessage : friendlyMessageList) {
                    Log.i(TAG, "onDataChange: LIST DISPLAY to : " + friendlyMessage.getToUser());
                    Log.i(TAG, "onDataChange: LIST DISPLAY con : " + friendlyMessage.getConversationId());
                }

                messageList.addAll(friendlyMessageList);
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
}