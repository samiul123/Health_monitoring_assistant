package com.example.samiu.health_monitoring_assistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends Activity {

    EditText name,email,pass,type, mobileNo, ownMobileNo;
    Button signUp;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialogue;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    ImageButton imageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.editNameID);
        email = findViewById(R.id.editEmailID);
        pass = findViewById(R.id.editPassID);
        mDialogue = new ProgressDialog(this);
        signUp = findViewById(R.id.signUpBtnID);
        type = findViewById(R.id.userTypeId);
        mobileNo = findViewById(R.id.relativeMobileNo);
        ownMobileNo = findViewById(R.id.ownMobileNo);
//        imageButton = findViewById(R.id.imageButtonID);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        System.out.println("in");
        final String nameText = name.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passText = pass.getText().toString().trim();
        final String typeText = type.getText().toString().trim();
        final String mobileNoRel = mobileNo.getText().toString().trim();
        final String ownMobile = ownMobileNo.getText().toString().trim();
        if(!TextUtils.isEmpty(nameText) && !TextUtils.isEmpty(emailText) && !TextUtils.isEmpty(passText)
                && !TextUtils.isEmpty(typeText) && !TextUtils.isEmpty(mobileNoRel)
                && !TextUtils.isEmpty(ownMobile)){
            mDialogue.setMessage("Signing up...");
            mDialogue.show();
            mAuth.createUserWithEmailAndPassword(emailText, passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        System.out.println("in");
                        /*String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = mDatabase.child(user_id);
                        current_user_db.child("name").setValue(nameText);
                        current_user_db.child("image").setValue("default");*/
                        mDialogue.dismiss();
                        Intent mainIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                        mainIntent.putExtra("type", typeText);
                        mainIntent.putExtra("mobileNo", mobileNoRel);
                        mainIntent.putExtra("token", FirebaseInstanceId.getInstance().getToken());
                        mainIntent.putExtra("ownMobile", ownMobile);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                    else{
                        mDialogue.dismiss();
                        Toast.makeText(getApplicationContext(), "Error while registering", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
