package com.example.myhouseforlicensedrealestateagent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NetworkTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String url;
    private String data;
    private int selection;

    public NetworkTask(Context _context, String url, String data, int action) {
        this.context = _context;
        this.url = url;
        this.data = data;
        this.selection = action;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = null;

            try {
                RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
                result = requestHttpURLConnection.Request(url, data);

            } catch (Exception e) {
                result = "Error";
            }

            return result;

    }

    @Override
    protected void onPreExecute() {
        //로딩
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(String result) {
        try {
            //경우에 따라 결과 값을 받아 일어났으면 하는 작업
            switch (this.selection) {
                case 1:
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String real_result = jsonObject.getString("result");
                        if (real_result.equals("success")) {
                            Toast.makeText(this.context, "성공적으로 저장 되었습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this.context, "저장에 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }catch (Exception e){

        }
    }
}
