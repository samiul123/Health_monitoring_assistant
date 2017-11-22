package com.example.samiu.health_monitoring_assistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class LogInActivity extends Activity {

    EditText logInEmail, logInPass;
    Button logIn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private ProgressDialog mDialogue;
    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInButton mGoogleBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_log_in);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mDialogue = new ProgressDialog(this);
        logInEmail = findViewById(R.id.logInEmailID);
        logInEmail.setPadding(45, 0, 0, 0);
        logInPass = findViewById(R.id.logInPassID);
        logInPass.setPadding(45, 0, 0, 0);

        mGoogleBtn = findViewById(R.id.googleBtn);
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        logIn = findViewById(R.id.logInBtnID);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogIn();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            mDialogue.setMessage("Starting sign in...");
            mDialogue.show();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                mDialogue.dismiss();
                Log.w("Failed", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("authWithGoogle", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("success", "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                            mDialogue.dismiss();
                            checkUserExist();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("failure", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            mDialogue.dismiss();
                        }

                        // ...
                    }
                });
    }

    private void checkLogIn() {
        String email = logInEmail.getText().toString().trim();
        String pass = logInPass.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){
            mDialogue.setMessage("Checking log in....");
            mDialogue.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mDialogue.dismiss();
                        checkUserExist();
                    }
                    else{
                        mDialogue.dismiss();
                        Toast.makeText(getApplicationContext(), "Error log in", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkUserExist() {
        if(mAuth.getCurrentUser() != null){
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(user_id)){
                        Intent logInIntent = new Intent(LogInActivity.this, MainActivity.class);
                        logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(logInIntent);
                    }
                    else{
                        Intent setUpIntent = new Intent(LogInActivity.this, SetUpActivity.class);
                        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setUpIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
