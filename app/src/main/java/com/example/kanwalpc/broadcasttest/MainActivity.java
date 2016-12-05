package com.example.kanwalpc.broadcasttest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    String phone_number_sender, message_sender;
    private static final int REQUEST_CODE=007;
    private static String TAG_APP="Error";
    private EditText senderPhoneNum, message_et;
    private Button send_btn;
    private TextView display_messages;
    private static String messages="";
    Handler mHandler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senderPhoneNum = (EditText) this.findViewById(R.id.sender_phone_et);
        message_et = (EditText) this.findViewById(R.id.send_message_et);
        send_btn = (Button) this.findViewById(R.id.send_message_btn);
        display_messages = (TextView) this.findViewById(R.id.display_messages);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(5000);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                display_messages.setText(messages);
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.i(TAG_APP, e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void send_message(View view) {
        phone_number_sender=senderPhoneNum.getText().toString().trim();
        message_sender=message_et.getText().toString();

        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE);
            } else {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone_number_sender, null, message_sender, null, null);
                Toast.makeText(MainActivity.this, "Send", Toast.LENGTH_SHORT).show();
            }
        }catch (IllegalArgumentException ex){
            Log.i(TAG_APP, ex.toString());
        }
        messages=messages+ "YOU :"+message_sender+"\n";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults!=null && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone_number_sender, null, message_sender, null, null);
                    Toast.makeText(MainActivity.this, "Send", Toast.LENGTH_SHORT).show();
                }
                    break;
        }
    }

    public static class SmsReceiver extends BroadcastReceiver{
        SmsManager smsManager=SmsManager.getDefault();

        public SmsReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle=intent.getExtras();

            try{
               Object[] pduObject=(Object[]) bundle.get("pdus");

                for(int i=0; i< pduObject.length; i++){
                    SmsMessage smsMessage=SmsMessage.createFromPdu((byte[]) pduObject[i]);
                    String receiver_num=smsMessage.getDisplayOriginatingAddress();
                    String receiver_msg=smsMessage.getDisplayMessageBody();

                    messages= messages+ receiver_num +" : "+receiver_msg+"\n";
                }
            }catch (Exception e){
                Log.i(TAG_APP, e.toString());
            }

        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
