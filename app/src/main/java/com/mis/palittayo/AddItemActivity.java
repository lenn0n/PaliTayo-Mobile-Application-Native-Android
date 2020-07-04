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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {
    Button goBack_add, item_sell_btn;
    ImageView select_image_upload;
    EditText item_name_add, item_price_add, item_desc_add, item_tags_add;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    public StorageReference storageReference;
    Uri resultUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        goBack_add = findViewById(R.id.goBack_add);
        item_name_add = findViewById(R.id.item_name_add);
        item_tags_add = findViewById(R.id.item_tags_add);
        item_desc_add = findViewById(R.id.item_desc_add);
        item_price_add = findViewById(R.id.item_price_add);
        select_image_upload = findViewById(R.id.select_image_upload);
        item_sell_btn = findViewById(R.id.item_sell_btn);
        goBack_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        select_image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AddItemActivity.this);
            }
        });
        item_sell_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultUri != null || item_name_add.getText().toString().equals("") || item_price_add.getText().toString().equals("") || item_desc_add.getText().toString().equals("")){
                    final ProgressDialog progressDialog = new ProgressDialog(AddItemActivity.this);
                    progressDialog.setMessage("Loading Now...");
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    storageReference = FirebaseStorage.getInstance().getReference().child("Post Images");
                    Calendar postDate = Calendar.getInstance();
                    final String datePost = DateFormat.getDateInstance(DateFormat.FULL).format(postDate.getTime());
                    final String timeLapsed = new SimpleDateFormat("hh_mm_ss_a", Locale.US).format(new Date());
                    final StorageReference filepath = storageReference.child(datePost+"_"+timeLapsed+"_"+FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Uri postPicture = uri;
                                        //TODO: Add data to posts collections
                                        progressDialog.setMessage("Posting...");
                                        progressDialog.setTitle("Checking...");
                                        Calendar calendar = Calendar.getInstance();
                                        final String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                                        final String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.US).format(new Date());
                                        Map<String, Object> postDetails = new HashMap<>();
                                        postDetails.put("sellerName", ""+FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                        postDetails.put("sellerID", ""+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        postDetails.put("itemName", ""+item_name_add.getText().toString());
                                        postDetails.put("itemPrice", ""+item_price_add.getText().toString());
                                        postDetails.put("itemContent", ""+item_desc_add.getText().toString());
                                        postDetails.put("itemStatus", "Available");
                                        postDetails.put("itemPicture",""+postPicture.toString());
                                        postDetails.put("itemTags",""+item_tags_add.getText().toString());
                                        postDetails.put("price", Integer.parseInt(item_price_add.getText().toString()));
                                        postDetails.put("itemDateAdded", currentDate+" "+currentTime);
                                        FireInstance.collection("posts")
                                                .add(postDetails)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(AddItemActivity.this, "Item was posted successfully!", Toast.LENGTH_SHORT).show();
                                                        Intent proceed = new Intent(AddItemActivity.this, ViewItemsActivity.class);
                                                        startActivity(proceed);
                                                    }
                                                });
                                    }
                                });
                            }

                        }
                    })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    //displaying the upload progress
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    progressDialog.setMessage("Uploaded: " + ((int) progress) + "%...");
                                }
                            });
                }
                else{
                    //TODO: No selected file! Empty URI
                    Toast.makeText(AddItemActivity.this, "Missing fields!", Toast.LENGTH_SHORT).show();
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
                resultUri  = result.getUri(); // We just need URI File
                select_image_upload.setImageURI(resultUri);
            }
        }
    }
}
