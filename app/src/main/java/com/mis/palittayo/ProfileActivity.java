package com.mis.palittayo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    ImageView profile_photo;
    TextView profile_name, profile_address, profile_phone, profile_age, profile_gender, profile_rating;
    String userID;
    Button goback_btn, redirect_to_main;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        redirect_to_main = findViewById(R.id.redirect_to_main);
        profile_photo = findViewById(R.id.profile_photo);
        profile_name = findViewById(R.id.profile_name);
        profile_address = findViewById(R.id.profile_address);
        profile_phone = findViewById(R.id.profile_phone);
        profile_age = findViewById(R.id.profile_age);
        profile_gender = findViewById(R.id.profile_gender);
        profile_rating = findViewById(R.id.profile_rating);
        goback_btn = findViewById(R.id.goback_btn);
        goback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ProfileActivity.this, ViewItemsActivity.class);
                startActivity(go);
            }
        });
        if(getIntent().hasExtra("userID")){
            userID = getIntent().getExtras().getString("userID");
            FireInstance.collection("users").document(""+userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint({"DefaultLocale", "SetTextI18n"})
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                Double up, down;
                                String total;
                                DocumentSnapshot document = task.getResult();
                                profile_name.setText(document.getString("Name"));
                                profile_address.setText(document.getString("Address"));
                                profile_age.setText(document.getString("Age"));
                                profile_gender.setText(document.getString("Gender"));
                                profile_phone.setText(document.getString("Phone"));
                                Glide.with(getApplicationContext())
                                        .load(document.getString("profilePicturePath"))
                                        .into(profile_photo);
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
                                profile_rating.setText(total+ "% ("+String.format("%.0f", sum)+" votes)");
                            }
                        }
                    });
        }
        redirect_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ProfileActivity.this, ViewItemsActivity.class);
                go.putExtra("loadUserItems", "OK");
                go.putExtra("userName", profile_name.getText().toString());
                go.putExtra("userID", userID);
                startActivity(go);
            }
        });
    }
}
