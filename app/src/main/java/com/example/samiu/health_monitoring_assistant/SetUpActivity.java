package com.example.samiu.health_monitoring_assistant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetUpActivity extends AppCompatActivity {

    ImageButton imageButton;
    EditText nameText;
    Button finishSetupBtn;
    private Uri imageUri = null;
    public static int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,mDatabase2;
    private StorageReference mStorage;
    private ProgressDialog mDialogue;
    String type;
    String mobileNo,token, ownMobile, relativeDeviceToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDialogue = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_pics");
        imageButton = findViewById(R.id.imageButtonID);
        nameText = findViewById(R.id.nameID);
        finishSetupBtn = findViewById(R.id.setUpBtnID);

        finishSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        Intent received_intent = getIntent();
        type = received_intent.getStringExtra("type");
        mobileNo = received_intent.getStringExtra("mobileNo");
        token = received_intent.getStringExtra("token");
        ownMobile = received_intent.getStringExtra("ownMobile");



    }

    private void startSetupAccount() {
        final String display_name = nameText.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();
        if(!TextUtils.isEmpty(display_name) && imageUri != null){
            mDialogue.setMessage("Finishing setup...");
            mDialogue.show();

            System.out.println("relative mobile: " + mobileNo);
            System.out.println("own mobile: " + ownMobile);
            mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabase2.orderByChild("ownMobileNo").equalTo(mobileNo).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //relativeDeviceToken = (String) dataSnapshot.getValue();
                    for(DataSnapshot child: dataSnapshot.getChildren()){
                        for(DataSnapshot childOfChild: child.getChildren()){
                            if(childOfChild.getKey().equals("ownDeviceToken")){
                                relativeDeviceToken = (String) childOfChild.getValue();
                                System.out.println("RELATIVE" + relativeDeviceToken);
                            }
                        }
                        System.out.println(child.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            StorageReference filePath = mStorage.child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downLoadUri = taskSnapshot.getDownloadUrl().toString();
                    mDatabase.child(user_id).child("name").setValue(display_name);
                    mDatabase.child(user_id).child("image").setValue(downLoadUri);
                    mDatabase.child(user_id).child("type").setValue(type);
                    mDatabase.child(user_id).child("relativeMobileNo").setValue(mobileNo);
                    mDatabase.child(user_id).child("ownDeviceToken").setValue(token);
                    mDatabase.child(user_id).child("ownMobileNo").setValue(ownMobile);
                    mDatabase.child(user_id).child("relativeDeviceToken").setValue(relativeDeviceToken);
                    mDialogue.dismiss();
                    Intent mainIntent = new Intent(SetUpActivity.this, HomeActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageButton.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
