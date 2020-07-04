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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Locale;

public class UpdateItemsActivity extends AppCompatActivity {
    String documentID, itemName,itemPrice, itemDesc, itemPicture, itemStatus, itemTag;
    EditText editName, editPrice, editDesc, editTag;
    ImageView editPicture;
    Spinner editStatus;
    Button myEditBtn;
    Uri resultUri;
    TextView gobacklbl;
    boolean hasSelected = false;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    public StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_items);
        myEditBtn = findViewById(R.id.my_edit_submit);
        editName = findViewById(R.id.my_item_edit_name);
        editPrice = findViewById(R.id.my_item_edit_price);
        gobacklbl = findViewById(R.id.goBack_lbl);
        editDesc = findViewById(R.id.my_item_edit_desc);
        editTag = findViewById(R.id.my_item_edit_tags);
        editPicture = findViewById(R.id.my_item_edit_picture);
        editStatus = findViewById(R.id.my_item_edit_status);
        gobacklbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if(getIntent().hasExtra("documentID")) {
            documentID = "";
            documentID = getIntent().getExtras().getString("documentID");
        }
        if(getIntent().hasExtra("name")) {
            itemName = "";
            itemName = getIntent().getExtras().getString("name");
            editName.setText(itemName);
        }
        if(getIntent().hasExtra("tags")) {
            itemTag = "";
            itemTag = getIntent().getExtras().getString("tags");
            editTag.setText(itemTag);
        }
        if(getIntent().hasExtra("price")) {
            itemPrice = "";
            itemPrice = getIntent().getExtras().getString("price");
            editPrice.setText(itemPrice.substring(7));
        }
        if(getIntent().hasExtra("desc")) {
            itemDesc ="";
            itemDesc = getIntent().getExtras().getString("desc");
            editDesc.setText(itemDesc);
        }
        if(getIntent().hasExtra("picture")) {
            itemPicture = "";
            itemPicture = getIntent().getExtras().getString("picture");
            Glide.with(getApplicationContext())
                    .load(itemPicture)
                    .into(editPicture);
        }
        if(getIntent().hasExtra("status")) {
            itemStatus = "";
            itemStatus = getIntent().getExtras().getString("status");
            if (itemStatus.contains("Available")){
                editStatus.setSelection(0);
            }
            else{
                editStatus.setSelection(1);
            }
        }
        editPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasSelected = true;
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(UpdateItemsActivity.this);
            }
        });

        myEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(UpdateItemsActivity.this);
                progressDialog.setMessage("Please Wait");
                progressDialog.setTitle("Updating...");
                progressDialog.show();
                if (!hasSelected){
                    //TODO: Nothing was changed, no uploading
                    DocumentReference updatePost = FireInstance.collection("posts").document(documentID);
                    updatePost.update("itemName", editName.getText().toString());
                    updatePost.update("itemPrice", editPrice.getText().toString());
                    updatePost.update("price", Integer.parseInt(editPrice.getText().toString()));
                    updatePost.update("itemContent", editDesc.getText().toString());
                    updatePost.update("itemTags", editTag.getText().toString());
                    updatePost.update("itemStatus", editStatus.getSelectedItem().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(UpdateItemsActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent goBack = new Intent(UpdateItemsActivity.this, ViewItemsActivity.class);
                                    goBack.putExtra("loadMyItems", "OK");
                                    startActivity(goBack);
                                }
                            });
                }
                else{
                    if (resultUri != null){
                        //todo: DELETE PHOTO TO PHOTO BUCKET!
                        final FirebaseStorage delRef = FirebaseStorage.getInstance().getReference().getStorage();
                        StorageReference photoRef = delRef.getReferenceFromUrl(itemPicture);
                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
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
                                                    DocumentReference updatePost = FireInstance.collection("posts").document(documentID);
                                                    updatePost.update("itemName", editName.getText().toString());
                                                    updatePost.update("itemPicture", postPicture.toString());
                                                    updatePost.update("itemPrice", editPrice.getText().toString());
                                                    updatePost.update("price", Integer.parseInt(editPrice.getText().toString()));
                                                    updatePost.update("itemContent", editDesc.getText().toString());
                                                    updatePost.update("itemStatus", editStatus.getSelectedItem().toString())
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(UpdateItemsActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                                                    Intent goBack = new Intent(UpdateItemsActivity.this, ViewItemsActivity.class);
                                                                    goBack.putExtra("loadMyItems", "OK");
                                                                    startActivity(goBack);
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
                        });
                    }
                    else{
                        Toast.makeText(UpdateItemsActivity.this, "Please select valid image!", Toast.LENGTH_SHORT).show();
                    }
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
             resultUri = result.getUri(); // We just need URI File
                editPicture.setImageURI(resultUri);
            }
        }
    }
}
