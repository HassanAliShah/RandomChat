package com.app.randomchat.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.Message;
import com.app.randomchat.Pojo.Super;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.Pojo.UserConHistory;
import com.app.randomchat.R;
import com.app.randomchat.adapters.TypeRecyclerViewAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements Info {

    User toUser;
    User fromUser;
    List<Super> friendlyMessages;
    String fromUserId;
    String toUserId;
    String conversationId = "";
    List<String> userList;
    TextView tvTitle;
    private RecyclerView mMessageListView;
    private TypeRecyclerViewAdapter mMessageAdapter;
    private EditText mMessageEditText;
    private ImageButton mSendButton;
    private DatabaseReference mMessageDbReference;
    private ChildEventListener mChildEvListener;
    private StorageReference mStoreReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendlyMessages = new ArrayList<>();

        checkIntent();

        tvTitle = findViewById(R.id.title);
        FirebaseDatabase mFireDb = FirebaseDatabase.getInstance();
        FirebaseStorage mFireStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mMessageDbReference = mFireDb.getReference().child(CONVERSATIONS);
        mStoreReference = mFireStorage.getReference().child(CHATTY_PHOTOS);

        ProgressBar mProgressBar = findViewById(R.id.progressBar);
        mMessageListView = findViewById(R.id.messageListView);
        ImageButton mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        mMessageAdapter = new TypeRecyclerViewAdapter(this, friendlyMessages, Info.TYPE_REC_MESSAGE);
        mMessageListView.setAdapter(mMessageAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mPhotoPickerButton.setOnClickListener(view -> requestExternalStorage());

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mSendButton.setOnClickListener(view -> requestInternet());

        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings = new FirebaseRemoteConfigSettings
                .Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(firebaseRemoteConfigSettings);

        Map<String, Object> defConfig = new HashMap<>();
        defConfig.put(CHATTY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);

        mFirebaseRemoteConfig.setDefaultsAsync(defConfig);
        fetchConfig();

    }

    private void checkIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String userId = bundle.get(KEY_TARGET_USER_ID).toString();
            setFromAndTo(userId);
            Log.i(TAG, "onDataChange: " + userList);
            setUser(1, userId);
        }
    }

    private void setFromAndTo(String toUserId) {
        fromUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.toUserId = toUserId;

        if (fromUserId.equals(toUserId))
            finish();

        userList = new ArrayList<>();
        userList.clear();
        userList.add(fromUserId);
        userList.add(toUserId);
//        checkConversations();
    }

    private void checkConversations() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(CONVERSATIONS);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot ss : snapshot.getChildren()) {
                        Message friendlyMessage = ss.getValue(Message.class);
                        assert friendlyMessage != null;
                        if (
                                userList.get(0).equals(friendlyMessage.getFromUser()) &&
                                        userList.get(1).equals(friendlyMessage.getToUser())
                                        || userList.get(1).equals(friendlyMessage.getFromUser()) &&
                                        userList.get(0).equals(friendlyMessage.getToUser())
                        ) {
                            Log.i(TAG, "onDataChange: condition true");
                            conversationId = friendlyMessage.getConversationId();
                            attachDatabaseReadListener();
                            return;
                        }
                    }
                }
                createConversation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void createConversation() {
        // Write a message to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String conId = UUID.randomUUID().toString();

        String messageId = String.valueOf(System.currentTimeMillis());

        Message friendlyMessage = new Message(messageId, "", toUserId,
                "", "", fromUserId, "", conId);
        DatabaseReference myRef = database.getReference(CONVERSATIONS).child(conId)
                .child(messageId);
        myRef.setValue(friendlyMessage);

        String conId2 = UUID.randomUUID().toString();
        String messageId2 = String.valueOf(System.currentTimeMillis());
        Message friendlyMessage2 = new Message(messageId2, "", fromUserId + "",
                "", "", toUserId + "", "", conId2);
        DatabaseReference myRef2 = database.getReference(CONVERSATIONS).child(conId2)
                .child(messageId2);
        myRef2.setValue(friendlyMessage2);
        attachDatabaseReadListener();

    }

    private void setUser(int i, String userId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(userId);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User value = dataSnapshot.getValue(User.class);
                if (i == 1) {
                    toUser = value;
                    assert toUser != null;
                    String userName = toUser.getFirstName() + " " + toUser.getLastName();
                    tvTitle.setText(userName);
                    String fromUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    setUser(2, fromUserId);
                } else {
                    fromUser = value;
                    checkConversations();
                    return;
                }
                Log.d(TAG, "Value is: " + value);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(ChatActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            final StorageReference mStoreRef = mStoreReference.child(selectedImage.getLastPathSegment());
            mStoreRef.putFile(selectedImage).continueWithTask(task -> mStoreRef.getDownloadUrl()).addOnSuccessListener(uri -> {
                String id = UUID.randomUUID().toString();
                String fromUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                String fromUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (!conversationId.equals("")) {
                    Message friendlyMessage = new Message(id, null,
                            fromUserId, fromUserName, uri.toString(), toUser.getId(), fromUser.getUserImageUrl(), conversationId);
                    mMessageDbReference.child(conversationId).child(id).setValue(friendlyMessage);
                    mMessageEditText.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Please wait", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.sign_out_menu) {
            AuthUI.getInstance().signOut(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        detachDatabaseReadListener();

    }

    private void detachDatabaseReadListener() {
        if (mChildEvListener != null) {
            mMessageDbReference.removeEventListener(mChildEvListener);
            mChildEvListener = null;
        }
    }

    void attachDatabaseReadListener() {
        Log.i(TAG, "attachDatabaseReadListener: ");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(CONVERSATIONS).child(conversationId);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendlyMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Message friendlyMessage = snapshot.getValue(Message.class);
                    if (friendlyMessage != null) {
                        try {
                            Log.i(TAG, "onDataChange: Message : " + friendlyMessage.getText());
                            String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            friendlyMessage.setShowOnRight(friendlyMessage.getFromUser().equals(currentUserId));
                            friendlyMessages.add(friendlyMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i(TAG, "onDataChange: " + e);
                        }
                    } else {
                        Log.i(TAG, "Message null: ");
                    }
                }


                if (friendlyMessages.size() <= 0) {
                    Log.i(TAG, "onDataChange: Not created history");
                    mMessageListView.setAdapter(mMessageAdapter);
                    return;
                }

                Message message = (Message) friendlyMessages.get(friendlyMessages.size() - 1);
                UserConHistory userConHistory = new UserConHistory(toUserId, conversationId, toUser.getUserImageUrl(),
                        toUser.getFirstName() + " " + toUser.getLastName(), message.getText());

                FirebaseDatabase.getInstance()
                        .getReference(USER_CON_HISTORY)
                        .child(fromUserId)
                        .child(conversationId).setValue(userConHistory);

                UserConHistory userConHistory2 = new UserConHistory(fromUserId, conversationId, fromUser.getUserImageUrl(),
                        fromUser.getFirstName() + " " + fromUser.getLastName(), message.getText());

                FirebaseDatabase.getInstance()
                        .getReference(USER_CON_HISTORY)
                        .child(toUserId)
                        .child(conversationId).setValue(userConHistory2);

//                mMessageAdapter.notifyDataSetChanged();
                mMessageListView.setAdapter(mMessageAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void fetchConfig() {
        long cacheExpiration = 3600;

        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(aVoid -> {
            mFirebaseRemoteConfig.fetchAndActivate();
            applyLengthLimit();
        }).addOnFailureListener(e -> applyLengthLimit());
    }

    private void applyLengthLimit() {
        long chatty_message_length = mFirebaseRemoteConfig.getLong(CHATTY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter((int) chatty_message_length)});
    }

    private void requestExternalStorage() {

        Dexter.withActivity(this)
                .withPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )


                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent inte = new Intent(Intent.ACTION_GET_CONTENT);
                        inte.setType("image/jpeg");
                        inte.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(Intent.createChooser(inte, "Complete Action Using"), RC_PHOTO_PICKER);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }

                })
                .withErrorListener(error -> Log.e("Dexter", "There was an error: " + error.toString()))
                .onSameThread()
                .check();
    }

    private void requestInternet() {
        Dexter.withActivity(this)
                .withPermission(
                        Manifest.permission.INTERNET
                )
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        String id = String.valueOf(System.currentTimeMillis());
                        String fromUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String fromUserName = fromUser.getFirstName() + " " + fromUser.getLastName();
                        if (!conversationId.equals("")) {
                            Message friendlyMessage = new Message(id, mMessageEditText.getText().toString(),
                                    fromUserId, fromUserName, null, toUser.getId(), fromUser.getUserImageUrl(), conversationId);
                            mMessageDbReference.child(conversationId).child(id).setValue(friendlyMessage);
                            mMessageEditText.setText("");

                        } else {
                            Toast.makeText(ChatActivity.this, "Please wait", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(error -> Log.e("Dexter", "There was an error: " + error.toString()))
                .onSameThread()
                .check();
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}