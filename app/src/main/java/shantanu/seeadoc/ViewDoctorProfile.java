package shantanu.seeadoc;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Explode explode = new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_view_doctor_profile);

        init();

        databaseBookedAppointments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(patientUid)) {
                    appointmentBooked = true;
                } else {
                    appointmentBooked = false;
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

        databaseAppointments = doctorDatabase.getReference().child("appointments");
        databaseAppointments.keepSynced(true);

        databaseBookedAppointments = doctorDatabase.getReference().child("doctor").child("patients");
        databaseBookedAppointments.keepSynced(true);

        databaseRejectedPatients = FirebaseDatabase.getInstance().getReference().child("rejected");
        databaseRejectedPatients.keepSynced(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Status...");
        progressDialog.show();

        bBookAppointment = (Button) findViewById(R.id.bBookAppointment);
        tvRejected = (TextView) findViewById(R.id.tvRejected);

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

        appointmentStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                            bBookAppointment.setTextColor(Color.rgb(0, 200, 200));
                            bBookAppointment.setClickable(false);
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

        appointmentRejectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
        databaseRejectedPatients.addValueEventListener(appointmentRejectedListener);
    }
}
