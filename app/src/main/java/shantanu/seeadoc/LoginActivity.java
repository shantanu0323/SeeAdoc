package shantanu.seeadoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail;
    private EditText etPassword;
    private Button bLogin;
    private Button bRegister;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Explode explode = new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_login);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_background));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_login);

        auth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        bRegister = (Button) findViewById(R.id.bRegister);
        progressDialog = new ProgressDialog(this);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: REDIRECTING TO RegisterActivity...");
                Intent intent = new Intent(getApplicationContext(), PatientRegistration.class);
                startActivity(intent, ActivityOptionsCompat
                        .makeSceneTransitionAnimation(LoginActivity.this, null).toBundle());
            }
        });
    }

    private void checkLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(email) &&
                !TextUtils.isEmpty(password)) {

            progressDialog.setMessage("Verifying Login...");
            progressDialog.show();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
//                                checkUserExist();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity
                                        .class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent, ActivityOptionsCompat
                                        .makeSceneTransitionAnimation(LoginActivity.this, null)
                                        .toBundle());
                            } else {
                                Toast.makeText(LoginActivity.this, "Error Logging in : " + task.getException().getMessage(), Toast
                                        .LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }
    }

}
