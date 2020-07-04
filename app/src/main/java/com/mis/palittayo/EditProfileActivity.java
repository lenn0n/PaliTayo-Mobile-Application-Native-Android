package com.mis.palittayo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditProfileActivity extends AppCompatActivity {
    ImageView open_uploader;
    EditText editName, editAddress, editAge, edit_phone;
    Spinner spinner_gender;
    Button save_btn;
    String userID;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    public StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        open_uploader = findViewById(R.id.open_uploader);
        edit_phone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        editAge = findViewById(R.id.editAge);
        editName = findViewById(R.id.editName);
        spinner_gender = findViewById(R.id.spinner_gender);
        save_btn = findViewById(R.id.save_btn);
        editName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reloadItems();
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
                progressDialog.setMessage("Working.. Please Wait...");
                progressDialog.setTitle("Updating...");
                progressDialog.show();
                DocumentReference contact = FireInstance.collection("users").document(userID);
                contact.update("Name", editName.getText().toString());
                contact.update("Address", editAddress.getText().toString());
                contact.update("Age", editAge.getText().toString());
                contact.update("Phone", edit_phone.getText().toString());
                contact.update("Gender", spinner_gender.getSelectedItem().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Updated Successfully!", Toast.LENGTH_LONG).show();
                                Intent go = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                go.putExtra("userID", userID);
                                startActivity(go);
                            }
                        });
            }
        });
        open_uploader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        });


    }
    public void reloadItems(){
        FireInstance.collection("users").document(""+userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            editName.setText(document.getString("Name"));
                            editAddress.setText(document.getString("Address"));
                            editAge.setText(document.getString("Age"));
                            edit_phone.setText(document.getString("Phone"));
                            Glide.with(getApplicationContext())
                                    .load(document.getString("profilePicturePath"))
                                    .into(open_uploader);
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: CROPPED IMAGE MODE
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
                progressDialog.setMessage("Loading Now...");
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                Uri resultUri  = result.getUri(); // instead, lets use this cool one. CROPPED version.
                storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
                final StorageReference filepath = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                DocumentReference contact = FireInstance.collection("users").document(userID);
                                contact.update("profilePicturePath", downloadUrl.toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                reloadItems();
                                                Toast.makeText(EditProfileActivity.this, "Profile Image Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                }
                            });
                    }
                    else{
                        //Firebase Bucket is not yet opened!
                        progressDialog.dismiss();
                        String message = task.getException().toString();
                        Toast.makeText(EditProfileActivity.this, "Error! :" +message, Toast.LENGTH_SHORT).show();
                    }
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //displaying the upload progress
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                            }
                        });
            }

        }
    }
}
