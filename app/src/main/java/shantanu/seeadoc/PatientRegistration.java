package shantanu.seeadoc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import shantanu.seeadoc.Data.Patient;

public class PatientRegistration extends AppCompatActivity {

    private static final String TAG = "PatientRegistration";
    private static final String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/docmate-d670e.appspot.com/o/default_image.png?alt=media&token=5431adb3-2946-41e5-9b67-4cf07b4aaa78";
    private static final int GALLERY_REQUEST = 10;

    private ImageButton bAddImage;
    private RadioGroup rgGender;
    private EditText etName, etAge, etEmail, etDiseases, etPhone, etBloodGroup, etPassword;
    private TextInputLayout nameLabel, ageLabel, emailLabel, diseasesLabel, phoneLabel,
            bloodGroupLabel, passwordLabel;
    private Button bRegister;
    private Activity activity = this;
    private String gender = "Male";
    private Uri uri = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Explode explode = new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_patient_registration);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_background));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Patient patient = null;
                try {
                    patient = new Patient(DEFAULT_IMAGE_URL,
                            gender,
                            etName.getText().toString().trim(),
                            etAge.getText().toString().trim(),
                            etDiseases.getText().toString().trim(),
                            etPhone.getText().toString().trim(),
                            etBloodGroup.getText().toString().trim(),
                            etEmail.getText().toString().trim(),
                            etPassword.getText().toString().trim());
                } catch (Exception e) {
                    Log.e(TAG, "onClick: ", e);
                    Toast.makeText(PatientRegistration.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                if (isDataValid(patient)) {
                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                    firebaseHelper.registerPatient(patient);
                }
            }
        });

        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserImage();
            }
        });

        if (rgGender != null) {
            rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    switch (checkedId) {
                        case R.id.rbMale:
                            Log.i(TAG, "onCheckedChanged: Male");
                            gender = "Male";
                            break;
                        case R.id.rbFemale:
                            Log.i(TAG, "onCheckedChanged: Female");
                            gender = "Female";
                            break;
                    }
                }
            });
        } else {
            Log.i(TAG, "getGender: rgGender == null");
        }
    }

    private boolean isDataValid(Patient patient) {
        boolean dataValid = true;

        nameLabel.setErrorEnabled(false);
        emailLabel.setErrorEnabled(false);
        ageLabel.setErrorEnabled(false);
        diseasesLabel.setErrorEnabled(false);
        phoneLabel.setErrorEnabled(false);
        bloodGroupLabel.setErrorEnabled(false);
        passwordLabel.setErrorEnabled(false);

        if (TextUtils.isEmpty(patient.getName())) {
            nameLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        }
        if (TextUtils.isEmpty(patient.getEmail())) {
            emailLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!(patient.getEmail().contains("@") && !patient.getEmail().contains(" "))) {
            emailLabel.setError("Please enter a valid Email-ID");
            dataValid = false;
        }
        if (TextUtils.isEmpty(patient.getAge())) {
            ageLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!((Integer.parseInt(patient.getAge()) > 0) && (Integer.parseInt(patient.getAge()) < 130))) {
            bloodGroupLabel.setError("Please enter a valid blood group!!!");
            dataValid = false;
        }
        if (TextUtils.isEmpty(patient.getBloodGroup())) {
            bloodGroupLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!patient.getBloodGroup().matches("^(A|B|AB|O)[+-]?$")) {
            bloodGroupLabel.setError("Please enter a valid blood group!!!");
            dataValid = false;
        }
        if (TextUtils.isEmpty(patient.getPhone())) {
            phoneLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!(patient.getPhone().matches("[0-9]+") &&
                patient.getPhone().length() == 10)) {
            phoneLabel.setError("Please enter a valid Phone No");
            dataValid = false;
        }
        if (TextUtils.isEmpty(patient.getDiseases())) {
            diseasesLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        }
        if (TextUtils.isEmpty(patient.getPassword())) {
            passwordLabel.setError("Field cannot be empty!!!");
            dataValid = false;
        }

        return dataValid;
    }


    private void init() {
        bAddImage = (ImageButton) findViewById(R.id.bAddImage);
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etAge = (EditText) findViewById(R.id.etAge);
        etDiseases = (EditText) findViewById(R.id.etDiseases);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etBloodGroup = (EditText) findViewById(R.id.etBloodGroup);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bRegister = (Button) findViewById(R.id.bRegister);

        nameLabel = (TextInputLayout) findViewById(R.id.nameLabel);
        emailLabel = (TextInputLayout) findViewById(R.id.emailLabel);
        ageLabel = (TextInputLayout) findViewById(R.id.ageLabel);
        diseasesLabel = (TextInputLayout) findViewById(R.id.diseasesLabel);
        phoneLabel = (TextInputLayout) findViewById(R.id.phoneLabel);
        bloodGroupLabel = (TextInputLayout) findViewById(R.id.bloodGroupLabel);
        passwordLabel = (TextInputLayout) findViewById(R.id.passwordLabel);

        progressDialog = new ProgressDialog(this);
    }

    private void pickUserImage() {
        Log.e(TAG, "pickUserImage: FUNCTION STARTED");
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                uri = resultUri;
                bAddImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "CROPPING UNSUCCESSFULL : " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class FirebaseHelper {

        private static final String TAG = "FirebaseHelper";

        private FirebaseAuth auth;
        private DatabaseReference databasePatient;
        private StorageReference storageProfilePics;

        public FirebaseHelper() {
            auth = FirebaseAuth.getInstance();
            databasePatient = FirebaseDatabase.getInstance().getReference().child("patient");
            storageProfilePics = FirebaseStorage.getInstance().getReference();
        }

        public void registerPatient(Patient patient) {
            startRegister(patient);
        }

        private void startRegister(final Patient patient) {
            Log.e(TAG, "startRegister: REGISTERING USER...");
            progressDialog.setMessage("Registering User...");
            progressDialog.show();
            auth.createUserWithEmailAndPassword(patient.getEmail(), patient.getPassword())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.e(TAG, "onComplete: REGISTERING USER SUCCESSFULL");
                                String userId = auth.getCurrentUser().getUid();

                                uploadImage(patient.getProfilePic());
                                DatabaseReference currentUser = databasePatient.child(userId);

                                currentUser.child("name").setValue(patient.getName());
                                currentUser.child("email").setValue(patient.getEmail());
                                currentUser.child("age").setValue(patient.getAge());
                                currentUser.child("diseases").setValue(patient.getDiseases());
                                currentUser.child("gender").setValue(patient.getGender());
                                currentUser.child("phone").setValue(patient.getPhone());
                                currentUser.child("bloodgroup").setValue(patient.getBloodGroup());
                                progressDialog.dismiss();
                                Log.e(TAG, "onComplete: Redirecting to HomeActivity");
                                Toast.makeText(PatientRegistration.this, "Registration Successfull !!!",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity
                                        .class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: REGISTRATION FAILED due to : " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(PatientRegistration.this, "REGISTRATION FAILED because : " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        private void uploadImage(String profilePicURL) {
            Log.e(TAG, "uploadImage: FUNCTION STARTED");
            progressDialog.show();
            if (uri != null) {
                Log.e(TAG, "uploadImage: URI NOT NULL");
                StorageReference filePath = storageProfilePics.child("ProfilePics").child(uri
                        .getLastPathSegment());
                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        DatabaseReference newPatient = databasePatient.child(auth.getCurrentUser().getUid());
                        newPatient.child("profilepic").setValue(downloadUrl.toString());
                        Log.e(TAG, "onSuccess: Image Added ...");
                        progressDialog.dismiss();
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to finish setup due to : " +
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: FAILED TO FINISH SETUP");
                        progressDialog.dismiss();
                    }
                });
            } else {
                Log.e(TAG, "uploadImage: URI NULL");
                DatabaseReference newPatient = databasePatient.child(auth.getCurrentUser().getUid());
                newPatient.child("profilepic").setValue(profilePicURL);
                Log.e(TAG, "onSuccess: SETUP DONE ...");
                progressDialog.dismiss();
            }
        }

    }

}
