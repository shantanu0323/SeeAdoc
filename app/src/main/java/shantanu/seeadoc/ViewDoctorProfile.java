package shantanu.seeadoc;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class ViewDoctorProfile extends AppCompatActivity {

    private static final String TAG = "ViewDoctorProfile";

    private String doctorUid;
    private String patientUid;
    private boolean appointmentBooked;

    private Button bBookAppointment;
    private DatabaseReference databaseDoctors;
    private DatabaseReference databasePatients;
    private DatabaseReference databaseAppointments;
    private boolean bookAppointmentClicked = false;
    private Object patientDetails;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Explode explode = new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_view_doctor_profile);

        init();

        databaseAppointments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (dataSnapshot.hasChild(patientUid)) {
                    bBookAppointment.setText("Cancel the Appointment");
                    bBookAppointment.setBackground(getResources().getDrawable(R.drawable.bg_ripple_cancel));
                } else {
                    bBookAppointment.setText("Book an Appointment");
                    bBookAppointment.setBackground(getResources().getDrawable(R.drawable.bg_ripple_book));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        bBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentClicked = true;

                databaseAppointments.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (bookAppointmentClicked) {
                            if (dataSnapshot.hasChild(patientUid)) {
                                // Cancelling Appointment
                                Log.i(TAG, "onDataChange: databaseAppointments : Cancelling appointment");
                                databaseAppointments.child(patientUid).removeValue();
                                Toast.makeText(ViewDoctorProfile.this, "Appointment Request Cancelled",
                                        Toast.LENGTH_SHORT).show();
                                bookAppointmentClicked = false;
                            } else {
                                // Booking Appointment
                                Log.i(TAG, "onDataChange: databaseAppointments : Booking Appointment");
                                databasePatients.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(patientUid)) {
                                            patientDetails = dataSnapshot.child(patientUid).getValue();
                                            Log.i(TAG, "onDataChange: databasePatients");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                databaseAppointments.child(patientUid).setValue(patientDetails);
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

    private void init() {
        doctorUid = getIntent().getStringExtra("doctorUid");
        patientUid = getIntent().getStringExtra("patientUid");
        Log.i(TAG, "init: doctorUid : " + doctorUid);
        Log.i(TAG, "init: patientUid : " + patientUid);

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

        databaseAppointments = databaseDoctors.child(doctorUid).child("appointments");
        databaseAppointments.keepSynced(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Status...");
        progressDialog.show();

        bBookAppointment = (Button) findViewById(R.id.bBookAppointment);
    }
}
