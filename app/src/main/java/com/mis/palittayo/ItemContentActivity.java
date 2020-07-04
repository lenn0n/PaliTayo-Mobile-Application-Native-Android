package com.mis.palittayo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemContentActivity extends AppCompatActivity {
    String sellerID, documentID, itemPicture, sellerPhone, itemTags;
    TextView loadItemTags, sendMsgLBL, loadPostName, loadPostPrice, loadPostDate, loadSellerName, loadSellerRatings, loadSellerLocation, loadPostContent;
    ImageView loadPostPicture, rateUp, rateDown;
    Button goBack;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_content);

        loadPostName = findViewById(R.id.loadPostName);
        goBack = findViewById(R.id.goBackBTN_content);
        loadPostPrice = findViewById(R.id.loadPostPrice);
        loadPostDate = findViewById(R.id.loadPostDate);
        loadItemTags = findViewById(R.id.loadItemTags);
        rateUp = findViewById(R.id.rateUp_button);
        rateDown = findViewById(R.id.rateDown_button);
        loadSellerName = findViewById(R.id.loadSellerName);
        loadSellerRatings = findViewById(R.id.loadSellerRatings);
        loadSellerLocation = findViewById(R.id.loadSellerPlace);
        loadPostPicture = findViewById(R.id.loadPostPicture);
        sendMsgLBL = findViewById(R.id.label_sendTxt);
        loadPostContent = findViewById(R.id.loadPostContent);

        if(getIntent().hasExtra("sellerID")) {
            sellerID = getIntent().getExtras().getString("sellerID");
        }
        if(getIntent().hasExtra("documentID")) {
            documentID = getIntent().getExtras().getString("documentID");
        }
        if(getIntent().hasExtra("itemPicture")) {
            itemPicture = getIntent().getExtras().getString("itemPicture");
        }
        loadSellerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goProfile = new Intent(ItemContentActivity.this, ProfileActivity.class);
                goProfile.putExtra("userID", sellerID);
                startActivity(goProfile);
            }
        });
        Glide.with(getApplicationContext())
                .load(itemPicture)
                .into(loadPostPicture);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.setTitle("Please Wait...");
        progressDialog.show();
        FireInstance.collection("posts")
                .document(documentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            loadPostName.setText(document.getString("itemName"));
                            loadPostContent.setText(document.getString("itemContent"));
                            loadPostDate.setText(document.getString("itemDateAdded"));
                            loadPostPrice.setText(document.getString("itemPrice"));
                            loadSellerName.setText(document.getString("sellerName"));
                            loadItemTags.setText(document.getString("itemTags"));
                            FireInstance.collection("users")
                                    .document(sellerID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @SuppressLint({"DefaultLocale", "SetTextI18n"})
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                loadSellerLocation.setText(document.getString("Address"));
                                                sellerPhone = document.getString("Phone");
                                                Double up, down;
                                                String total;
                                                up = Double.parseDouble(document.getString("Positive"));
                                                down = Double.parseDouble(document.getString("Negative"));
                                                Double computeUp, finalCompute;
                                                if (up > down){
                                                    computeUp = up - down;
                                                    computeUp = computeUp /  (up + down);
                                                    computeUp = computeUp * 100;
                                                }
                                                else if(up < down){
                                                    computeUp = 0.00;
                                                }
                                                else{
                                                    computeUp = 0.00;
                                                }
                                                Double sum;
                                                sum = up + down;
                                                finalCompute = computeUp;
                                                total = String.format("%.2f", finalCompute);
                                                loadSellerRatings.setText(total+ "% ("+String.format("%.0f", sum)+" votes)");
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                            goBack.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onBackPressed();
                                }
                            });

                        }

                    }
                });
    sendMsgLBL.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userUID.equals(sellerID)){

            }
            else{
                Intent goSMS = new Intent(ItemContentActivity.this, SMSActivity.class);
                goSMS.putExtra("phone", sellerPhone);
                goSMS.putExtra("message", "Replying to (" + loadPostName.getText().toString() + "): ");
                startActivity(goSMS);
            }

        }
    });
    rateUp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rateUser("up");
        }
    });
    rateDown.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rateUser("down");
        }
    });
    }

    public void rateUser(final String what){
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(sellerID)){

        }
        else{
            //TODO: CHECK votes if already exists!
            FireInstance.collection("votes")
                    .document(userUID)
                    .collection("voted")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                boolean isVoted = false;
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    if (document.getId().equals(sellerID)){
                                        isVoted = true;
                                    }
                                }
                                if (isVoted){
                                    //TODO: Already voted this user!
                                    Toast.makeText(ItemContentActivity.this, "You have already voted this user!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Voted", "YES");
                                    FireInstance.collection("votes")
                                        .document(userUID)
                                        .collection("voted")
                                        .document(sellerID)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FireInstance.collection("users")
                                                        .document(sellerID)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()){
                                                                    String up, down;
                                                                    int up_int = 0, down_int = 0;
                                                                    DocumentSnapshot document = task.getResult();
                                                                    up = document.getString("Positive");
                                                                    down = document.getString("Negative");
                                                                    if (what.equals("up")){
                                                                        //TODO: Up
                                                                        up_int = Integer.parseInt(up) + 1;
                                                                        up = String.valueOf(up_int);
                                                                    }
                                                                    else{
                                                                        //TODO: Down
                                                                        down_int = Integer.parseInt(down) + 1;
                                                                        down = String.valueOf(down_int);
                                                                    }
                                                                    DocumentReference updateUser = FireInstance.collection("users").document(sellerID);
                                                                    updateUser.update("Positive", up);
                                                                    updateUser.update("Negative", down)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    //TODO: DONE!
                                                                                    Toast.makeText(ItemContentActivity.this, "Your Feedback was sent to the user! Thank you for supporting!", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        });

                                }

                            }
                        }
                    });
        }

    }
}
