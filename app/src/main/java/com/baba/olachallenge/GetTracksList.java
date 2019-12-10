package com.baba.olachallenge;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by RaghuBaba on 16-12-2017.
 */

public class GetTracksList extends AsyncTask<Void, Void, Void> {

    Context context;
    ProgressDialog progress;
    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;

    private ArrayList<HashMap<String, String>> data;





    public GetTracksList(Context ctx, RecyclerView recyclerView) {
        this.context = ctx;
        this.recyclerView = recyclerView;
        this.data = new ArrayList<>();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Setting Busy Dialog
        progress = new ProgressDialog(context);
        progress.setTitle("Ola Play Studio");
        progress.setMessage("Loading Songs List...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        ServerConnectionHandler serverConnectionHandler = new ServerConnectionHandler();
        // Making a request to url and getting response
        String url = context.getResources().getString(R.string.playstudioAPI);

        String jsonStr = serverConnectionHandler.callOlaApi(url);


        if (jsonStr != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonStr);

                for (int i=0;i<jsonArray.length();i++){
                    JSONObject music = jsonArray.getJSONObject(i);
                    String song = music.getString("song");
                    String song_URL = music.getString("url");
                    String artists = music.getString("artists");
                    String cover_image = music.getString("cover_image");

                    HashMap<String, String> musicDetails = new HashMap<>();
                    musicDetails.put("song",song);
                    musicDetails.put("url",song_URL);
                    musicDetails.put("artists",artists);
                    musicDetails.put("cover_image",cover_image);
                    data.add(musicDetails);
                }

                MainActivity.data = data;


            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }

        } else {
            Log.e(TAG, "Couldn't get json from server.");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        progress.dismiss();
        MainActivity.data = data;
        adapter = new CustomAdapter(data,context);
        recyclerView.setAdapter(adapter);
    }
}

