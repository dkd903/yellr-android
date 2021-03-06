package yellr.net.yellr_android.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

import yellr.net.yellr_android.BuildConfig;
import yellr.net.yellr_android.R;
import yellr.net.yellr_android.activities.PostActivity;
import yellr.net.yellr_android.fragments.PostFragment;
import yellr.net.yellr_android.intent_services.assignments.Assignment;
import yellr.net.yellr_android.intent_services.assignments.AssignmentsResponse;

/**
 * Created by TDuffy on 2/7/2015.
 */
public class YellrUtils {

    public static boolean isFirstBoot(Context context) {

        boolean firstBoot = false;

        SharedPreferences sharedPref = context.getSharedPreferences("isFirstBootAppVersion", Context.MODE_PRIVATE);
        String isFirstBootAppVersion = sharedPref.getString("isFirstBootAppVersion", "");

        if (!isFirstBootAppVersion.equals(BuildConfig.VERSION_NAME)) {

            // set our return var
            firstBoot = true;

            // record our app version to the shared pref, so we return
            // false next time.
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("isFirstBootAppVersion", BuildConfig.VERSION_NAME);
            editor.commit();
        }

        return firstBoot;
    }

    public static boolean isFirstPost(Context context) {

        boolean firstBoot = false;

        SharedPreferences sharedPref = context.getSharedPreferences("isFirstPostAppVersion", Context.MODE_PRIVATE);
        String isFirstBootAppVersion = sharedPref.getString("isFirstPostAppVersion", "");

        if (!isFirstBootAppVersion.equals(BuildConfig.VERSION_NAME)) {

            // set our return var
            firstBoot = true;

            // record our app version to the shared pref, so we return
            // false next time.
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("isFirstPostAppVersion", BuildConfig.VERSION_NAME);
            editor.commit();
        }

        return firstBoot;
    }

    public static Date prettifyDateTime(String rawDateTime) {

        Date date = new Date();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = dateFormat.parse(rawDateTime);
        } catch (Exception e) {
            // todo: report
            Log.d("YellrUtils.PrettifyDateTime()", "Error: " + e.toString());
        }
        return date;
    }

    public static String calcTimeBetween(Date start, Date end) {

        int SECOND = 1000;
        int MINUTE = SECOND * 60;
        int HOUR = MINUTE * 60;
        int DAY = HOUR * 24;

        int milliSeconds = Math.round(end.getTime() - start.getTime());
        int t = 0;
        String retString = "";

        if (milliSeconds > DAY) {
            t = Math.round(milliSeconds / DAY);
            retString = String.format("%dd", t);
            //if (t > 1) {
            //    retString += "s";
            //}
            //return retString;
        } else if (milliSeconds < DAY && milliSeconds > HOUR) {
            t = Math.round(milliSeconds / HOUR);
            retString = String.format("%dh", t);
            //if (t > 1) {
            //    retString += "s";
            //}
            //return retString;
        } else if (milliSeconds < HOUR && milliSeconds > 15 * MINUTE) {
            t = Math.round(milliSeconds / MINUTE);
            retString = String.format("%dm", t);
            //if (t > 1) {
            //    retString += "s";
            //}
            //return retString;
        } else if (milliSeconds < 15 * MINUTE) {
            retString = "Just now";
        } else {
            //Throw an exception instead?
        }
        return retString;
    }

    public static String booleanToString(boolean val) {
        String retVal = "0";
        if (val)
            retVal = "1";
        return retVal;
    }

    public static boolean intToBoolean(int val) {
        boolean retVal = false;
        if (val == 1)
            retVal = true;
        return retVal;
    }

    public static String lessDownVote(String countString) {

        // since we can't be negative (that is, we can't ever have a number that
        // is less than zero down votes), we know that if we want to subtract
        // the first char is always going to be '-'.

        int downCountInt = Integer.valueOf(countString.substring(1));
        String retCount = "0";
        if (downCountInt > 1)
            retCount = "-" + String.valueOf(downCountInt - 1);

        return retCount;
    }

    public static String moreDownVote(String countString) {

        // since we could be zero, we need to check for the first char
        // to see if it is negative or not.

        String retCount = "-1";

        try {
            if (countString.substring(0, 1).equals("-")) {
                int downCountInt = Integer.valueOf(countString.substring(1));
                retCount = "-" + String.valueOf(downCountInt + 1);
            }

        } catch (Exception e) {
            retCount = "0";
        }

        return retCount;
    }

    public static String shortenString(String str) {

        String retString = str;

        try {
            if (str.length() > 20) {
                retString = str.substring(0, 20) + " ...";
            }
        } catch (Exception e) {
            retString = "";
        }

        return retString;
    }

    public static String getAppVersion(Context context) {
        String version = "Unknown";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception ex) {
            Log.d("YellrUtils.GetAppVersion()", "Error: " + ex.toString());
        }
        return version;
    }

    public static String buildUrl(Context context, String baseUrl) {

        String url = null;

        double latLng[] = YellrUtils.getLocation(context);
        if (latLng != null) {

            String languageCode = Locale.getDefault().getLanguage();

            url = baseUrl
                    + "?cuid=" + YellrUtils.getCUID(context)
                    + "&language_code=" + languageCode
                    + "&lat=" + latLng[0]
                    + "&lng=" + latLng[1]
                    + "&platform=" + "Android"
                    + "&app_version=" + YellrUtils.getAppVersion(context);
        }

        return url;
    }

    // derived from
    //     http://stackoverflow.com/a/7472559
    public static double roundLocation(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        double retVal = Double.valueOf(twoDForm.format(d));
        return retVal;
    }

    public static double[] getLocation(Context context) {

        /*
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String bestProvider = lm.getBestProvider(criteria, true);
        Location location = lm.getLastKnownLocation(bestProvider); //LocationManager.GPS_PROVIDER);
        */

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = lm.getAllProviders();
        //providers.add(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        Log.d("YellrUtils.getLocation()", "providers: " + providers.toString());
        try {
            for (int i = 0; i < providers.size(); i++) {
                Location l = lm.getLastKnownLocation(providers.get(i));
                if (null != l) {
                    location = l;
                    break;
                }
            }
        } catch (Exception ex) {
            Log.d("YellrUtils.getLocation()", "Error: " + ex.toString());
        }

        // default to invalid lat/lng values
        double latitude = 1000; //43.1656;
        double longitude = 1000; //-77.6114;
        double[] latLng = null;
        // if we have a location available, then set it
        if (null != location) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            // round the location for obfuscation.  Rounding
            // to two decimal places provides approximately
            // half a km of obfuscation.
            latLng = new double[2];
            latLng[0] = roundLocation(latitude);
            latLng[1] = roundLocation(longitude);

            YellrUtils.saveLocation(context, latLng[0], latLng[1]);

        } else if (BuildConfig.SPOOF_LOCATION.equals("1")) {

            latLng = new double[2];
            latLng[0] = 43.1656;
            latLng[1] = -77.6114;

        } else {

            Log.d("YellrUtils.getLocation()", "Pulling from saved location ...");

            // we may have lost location, let's see if we have one saved.
            // note: if we don't have a saved location, or that saved
            //       location is invalid, this will return null.
            latLng = YellrUtils.getSavedLocation(context);

            if ( latLng == null ) {
                Log.d("YellrUtils.getLocation()", "WARNING: Pulled location was null.");
            } else {
                Log.d("YellrUtils.getLocation()", "Pulled location was valid.");
            }
        }

        //Log.d("YellrUtils.getLocation()", "Location: " + String.valueOf(latLng));

        return latLng;
    }

    public static void saveLocation(Context context, double lat, double lng) {
        SharedPreferences sharedPref = context.getSharedPreferences("lastLocation", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastLat", String.valueOf(lat));
        editor.putString("lastLng", String.valueOf(lng));
        editor.commit();
    }

    public static double[] getSavedLocation(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("lastLocation", Context.MODE_PRIVATE);
        String lastLatStr = sharedPref.getString("lastLat", null);
        String lastLngStr = sharedPref.getString("lastLng", null);
        double [] latLng = null;
        if ( lastLatStr != null && !lastLatStr.equals(null) && lastLngStr != null && !lastLngStr.equals(null)) {
            latLng = new double[2];
            try {
                latLng[0] = Double.parseDouble(lastLatStr);
                latLng[1] = Double.parseDouble(lastLngStr);
            } catch (NumberFormatException e) {
                latLng = null;
            }
        }
        return latLng;
    }


    public static void resetHomeLocation(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("homeLocationSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("homeLocationSet", String.valueOf(false));
        editor.commit();
    }

    public static boolean isHomeLocationSet(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("homeLocationSet", Context.MODE_PRIVATE);
        String homeLocationSetString = sharedPref.getString("homeLocationSet", "false");
        return Boolean.valueOf(homeLocationSetString);
    }

    public static void setHomeLocation(Context context, String zipcode, String city, String stateCode, Float lat, Float lng) {
        SharedPreferences sharedPref = context.getSharedPreferences("homeLocationSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("zipcode", zipcode);
        editor.putString("city", city);
        editor.putString("stateCode", stateCode);
        editor.putString("lat", String.valueOf(lat));
        editor.putString("lng", String.valueOf(lng));
        editor.putString("homeLocationSet", String.valueOf(true));
        editor.commit();
        //String homeLocationSetString = sharedPref.getString("homeLocationSet", "false");
        //return Boolean.valueOf(homeLocationSetString);
    }

    public static Float[] getHomeLocation(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("homeLocationSet", Context.MODE_PRIVATE);
        Float lat = Float.valueOf(sharedPref.getString("lat", "0"));
        Float lng = Float.valueOf(sharedPref.getString("lng", "0"));
        Float[] latLng = new Float[2];
        latLng[0] = lat;
        latLng[1] = lng;
        return latLng;
    }

    public static String getCUID(Context context) {
        String cuid;
        SharedPreferences sharedPref = context.getSharedPreferences("cuid", Context.MODE_PRIVATE);
        cuid = sharedPref.getString("cuid", "");

        // check to see if there is a cuid on the device, if not created one
        if (cuid.equals("")) {

            cuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("cuid", cuid);
            editor.commit();
        }
        return cuid;
    }

    public static void resetCUID(Context context) {
        String cuid;
        SharedPreferences sharedPref = context.getSharedPreferences("cuid", Context.MODE_PRIVATE);

        cuid = UUID.randomUUID().toString();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cuid", cuid);
        editor.commit();
    }

    public static String getPreviewImageName(String filename) {

        //String[] filenameParts = filename.split(".");

        StringTokenizer st = new StringTokenizer(filename, ".");
        String baseFilename = st.nextToken();
        String fileExtention = st.nextToken();

        //Log.d("YellrUtils.getPreviewImageName()","baseFilename: " + baseFilename);
        //Log.d("YellrUtils.getPreviewImageName()","fileExtention: " + fileExtention);

        String previewFileName = baseFilename + "p." + fileExtention;

        return previewFileName;
    }

    public static String getFileName(String url) {
        String file = url.substring(url.lastIndexOf('/')+1, url.lastIndexOf('.'));
        return file;
    }

    //
    // This function modified from here:
    //    http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
    ///
    public static Bitmap downloadBitmap(String url) {
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);

        Bitmap bitmap = null;

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    //final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmap = BitmapFactory.decodeStream(inputStream);

                    // resize image if it is the incorrect orientation

                    if (bitmap.getHeight() > bitmap.getWidth() ) {
                        int newHeight = (int)((7.0/16.0) * (double)bitmap.getHeight());
                        int newWidth = bitmap.getWidth();
                        bitmap = Bitmap.createBitmap(bitmap,0, (int)((3.0/16.0)*(double)bitmap.getHeight()),newWidth,newHeight);
                    }

                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Log.d("YellrUtils.downloadBitmap()", "Error while retrieving bitmap from " + url + ": " + e.toString());
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return bitmap;
    }

    public static String downloadJson(Context context, String url){

        String jsonString = "{}";

        try {

            //
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();

            //
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));

            //
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            jsonString = builder.toString();

            Log.d("YellrUtils.downloadJson()", "Successfully downloaded JSON from server");

        } catch (Exception e) {
            Log.d("YellrUtils.downloadJson()", "Error: " + e.toString());
        }

        return jsonString;
    }

    public static Assignment[] decodeAssignmentJson(Context context, String assignmentsJson) {
        Gson gson = new Gson();
        AssignmentsResponse response = new AssignmentsResponse();
        try{
            response = gson.fromJson(assignmentsJson, AssignmentsResponse.class);
        } catch(Exception e){
            Log.d("YellrUtils.decodeAssignmentJson()", "ERROR: GSON puked.");
        }

        Assignment[] assignments = null;
        if (response.success && response.assignments != null) {
            //assignmentsArrayAdapter.clear();
            assignments = new Assignment[response.assignments.length];
            for (int i = 0; i < response.assignments.length; i++) {
                Assignment assignment = response.assignments[i];
                //assignmentsArrayAdapter.add(assignment);
                assignments[i] = assignment;
            }
        }

        return assignments;
    }

    public static void buildNewAssignmentNotification(Context context, Assignment assignment) {
        Intent assignmentIntent;
        assignmentIntent = new Intent(context, PostActivity.class);
        assignmentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getNotificationIcon())
                        .setContentTitle(assignment.question_text)
                        .setContentText(assignment.description);

        assignmentIntent.putExtra(PostFragment.ARG_ASSIGNMENT_QUESTION, assignment.question_text);
        assignmentIntent.putExtra(PostFragment.ARG_ASSIGNMENT_DESCRIPTION, assignment.description);
        assignmentIntent.putExtra(PostFragment.ARG_ASSIGNMENT_ID, assignment.assignment_id);

        PendingIntent pendingAssignmentIntent = PendingIntent.getActivity(context, 0, assignmentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingAssignmentIntent);
        mBuilder.setAutoCancel(true); // clear notification after click
        int assignmentNotificationId = 2;
        NotificationManager mNotificationMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationMgr.notify(assignmentNotificationId, mBuilder.build());
    }

    private static int getNotificationIcon() {
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.drawable.ic_stat_y : R.drawable.icon;
    }

    public static void setCurrentAssignmentIds(Context context, Assignment[] assignments) { //String[] currentAssignmentIds) {

        String[] currentAssignmentIds = new String[assignments.length];
        for (int i = 0; i < assignments.length; i++) {
            currentAssignmentIds[i] = String.valueOf(assignments[i].assignment_id);
        }
        //YellrUtils.setCurrentAssignmentIds(context, currentAssignmentIds);

        SharedPreferences currentAssignmentIdsCountPref = context.getSharedPreferences("current_assignment_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentAssignmentIdsPref = context.getSharedPreferences("current_assignment_ids", Context.MODE_PRIVATE);

        StringBuilder currentAssignmentIdsCsv = new StringBuilder();
        for (int i = 0; i < currentAssignmentIds.length; i++) {
            currentAssignmentIdsCsv.append(currentAssignmentIds[i]).append(",");
        }

        SharedPreferences.Editor countEditor = currentAssignmentIdsCountPref.edit();
        countEditor.putString("current_assignment_ids_count", String.valueOf(currentAssignmentIds.length));
        countEditor.commit();

        SharedPreferences.Editor idsEditor = currentAssignmentIdsPref.edit();
        idsEditor.putString("current_assignment_ids", currentAssignmentIdsCsv.toString());
        idsEditor.commit();
    }

    public static String[] getCurrentAssignmentIds(Context context) {
        SharedPreferences currentAssignmentIdsCountPref = context.getSharedPreferences("current_assignment_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentAssignmentIdsPref = context.getSharedPreferences("current_assignment_ids", Context.MODE_PRIVATE);

        String currentAssignmentIdsCsv = currentAssignmentIdsPref.getString("current_assignment_ids", "");
        int count = Integer.parseInt(currentAssignmentIdsCountPref.getString("current_assignment_ids_count", "0"));

        StringTokenizer st = new StringTokenizer(currentAssignmentIdsCsv, ",");
        String[] currentAssignmentIds = new String[count];
        for (int i = 0; i < count; i++) {
            currentAssignmentIds[i] = st.nextToken();
        }
        return currentAssignmentIds;
    }

    public static void setCurrentStoryIds(Context context, String[] currentStoryIds) {
        SharedPreferences currentStoryIdsCountPref = context.getSharedPreferences("current_story_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentStoryIdsPref = context.getSharedPreferences("current_story_ids", Context.MODE_PRIVATE);

        StringBuilder currentStoryIdsCsv = new StringBuilder();
        for (int i = 0; i < currentStoryIds.length; i++) {
            currentStoryIdsCsv.append(currentStoryIds[i]).append(",");
        }

        SharedPreferences.Editor countEditor = currentStoryIdsCountPref.edit();
        countEditor.putString("current_story_ids_count", String.valueOf(currentStoryIds.length));
        countEditor.commit();

        SharedPreferences.Editor idsEditor = currentStoryIdsPref.edit();
        idsEditor.putString("current_story_ids", currentStoryIdsCsv.toString());
        idsEditor.commit();
    }

    public static String[] getCurrentStoryIds(Context context) {
        SharedPreferences currentStoryIdsCountPref = context.getSharedPreferences("current_story_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentStoryIdsPref = context.getSharedPreferences("current_story_ids", Context.MODE_PRIVATE);

        String currentStoryIdsCsv = currentStoryIdsPref.getString("current_story_ids", "");
        int count = Integer.parseInt(currentStoryIdsCountPref.getString("current_story_ids_count","0"));

        StringTokenizer st = new StringTokenizer(currentStoryIdsCsv, ",");
        String[] currentStoryIds = new String[count];
        for (int i = 0; i < count; i++) {
            currentStoryIds[i] = st.nextToken();
        }
        return currentStoryIds;
    }

    public static void setCurrentNotificationIds(Context context, String[] currentNotificationIds) {
        SharedPreferences currentNotificationIdsCountPref = context.getSharedPreferences("current_notification_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentNotificationIdsPref = context.getSharedPreferences("current_notification_ids", Context.MODE_PRIVATE);

        StringBuilder currentNotificationIdsCsv = new StringBuilder();
        for (int i = 0; i < currentNotificationIds.length; i++) {
            currentNotificationIdsCsv.append(currentNotificationIds[i]).append(",");
        }

        SharedPreferences.Editor countEditor = currentNotificationIdsCountPref.edit();
        countEditor.putString("current_notification_ids_count", String.valueOf(currentNotificationIds.length));
        countEditor.commit();

        SharedPreferences.Editor idsEditor = currentNotificationIdsPref.edit();
        idsEditor.putString("current_notification_ids", currentNotificationIdsCsv.toString());
        idsEditor.commit();
    }

    public static String[] getCurrentNotificationIds(Context context) {
        SharedPreferences currentNotificationIdsCountPref = context.getSharedPreferences("current_notification_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentNotificationIdsPref = context.getSharedPreferences("current_notification_ids", Context.MODE_PRIVATE);

        String currentNotificationIdsCsv = currentNotificationIdsPref.getString("current_notification_ids", "");
        int count = Integer.parseInt(currentNotificationIdsCountPref.getString("current_notification_ids_count","0"));

        StringTokenizer st = new StringTokenizer(currentNotificationIdsCsv, ",");
        String[] currentNotificationIds = new String[count];
        for (int i = 0; i < count; i++) {
            currentNotificationIds[i] = st.nextToken();
        }
        return currentNotificationIds;
    }

    public static void setCurrentMessageIds(Context context, String[] currentMessageIds) {
        SharedPreferences currentMessageIdsCountPref = context.getSharedPreferences("current_message_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentMessageIdsPref = context.getSharedPreferences("current_message_ids", Context.MODE_PRIVATE);

        StringBuilder currentMessageIdsCsv = new StringBuilder();
        for (int i = 0; i < currentMessageIds.length; i++) {
            currentMessageIdsCsv.append(currentMessageIds[i]).append(",");
        }

        SharedPreferences.Editor countEditor = currentMessageIdsCountPref.edit();
        countEditor.putString("current_message_ids_count", String.valueOf(currentMessageIds.length));
        countEditor.commit();

        SharedPreferences.Editor idsEditor = currentMessageIdsPref.edit();
        idsEditor.putString("current_message_ids", currentMessageIdsCsv.toString());
        idsEditor.commit();
    }

    public static String[] getCurrentMessageIds(Context context) {
        SharedPreferences currentMessageIdsCountPref = context.getSharedPreferences("current_message_ids_count", Context.MODE_PRIVATE);
        SharedPreferences currentMessageIdsPref = context.getSharedPreferences("current_message_ids", Context.MODE_PRIVATE);

        String currentMessageIdsCsv = currentMessageIdsPref.getString("current_message_ids", "");
        int count = Integer.parseInt(currentMessageIdsCountPref.getString("current_message_ids_count","0"));

        StringTokenizer st = new StringTokenizer(currentMessageIdsCsv, ",");
        String[] currentMessageIds = new String[count];
        for (int i = 0; i < count; i++) {
            currentMessageIds[i] = st.nextToken();
        }
        return currentMessageIds;
    }
}