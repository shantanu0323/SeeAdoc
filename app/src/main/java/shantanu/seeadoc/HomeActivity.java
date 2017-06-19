package shantanu.seeadoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import shantanu.seeadoc.Data.Doctor;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";
    private static ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseDoctors;
    private RecyclerView doctorList;
    private boolean flag = true;
    private boolean loadingDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Explode explode = new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);

        setContentView(R.layout.activity_home);

        init();

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void init() {
        doctorList = (RecyclerView) findViewById(R.id.doctorList);
        doctorList.setHasFixedSize(true);
        doctorList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_background));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        auth = FirebaseAuth.getInstance();
        Log.i(TAG, "init: auth == null : " + (auth == null));

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Log.e(TAG, "onAuthStateChanged: NO USER LOGGED IN");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(HomeActivity.this, null).toBundle());
                    finish();
                }
            }
        };
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        checkUserExist();
        if (auth != null) {
            auth.addAuthStateListener(authStateListener);
        }
        if (!loadingDone) {
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }
        FirebaseRecyclerAdapter<Doctor, DoctorViewHolder> adapter = new FirebaseRecyclerAdapter<Doctor, DoctorViewHolder>(
                Doctor.class,
                R.layout.row_doctor,
                DoctorViewHolder.class,
                databaseDoctors
        ) {
            @Override
            protected void populateViewHolder(final DoctorViewHolder viewHolder, final Doctor model, final int position) {
                Log.i(TAG, "populateViewHolder: Started");
                final String doctorKey = getRef(position).getKey().toString();


                Log.i(TAG, "populateViewHolder: Name : " + model.getName());
                Log.i(TAG, "populateViewHolder: Specialization : " + model.getSpecialization());
                viewHolder.setName(model.getName());
                viewHolder.setSpecialization(model.getSpecialization());

                databaseDoctors.child(model.getUid()).child("profilepic")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    Log.i(TAG, "onDataChange: profilePic");
                                    viewHolder.setProfilePic(getApplicationContext(), dataSnapshot
                                            .getValue().toString());
                                    if (flag) {
                                        progressDialog.dismiss();
                                        loadingDone = true;
                                        flag = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                // Adding OnClickListener() to the entire Card
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(HomeActivity.this, "Doctor : " + doctorKey, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ViewDoctorProfile.class);
                        intent.putExtra("doctorUid", doctorKey);
                        intent.putExtra("patientUid", auth.getCurrentUser().getUid());
                        startActivity(intent);
                    }
                });


            }
        };

        adapter.notifyDataSetChanged();
        doctorList.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (auth != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_settings:

                break;
            case R.id.action_logout:
                auth.signOut();
                break;
        }

        return true;

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
