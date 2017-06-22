package shantanu.seeadoc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class PrescriptionViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "PatientViewHolder";
    View view;

    public PrescriptionViewHolder(View itemView) {
        super(itemView);
        view = itemView;

    }

    public void setName(String name) {
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.setText(name + " says :");
        Log.i(TAG, "setName: " + name);
    }

    public void setMessage(String message) {
        TextView tvAge = (TextView) view.findViewById(R.id.tvMessage);
        tvAge.setText("\"" + message + "\"");
        Log.i(TAG, "setAge: " + message);
    }

    public void setProfilepic(final Context context, final String profilePicUrl, int width) {
        final ImageView profilePic = (ImageView) view.findViewById(R.id.profilepic);

        (new AQuery(context)).id(profilePic).image(profilePicUrl, true, true, width, R.drawable.default_image);
    }

    public void setImage(final Context context, final String imageUrl, int width) {
        final ImageView image = (ImageView) view.findViewById(R.id.image);

        (new AQuery(context)).id(image).progress(R.id.progressBar).image(imageUrl, true, true, width, R.drawable.default_prescription);
    }

}