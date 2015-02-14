package yellr.net.yellr_android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;


import yellr.net.yellr_android.R;
import yellr.net.yellr_android.intent_services.profile.ProfileIntentService;
import yellr.net.yellr_android.intent_services.profile.ProfileResponse;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private String clientId;
    private TextView userImage;
    private TextView userName;
    private TextView userUUID;
    private TextView userVerifiedIcon;
    private TextView userVerified;
    private TextView postsIcon;
    private TextView postsNumber;
    private TextView postsViewedIcon;
    private TextView postsViewedNumber;
    private TextView postsUsedIcon;
    private TextView postsUsedNumber;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        Log.d("ProfileFragment.onResume()", "Starting profile intent service ...");
        refreshProfileData();
        super.onResume();
    }

    private void refreshProfileData() {
        // init service
        Context context = getActivity().getApplicationContext();
        Intent profileWebIntent = new Intent(context, ProfileIntentService.class);
        profileWebIntent.putExtra(ProfileIntentService.PARAM_CLIENT_ID, clientId);
        profileWebIntent.setAction(ProfileIntentService.ACTION_GET_PROFILE);
        context.startService(profileWebIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        Log.d("ProfileFragment.onCreate()","Setting up IntentService receiver ...");

        // get clientId
        SharedPreferences sharedPref = getActivity().getSharedPreferences("clientId", Context.MODE_PRIVATE);
        clientId = sharedPref.getString("clientId", "");

        // init new profile receiver
        Context context = getActivity().getApplicationContext();
        IntentFilter profileFilter = new IntentFilter(ProfileReceiver.ACTION_NEW_PROFILE);
        profileFilter.addCategory(Intent.CATEGORY_DEFAULT);
        ProfileReceiver profileReceiver = new ProfileReceiver();
        context.registerReceiver(profileReceiver, profileFilter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf");

        userImage = (TextView) view.findViewById(R.id.frag_profile_user_image);
        userImage.setTypeface(font);
        userImage.setText(R.string.fa_user_secret);

        userName = (TextView) view.findViewById(R.id.frag_profile_name);
        userUUID = (TextView) view.findViewById(R.id.frag_profile_id);
        userUUID.setText(String.format("UUID: %s", clientId));

        userVerified = (TextView) view.findViewById(R.id.frag_profile_verified);
        userVerifiedIcon = (TextView)view.findViewById(R.id.frag_profile_verified_icon);
        userVerifiedIcon.setTypeface(font);

        postsIcon = (TextView)view.findViewById(R.id.frag_profile_posts_icon);
        postsIcon.setTypeface(font);
        postsNumber = (TextView)view.findViewById(R.id.frag_profile_posts_number);

        postsViewedIcon = (TextView)view.findViewById(R.id.frag_profile_viewed_posts_icon);
        postsViewedIcon.setTypeface(font);
        postsViewedNumber = (TextView)view.findViewById(R.id.frag_profile_viewed_posts_number);

        postsUsedIcon = (TextView)view.findViewById(R.id.frag_profile_used_posts_icon);
        postsUsedIcon.setTypeface(font);
        postsUsedNumber = (TextView)view.findViewById(R.id.frag_profile_used_posts_number);

        return view;
    }

    public class ProfileReceiver extends BroadcastReceiver {
        public static final String ACTION_NEW_PROFILE =
                "yellr.net.yellr_android.action.NEW_PROFILE";

        public ProfileReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String profileJson = intent.getStringExtra(ProfileIntentService.PARAM_PROFILE_JSON);

            Gson gson = new Gson();
            ProfileResponse response = gson.fromJson(profileJson, ProfileResponse.class);

            if ( response.success ) {
                if(!response.first_name.isEmpty()){
                    userName.setText(response.first_name + " " + response.last_name);
                    userImage.setText(R.string.fa_user);

                }

                if(response.verified){
                    userVerified.setText("Verified");
                    userVerifiedIcon.setText(R.string.fa_check_square);
                }
                postsNumber.setText(String.valueOf(response.post_count));
                postsViewedNumber.setText(String.valueOf(response.post_view_count));
                postsUsedNumber.setText(String.valueOf(response.post_used_count));
            /*
            {
                "first_name": "",
                "last_name": "",
                "verified": false,
                "success": true,
                "post_count": 1,
                "post_view_count": 0,
                "organization": "",
                "post_used_count": 0,
                "email": ""
            }
             */

            }

        }
    }
    
}
