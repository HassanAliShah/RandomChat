package com.app.randomchat.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.R;
import com.app.randomchat.Utils.ImagePicker;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements Info {

    TextInputEditText etFirstName;
    TextInputEditText etLastName;
    TextInputEditText etEmail;
    TextInputEditText etPassword;

    RadioButton rbMale;
    RadioButton rbFemale;

    String strEtFirstName = "";
    String strEtLastName = "";
    String strEtEmail = "";
    String strEtPassword = "";

    String strRbGender = "";

    ProgressBar progressBar;

    SimpleDraweeView ivProfileImage;

    Bitmap bmpUserImage;

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
        ivProfileImage = findViewById(R.id.iv_profile_image);
        rbFemale = findViewById(R.id.rb_female);
        rbMale = findViewById(R.id.rb_male);

        rbMale.setChecked(true);

    }

    public void signUp(View view) {
        progressBar.setVisibility(View.VISIBLE);
        strEtFirstName = Objects.requireNonNull(etFirstName.getText()).toString();
        strEtLastName = Objects.requireNonNull(etLastName.getText()).toString();
        strEtEmail = Objects.requireNonNull(etEmail.getText()).toString();
        strEtPassword = Objects.requireNonNull(etPassword.getText()).toString();

        if (rbFemale.isChecked())
            strRbGender = FEMALE;
        else
            strRbGender = MALE;


        if (!isEverythingValid()) {
            Toast.makeText(this, "Invalid argument", Toast.LENGTH_SHORT).show();
            return;
        }
        initSignUp();
    }

    private void initSignUp() {
        Log.i(TAG, "initSignUp: ");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(strEtEmail, strEtPassword).addOnCompleteListener(task -> {
            Log.i(TAG, "initSignUp: " + task.getException());
            if (task.isSuccessful()) {
                if (bmpUserImage != null)
                    uploadImage(bmpUserImage);
                else
                    writeDataToFirebase(defaultImageUrl);
            } else {
                Log.i(TAG, "initSignUp: Exception  " + task.getException());
                Toast.makeText(this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

            }
        });
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
        ref = FirebaseStorage.getInstance().getReference("Images").child(user.getUid());
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading....");
        pd.show();
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            new Handler().postDelayed(pd::dismiss, 500);
            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            result.addOnSuccessListener(uri -> {
                String urlToImage = uri.toString();
                writeDataToFirebase(urlToImage);
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

    private void writeDataToFirebase(String urlToImage) {
        Log.i(TAG, "writeDataToFirebase: ");
        String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        User user = new User(id, strEtFirstName, strEtLastName, strEtEmail, strEtPassword, urlToImage, strRbGender, BOTH, "18", "50", "18");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USERS).child(id);
        myRef.setValue(user).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onComplete: " + task.getException());
            }
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

    public void openGallery(View view) {
        initGalleryAccess();
    }

    public void initGalleryAccess() {
        if (isStoragePermissionGranted()) {
            openGallery();
        }
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
                    ivProfileImage.setImageURI(imageUri);
                    bmpUserImage = ImagePicker.getImageFromResult(this, resultCode, data);
                }
            }
            Log.i(TAG, "onActivityResult: " + data);

        }
    }

}