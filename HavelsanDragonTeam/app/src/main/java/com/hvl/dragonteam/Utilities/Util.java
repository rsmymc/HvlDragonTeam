package com.hvl.dragonteam.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.sql.Time;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class Util {

    public static final String DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm = "dd MMM yyyy, EEE HH:mm";
    public static final String DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm_ss = "dd MMM yyyy, EEE HH:mm:ss";
    public static final String DATE_FORMAT_yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final int MINUTES_PER_HOUR = 60;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

    public void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public void shareTextIntent(String body, Context context) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share)));
    }



  /*  public static Comparator<PersonTransportationView> timeComparator = new Comparator<PersonTransportationView>() {
        @Override
        public int compare(PersonTransportationView p1, PersonTransportationView p2) {
            int diff1 = p1.getDailyTimeDiffInSecs();
            int diff2 = p2.getDailyTimeDiffInSecs();

            if (diff1 == diff2) {
                return 0;
            } else if (diff1 > diff2) {
                return 1;
            } else {
                return -1;
            }
        }
    };
*/


    public static String convertTime(long time, String pattern) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static int getTimeZone() {
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        return (int) TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
    }

    public static String formatDate(Date date, String format) {

        SimpleDateFormat format1 = new SimpleDateFormat(format);

        String formatted = format1.format(date);

        return formatted;
    }

    public static String parseDate(String date,String from, String to)
    {
        SimpleDateFormat fromUser = new SimpleDateFormat(from);
        SimpleDateFormat myFormat = new SimpleDateFormat(to);

        try {
           return myFormat.format(fromUser.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }


    public static void postRequest(Context context, String url, Map<String, String> params, VolleyCallback volleyCallback) {

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        if (volleyCallback != null) {
                            try {
                                volleyCallback.onSuccess(new JSONObject(response));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.Response", error.getLocalizedMessage());
                        if (volleyCallback != null) {
                            volleyCallback.onError(error.toString());
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };


        queue.add(postRequest);
    }

    public static String setDirectoryFromFileName(String fileName) {
        int hash = fileName.hashCode();
        int mask = 255;
        int firstDir = hash & mask;
        int secondDir = (hash >> 8) & mask;
        String path = new StringBuilder(File.separator)
                .append(String.format("%03d", firstDir))
                .append(File.separator)
                .append(String.format("%03d", secondDir))
                .append(File.separator)
                .toString();
        return path;
    }

    public static String getCurrentVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pInfo = null;
        try {
            pInfo = pm.getPackageInfo(context.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e1) {

            e1.printStackTrace();
        }
        return pInfo.versionName;

    }

    @NonNull
    public static String getLocalizedString(Context context, Locale desiredLocale, int id) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getString(id);
    }

    public static String getShortName(String name){
        String shortName = "";

        String [] arr = name.split(" ");

        for (int i = 0; i<arr.length-1; i ++){

            shortName += arr[i].substring(0,1) + ". ";
        }
        shortName += arr[arr.length-1];

        return shortName;
    }

    public static void toastError(Context context){
        toastError(context, context.getString(R.string.error));
    }

    public static void toastError(Context context, String message){
        Toasty.error(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void toastInfo(Context context, String message){
        Toasty.info(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void toastInfo(Context context, int message){
        Toasty.info(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void toastWarning(Context context, String message){
        Toasty.warning(context, message, Toast.LENGTH_LONG, false).show();
    }
    public static void toastWarning(Context context, int  message){
        Toasty.warning(context, message, Toast.LENGTH_LONG, false).show();
    }

}
