package com.mis.palittayo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMSActivity extends AppCompatActivity {
    String phone, message;
    EditText messageContent;
    Button sendMessage, goBack_text;
    TextView infoText;
    private final static int SEND_SMS_PERMISSION_REQUEST_CODE = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        messageContent = findViewById(R.id.messageContent);
        sendMessage = findViewById(R.id.sendButton);
        goBack_text = findViewById(R.id.goback_text);
        infoText = findViewById(R.id.infoText);

        if(getIntent().hasExtra("phone")) {
            phone = getIntent().getExtras().getString("phone");
        }
        infoText.setText("You are sending a text message to: "+phone);
        if(getIntent().hasExtra("message")) {
            message = getIntent().getExtras().getString("message");
            messageContent.setText(message);
        }

        if (checkPermission(Manifest.permission.SEND_SMS)){
            sendMessage.setEnabled(true);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(messageContent.getText().toString()) && !TextUtils.isEmpty(phone)){
                    if(checkPermission(Manifest.permission.SEND_SMS)){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone, null, messageContent.getText().toString(), null, null);
                        Toast.makeText(SMSActivity.this, "SMS will be delivered shortly! Make sure you have a load balance.", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                    else{
                        Toast.makeText(SMSActivity.this, "You have declined the permission to send message!", Toast.LENGTH_SHORT).show();
                    }
                }
                else { Toast.makeText(SMSActivity.this, "Empty Fields! Might the number is not correct.", Toast.LENGTH_SHORT).show(); }
            }
        });
        goBack_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case SEND_SMS_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    sendMessage.setEnabled(true);
                }
                break;
        }
    }
}
