package shantanu.seeadoc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class DoctorViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "PatientViewHolder";
    View view;
    ImageView bProfilePic;

    FirebaseAuth mAuth;

    public DoctorViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        bProfilePic = (ImageView) view.findViewById(R.id.profilePic);

        mAuth = FirebaseAuth.getInstance();
    }

    public void setName(String name) {
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.setText(name);
        Log.i(TAG, "setName: " + name);
    }

    public void setSpecialization(String specialization) {
        TextView tvSpecialization = (TextView) view.findViewById(R.id.tvSpecialization);
        tvSpecialization.setText(specialization);
        Log.i(TAG, "setSpecialization: " + specialization);
    }

    public void setProfilePic(final Context context, final String profilePicUrl) {
        final ImageView profilePic = (ImageView) view.findViewById(R.id.profilePic);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        Picasso.with(context).load(profilePicUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profilePic,
                new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(profilePicUrl).into(profilePic);
                        progressBar.setVisibility(View.GONE);

                    }
                });
    }

}