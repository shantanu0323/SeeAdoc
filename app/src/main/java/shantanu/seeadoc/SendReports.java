package shantanu.seeadoc;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class SendReports extends AppCompatActivity {

    private static final String TAG = "SendReports";

    private ImageView ivReports;
    private ProgressDialog progressDialog;

    private String doctorUid;
    private String patientUid;
    private DatabaseReference databaseReports;
    private Uri reportImageUri;
    private StorageReference storageReports;
    private String reportKey;
    private EditText etMessage;
    private DatabaseReference databasePatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_reports);

        init();
        initializeDocmate();

        ivReports = (ImageView) findViewById(R.id.ivReport);
        Log.i(TAG, "onCreate: reportImageUri : " + reportImageUri);
        ivReports.setImageURI(reportImageUri);

    }

    private void initializeDocmate() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:530266078999:android:481c4ecf3253701e") // Required for Analytics.
                .setApiKey("AIzaSyBRxOyIj5dJkKgAVPXRLYFkdZwh2Xxq51k") // Required for Auth.
                .setDatabaseUrl("https://docmate-d670e.firebaseio.com/")// Required for RTDB.
//                .setStorageBucket("gs://docmate-d670e.appspot.com/")
                .build();
        String doctorApp = UUID.randomUUID().toString();
        try {
            FirebaseApp doctorApplication = FirebaseApp.initializeApp(this, options, doctorApp);
            // Retrieve my other app.
//            FirebaseApp doctorApplication = FirebaseApp.getInstance(doctorApp);
//             Get the database for the other app.
            FirebaseDatabase doctorDatabase = FirebaseDatabase.getInstance(doctorApplication);

            databaseReports = doctorDatabase.getReference().child("doctor").child(doctorUid).child("reports");
            databaseReports.keepSynced(true);

            databasePatient = doctorDatabase.getReference().child("doctor").child(doctorUid).child("patients").child(patientUid);
            databasePatient.keepSynced(true);

            FirebaseStorage reportsStorage = FirebaseStorage.getInstance(doctorApplication);

            storageReports = reportsStorage.getReferenceFromUrl("gs://docmate-d670e.appspot.com/");

            Log.i(TAG, "initializeDocmate: strorageReports : " + ((storageReports == null) ? "null" : "NOT null"));
        } catch (Exception e) {
            Log.e(TAG, "initializeDocmate: doctorApplication : ", e);
        }

    }

    private void uploadReport() {
        Log.e(TAG, "uploadReport: FUNCTION STARTED");
        progressDialog.show();
        reportKey = databaseReports.push().getKey();
        if (reportImageUri != null) {
            Log.e(TAG, "uploadReport: URI NOT NULL");
            Log.i(TAG, "uploadReport: strorageReports : " + ((storageReports == null) ? "null" : "NOT null"));
            StorageReference filePath = storageReports.child("Reports").child(reportImageUri
                    .getLastPathSegment());
            filePath.putFile(reportImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    DatabaseReference newReport = databaseReports.child(reportKey);
                    newReport.child("image").setValue(downloadUrl.toString());
                    newReport.child("message").setValue(etMessage.getText().toString().trim());

                    newReport = databasePatient.child("reports").child(reportKey);
                    newReport.child("image").setValue(downloadUrl.toString());
                    newReport.child("message").setValue(etMessage.getText().toString().trim());

                    Log.e(TAG, "onSuccess: Report Image Added ...");
                    Toast.makeText(SendReports.this, "Report Sent Successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to upload report due to : " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: FAILED TO upload report");
                    progressDialog.dismiss();
                }
            });
        } else {
            Log.e(TAG, "uploadReport: REPORT IMAGE URI NULL");
            progressDialog.dismiss();
        }
    }

    private void init() {
        doctorUid = getIntent().getStringExtra("doctorUid");
        patientUid = getIntent().getStringExtra("patientUid");
        reportImageUri = Uri.parse(getIntent().getStringExtra("imageUri"));

        etMessage = (EditText) findViewById(R.id.etMessage);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Report...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_reports, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.send_report:
                try {
                    uploadReport();
                } catch (Exception e) {
                    Log.e(TAG, "onOptionsItemSelected: uploadReport : ", e);
                }
                break;
            case R.id.discard_report:
                finish();
                break;
        }

        return true;

    }

}
