package com.mis.palittayo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private final static int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(auth.getCurrentUser() != null){
            Toast.makeText(SplashScreen.this, "Welcome back, "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "!", Toast.LENGTH_SHORT).show();
            Intent go = new Intent(SplashScreen.this, ViewItemsActivity.class);
            go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(go);

        }
        else{
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTheme(R.style.SplashTheme)
                    .build(), RC_SIGN_IN);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final ProgressDialog progressDialog = new ProgressDialog(SplashScreen.this);
                progressDialog.setMessage("Login Success! Getting data...");
                progressDialog.setTitle("Please Wait...");
                progressDialog.show();
                final FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                FireInstance.collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.getString("Positive") != null){
                                        progressDialog.dismiss();
                                        Toast.makeText(SplashScreen.this, "Welcome back, "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                                        Intent go = new Intent(SplashScreen.this, ViewItemsActivity.class);
                                        go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(go);
                                    }
                                    else{
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("Name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                        user.put("Positive", "0");
                                        user.put("Negative", "0");
                                        user.put("profilePicturePath", "");
                                        FireInstance.collection("users")
                                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SplashScreen.this, "Successfully Logged As: "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                                                        Intent go = new Intent(SplashScreen.this, EditProfileActivity.class);
                                                        go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(go);
                                                    }
                                                });
                                    }

                                }

                            }
                        });


            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                Toast.makeText(this, "Error Code: "+response.getError().getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
