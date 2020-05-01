package com.example.collegeconnect.settingsactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.collegeconnect.R;
import com.example.collegeconnect.datamodels.SaveSharedPreference;
import com.example.collegeconnect.datamodels.User;
import com.example.collegeconnect.navigation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeEditActivity extends AppCompatActivity {

    TextDrawable drawable;
    TextView tv;
    EditText nameField, enrollNo, branch;
    ImageButton imageButton;
    CircleImageView prfileImage;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    Uri uri;
    private StorageReference storageRef;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private Uri filePath;
    FloatingActionButton submitDetails;
    private static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_edit);
        Toolbar toolbar = findViewById(R.id.toolbarcom);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        TextView tv = findViewById(R.id.tvtitle);
        tv.setText("Edit Details");
        storageRef = storage.getReference();
        storageRef.child("User/" + SaveSharedPreference.getUserName(this) + "/DP.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                HomeEditActivity.this.uri = uri;
                if (uri != null)
                    Picasso.get().load(uri).into(prfileImage);
//                progressBar.setVisibility(View.GONE);

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(getActivity(), "No DP!", Toast.LENGTH_SHORT).show();
//                progressBar.setVisibility(View.GONE);
            }

        });
        if (uri != null)
            Picasso.get().load(uri).into(prfileImage);
        int dot = SaveSharedPreference.getUserName(this).indexOf(".");
        databaseReference = firebaseDatabase.getReference("users/" + SaveSharedPreference.getUserName(this).substring(0, dot));
        prfileImage = findViewById(R.id.imageView3copy);
        nameField = findViewById(R.id.nameFieldcopy);
        enrollNo = findViewById(R.id.textView3copy);
        branch = findViewById(R.id.textView4copy);
        imageButton = findViewById(R.id.imageButtoncopy);
//        editDetails = view.findViewById(R.id.editDetailscopy);
        submitDetails = findViewById(R.id.submitDetailscopy);
//        totalAttendance = view.findViewById(R.id.aggregateAttendancecopy);
//        circleprog = view.findViewById(R.id.cicleprog);
//        totalAttendance.setEnabled(false);
        nameField.setEnabled(false);
        enrollNo.setEnabled(false);
        branch.setEnabled(false);
        imageButton.setEnabled(false);
//        circleprog.setMax(100);
//        circleprog.setProgress(0);
        submitDetails.setColorFilter(getResources().getColor(R.color.colorwhite));
        datachange();


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getprfpic();
            }
        });
        edit();


        submitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameField.getText().toString();
                String enroll = enrollNo.getText().toString();
                String clg = branch.getText().toString();
                User.addUser(enroll, firebaseAuth.getCurrentUser().getEmail(), name, null, clg);
                finish();
            }
        });
    }
    private void getprfpic() {
        if (ContextCompat.checkSelfPermission(HomeEditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(HomeEditActivity.this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    100);
        }
        else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select an image"), GET_FROM_GALLERY);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getprfpic();

        } else {
            Toast.makeText(this,
                    "Storage Permission Denied",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void edit(){
        nameField.setEnabled(true);
        enrollNo.setEnabled(true);
        branch.setEnabled(true);
        nameField.setTextColor(getColor(R.color.blackToWhite));
        enrollNo.setTextColor(getColor(R.color.blackToWhite));
        branch.setTextColor(getColor(R.color.blackToWhite));
        imageButton.setEnabled(true);
        imageButton.setVisibility(View.VISIBLE);
//        editDetails.setEnabled(false);
//        editDetails.setVisibility(View.GONE);
        submitDetails.setEnabled(true);
        submitDetails.setVisibility(View.VISIBLE);
    }

    private void datachange() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, Object> map= (Map<String,Object>)dataSnapshot.getValue();
                String name = (String) map.get("Name");
                String rollNo = (String) map.get("Username");
                String college = (String) map.get("Clgname");
                nameField.setText(name);
                enrollNo.setText(rollNo);
                branch.setText(college);
                try {
                    int space = name.indexOf(" ");
                    int color = navigation.generatecolor();
                    drawable = TextDrawable.builder().beginConfig()
                            .width(150)
                            .height(150)
                            .bold()
                            .endConfig()
                            .buildRound(name.substring(0, 1) + name.substring(space + 1, space + 2), color);
                    prfileImage.setImageDrawable(drawable);
                }
                catch (Exception e){

                }
                if (uri!=null)
                    Picasso.get().load(uri).into(prfileImage);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_FROM_GALLERY && resultCode == this.RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            CropImage.activity(filePath).setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == this.RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    prfileImage.setImageBitmap(bitmap);
                    uploadImage(resultUri);
                }
                catch (Exception e){
                    Log.d("Home fragment", "onActivityResult: CropImage failed");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(Uri resultUri)
    {
        if (resultUri!=null){
//            progressBar.setVisibility(View.VISIBLE);
            StorageReference unique = storageRef.child("User/");
            final StorageReference timeTableref = unique.child( SaveSharedPreference.getUserName(this)+"/DP.jpeg");
            timeTableref.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    progressBar.setVisibility(View.GONE);

//                    Toast.makeText(getActivity(), "DP updated!", Toast.LENGTH_SHORT).show();

//                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
////                        Toast.makeText(TimeTable.this, uri.toString(), Toast.LENGTH_SHORT).show();
//
//                        int dot = SaveSharedPreference.getUserName(getContext()).indexOf(".");
//                        databaseReference.child(SaveSharedPreference.getUserName(getActivity()).substring(0,dot)).child("TimeTable").setValue(uri.toString());
//                        }
//                    });
                }
            }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {
//                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(HomeEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}