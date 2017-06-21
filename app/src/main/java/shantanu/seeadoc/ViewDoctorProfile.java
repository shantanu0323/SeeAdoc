package shantanu.seeadoc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.UUID;

public class ViewDoctorProfile extends AppCompatActivity {

    private static final String TAG = "ViewDoctorProfile";
    private static final int TAKE_PICTURE = 20;

    private String doctorUid;
    private String patientUid;
    private boolean appointmentBooked;

    private Button bBookAppointment;
    private DatabaseReference databaseDoctors;
    private DatabaseReference databasePatients;
    private DatabaseReference databaseAppointments;
    private DatabaseReference databaseBookedAppointments;

    private boolean bookAppointmentClicked = false;
    private Object patientDetails;
    private ProgressDialog progressDialog;
    private TextView tvRejected;
    private DatabaseReference databaseRejectedPatients;
    private boolean appointmentRejected;
    private ValueEventListener appointmentRejectedListener;
    private boolean rejectedChecked = false;
    private ValueEventListener appointmentStatusListener;
    private FloatingActionButton bSendReports;
    private Uri imageUri;
    private ValueEventListener retrievePatientDetails;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Explode explode = new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_view_doctor_profile);

        init();

        bSendReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send Report
                takePhoto();
            }
        });

        bBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentClicked = true;
                if (appointmentRejected) {
                    databaseRejectedPatients.child(doctorUid).child(patientUid).removeValue();
                }
                databaseRejectedPatients.addValueEventListener(appointmentRejectedListener);
                tvRejected.setVisibility(View.GONE);
                databaseAppointments.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (bookAppointmentClicked) {
                            if (dataSnapshot.child(doctorUid).hasChild(patientUid)) {
                                // Cancelling Appointment
                                Log.i(TAG, "onDataChange: databaseAppointments : Cancelling appointment");
                                databaseAppointments.child(doctorUid).child(patientUid).removeValue();
                                Toast.makeText(ViewDoctorProfile.this, "Appointment Request Cancelled",
                                        Toast.LENGTH_SHORT).show();
                                bookAppointmentClicked = false;
                            } else {
                                // Booking Appointment
                                Log.i(TAG, "onDataChange: databaseAppointments : Booking Appointment");
                                databaseAppointments.child(doctorUid).child(patientUid).setValue(patientDetails);
                                Toast.makeText(ViewDoctorProfile.this, "Appointment Request Sent successfully",
                                        Toast.LENGTH_SHORT).show();
                                bookAppointmentClicked = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    Intent intent = new Intent(getApplicationContext(), SendReports.class);
                    intent.putExtra("imageUri", selectedImage.toString());
                    intent.putExtra("doctorUid", doctorUid);
                    intent.putExtra("patientUid", patientUid);
                    startActivity(intent);
                    // start picker to get image for cropping and then use the image in cropping activity
//                    CropImage.activity(selectedImage)
//                            .setGuidelines(CropImageView.Guidelines.ON)
//                            .start(this);
                }
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Log.i(TAG, "onActivityResult: AFTER CROP");
                    if (resultCode == RESULT_OK) {
                        Log.i(TAG, "onActivityResult: CROPPING SUCCESSFULL");
                        Uri resultUri = result.getUri();
                        imageUri = resultUri;
                        Intent intent = new Intent(getApplicationContext(), SendReports.class);
                        intent.putExtra("imageUri", imageUri.toString());
                        startActivity(intent);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        Toast.makeText(this, "CROPPING UNSUCCESSFULL : " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.i(TAG, "onActivityResult: requestCode : " + resultCode);
                }
        }
    }

    private void init() {
        profilePic = (ImageView) findViewById(R.id.profilepic);
        doctorUid = getIntent().getStringExtra("doctorUid");
        patientUid = getIntent().getStringExtra("patientUid");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:530266078999:android:481c4ecf3253701e") // Required for Analytics.
                .setApiKey("AIzaSyBRxOyIj5dJkKgAVPXRLYFkdZwh2Xxq51k") // Required for Auth.
                .setDatabaseUrl("https://docmate-d670e.firebaseio.com/") // Required for RTDB.
                .build();
        String doctorApp = UUID.randomUUID().toString();
        FirebaseApp.initializeApp(this, options, doctorApp);
        // Retrieve my other app.
        FirebaseApp doctorAppliction = FirebaseApp.getInstance(doctorApp);
// Get the database for the other app.
        FirebaseDatabase doctorDatabase = FirebaseDatabase.getInstance(doctorAppliction);

        databaseDoctors = doctorDatabase.getReference().child("doctor");
        databaseDoctors.keepSynced(true);

        databasePatients = FirebaseDatabase.getInstance().getReference().child("patient");
        databasePatients.keepSynced(true);

        databaseAppointments = doctorDatabase.getReference().child("appointments");
        databaseAppointments.keepSynced(true);

        databaseBookedAppointments = doctorDatabase.getReference().child("doctor").child(doctorUid).child("patients");
        databaseBookedAppointments.keepSynced(true);

        databaseRejectedPatients = FirebaseDatabase.getInstance().getReference().child("rejected");
        databaseRejectedPatients.keepSynced(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Status...");
        progressDialog.show();

        bBookAppointment = (Button) findViewById(R.id.bBookAppointment);
        tvRejected = (TextView) findViewById(R.id.tvRejected);
        bSendReports = (FloatingActionButton) findViewById(R.id.bSendReports);

        appointmentRejectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: appointmentRejectedListener CALLED");
                if (dataSnapshot.child(doctorUid).hasChild(patientUid)) {
                    Log.i(TAG, "onDataChange: Rejected has child");
                    appointmentRejected = true;
                } else {
                    Log.i(TAG, "onDataChange: Rejected has NO child");
                    appointmentRejected = false;
                }
                rejectedChecked = true;
                databaseAppointments.child(doctorUid).addValueEventListener(appointmentStatusListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
//
        retrievePatientDetails = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(patientUid)) {
                    patientDetails = dataSnapshot.child(patientUid).getValue();
                    Log.i(TAG, "onDataChange: Retrieving patient details...");
                }
                databaseRejectedPatients.addValueEventListener(appointmentRejectedListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseBookedAppointments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(patientUid)) {
                    Log.i(TAG, "onDataChange: " + dataSnapshot.getValue());
                    appointmentBooked = true;
                    Log.i(TAG, "onDataChange: APPOINTMENT BOOKED");
                } else {
                    Log.i(TAG, "onDataChange: " + dataSnapshot.getValue());
                    appointmentBooked = false;
                    Log.i(TAG, "onDataChange: APPOINTMENT NOT BOOKED");
                }
                databasePatients.addValueEventListener(retrievePatientDetails);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        appointmentStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: appointmentStatusListener CALLED");
                databaseRejectedPatients.removeEventListener(appointmentRejectedListener);
                progressDialog.dismiss();
                if (rejectedChecked) {
                    if (dataSnapshot.hasChild(patientUid)) {
                        Log.i(TAG, "onDataChange: Appointment Pending");
                        bBookAppointment.setText("Cancel the Appointment");
                        bBookAppointment.setBackground(getResources().getDrawable(R.drawable.bg_ripple_cancel));
                    } else {
                        if (appointmentBooked) {
                            // Appointment accepted
                            Log.i(TAG, "onDataChange: Accepted");
                            bBookAppointment.setText("Appointment Confirmed");
                            bBookAppointment.setBackground(getResources().getDrawable(R.drawable.bg_blank));
                            bBookAppointment.setTextColor(Color.rgb(0, 220, 200));
                            bBookAppointment.setClickable(false);
                            bSendReports.setVisibility(View.VISIBLE);
                        } else {
                            if (appointmentRejected) {
                                // Appointment rejected
                                Log.i(TAG, "onDataChange: Rejected");
                                tvRejected.setVisibility(View.VISIBLE);
                                bBookAppointment.setText("Try Booking an Appointment again");
                                bBookAppointment.setBackground(getResources().getDrawable(R.drawable.bg_ripple_book));
                                databaseRejectedPatients.removeEventListener(appointmentRejectedListener);

                            } else {
                                // Appointment Cancelled or not booked by patient
                                Log.i(TAG, "onDataChange: Cancelled or not booked");
                                tvRejected.setVisibility(View.GONE);
                                bBookAppointment.setText("Book an Appointment");
                                bBookAppointment.setBackground(getResources().getDrawable(R.drawable.bg_ripple_book));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }
}
