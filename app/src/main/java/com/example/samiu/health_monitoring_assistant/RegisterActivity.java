package com.example.samiu.health_monitoring_assistant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText name,email,pass;
    Button signUp;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialogue;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.editNameID);
        email = findViewById(R.id.editEmailID);
        pass = findViewById(R.id.editPassID);
        mDialogue = new ProgressDialog(this);
        signUp = findViewById(R.id.signUpBtnID);

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

        if(!TextUtils.isEmpty(nameText) && !TextUtils.isEmpty(emailText) && !TextUtils.isEmpty(passText)){
            mDialogue.setMessage("Signing up...");
            mDialogue.show();
            mAuth.createUserWithEmailAndPassword(emailText, passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        System.out.println("in");
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = mDatabase.child(user_id);
                        current_user_db.child("name").setValue(nameText);
                        current_user_db.child("image").setValue("default");
                        mDialogue.dismiss();
                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });
        }
    }
}
