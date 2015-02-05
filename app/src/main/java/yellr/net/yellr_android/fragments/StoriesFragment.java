package yellr.net.yellr_android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import yellr.net.yellr_android.R;
import yellr.net.yellr_android.activities.HomeActivity;
import yellr.net.yellr_android.intent_services.IntentServicesHelper;
import yellr.net.yellr_android.intent_services.stories.Story;
import yellr.net.yellr_android.intent_services.stories.StoriesIntentService;
import yellr.net.yellr_android.intent_services.stories.StoriesResponse;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StoriesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StoriesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Story> stories;
    //private StoriesArrayAdapter storiesArrayAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StoriesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StoriesFragment newInstance(String param1, String param2) {
        StoriesFragment fragment = new StoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public StoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // init new stories receiver
        Context context = getActivity().getApplicationContext();
        IntentFilter storiesFilter = new IntentFilter(StoriesReceiver.ACTION_NEW_STORIES);
        storiesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        StoriesReceiver storiesReceiver = new StoriesReceiver();
        context.registerReceiver(storiesReceiver, storiesFilter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stories, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {

        // get clientId
        SharedPreferences sharedPref = getActivity().getSharedPreferences("clientId", Context.MODE_PRIVATE);
        String clientId = sharedPref.getString("clientId", "");

        Log.d("StoriesFragment.onResume()", "Starting stories intent service ...");

        // init service
        Context context = getActivity().getApplicationContext();
        Intent storiesWebIntent = new Intent(context, StoriesIntentService.class);
        storiesWebIntent.putExtra(StoriesIntentService.PARAM_CLIENT_ID, clientId);
        storiesWebIntent.setAction(StoriesIntentService.ACTION_GET_STORIES);
        context.startService(storiesWebIntent);

        super.onResume();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class StoriesReceiver extends BroadcastReceiver {
        public static final String ACTION_NEW_STORIES =
                "yellr.net.yellr_android.action.NEW_STORIES";

        private ListView listView;

        public StoriesReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            listView = (ListView)getView().findViewById(R.id.storiesList);

            Log.d("StoriesReceiver.onReceive()", "onReceive called.");

            String storiesJson = intent.getStringExtra(StoriesIntentService.PARAM_STORIES_JSON);

            Log.d("StoriesReceiver.onReceive()", "JSON: " + storiesJson);

            Gson gson = new Gson();
            StoriesResponse response = gson.fromJson(storiesJson, StoriesResponse.class);

            if (response.success) {

                StoriesArrayAdapter storiesArrayAdapter = new StoriesArrayAdapter(getActivity(), new ArrayList<Story>());

                for (int i = 0; i < response.stories.length; i++) {
                    Story story = response.stories[i];
                    storiesArrayAdapter.add(story);
                }

                Log.d("StoriesReceiver.onReceive()", "Setting listView adapter ...");

                listView.setAdapter(storiesArrayAdapter);

            }

        }
    }

    class StoryListOnClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("neat");

        }

    }

    class StoriesArrayAdapter extends ArrayAdapter<Story> {

        private ArrayList<Story> stories;

        public StoriesArrayAdapter(Context context, ArrayList<Story> stories) {
            super(context, R.layout.fragment_story_row, R.id.frag_home_story_title, stories);
            this.stories = stories;

            Log.d("StoriesArrayAdapter.StoriesArrayAdapter()","Constructor.");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);

            Log.d("StoriesArrayAdapter.getView()","Setting values for view.");

            TextView textViewTitle = (TextView) row.findViewById(R.id.frag_home_story_title);
            TextView textViewPublishDateTime = (TextView) row.findViewById(R.id.frag_home_story_publish_datetime);

            textViewTitle.setText(this.stories.get(position).title);

            // TODO: format datetime pretty
            textViewPublishDateTime.setText("Published: " + this.stories.get(position).publish_datetime);


            return row;
        }

    }


}