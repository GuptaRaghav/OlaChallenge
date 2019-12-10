package com.baba.olachallenge;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by RaghuBaba on 20-12-2017.
 */

public class DownloadTrack extends AsyncTask<String, Integer, String> {

    Context ctx;
    String trackurl;
    String trackName;
    URL downloadUrl ;

    public DownloadTrack(Context ctx,String trackName, String trackurl) {

        this.ctx = ctx;
        this.trackName = trackName;
        this.trackurl = trackurl;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(ctx,ctx.getResources().getString(R.string.downloadStarted),Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(String... params) {
        int count;

        try {
            //Creating mp3 file Directory

            File olaplaystudiodir =new File(android.os.Environment.getExternalStorageDirectory(),"Ola Play Studio");

            if(!olaplaystudiodir.exists())
                olaplaystudiodir.mkdirs();

            /*String trackName = trackurl.substring(0,trackurl.indexOf("$"));*/

            /*trackurl = trackurl.substring(trackurl.indexOf("$")+1,trackurl.length());*/

            // Crreate Mp3 file
            File olaplaystudioMP3  =new File(olaplaystudiodir,trackName+".mp3");

            URL url = new URL(trackurl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(9000);
            connection.connect();

            //Handles Redirected URL
            if(connection.getResponseCode()==HttpURLConnection.HTTP_MOVED_TEMP||connection.getResponseCode()==HttpURLConnection.HTTP_MOVED_PERM)
                downloadUrl = new URL(connection.getHeaderField("Location"));

            // download the file
            InputStream input = new BufferedInputStream(downloadUrl .openStream());
            OutputStream output = new FileOutputStream(olaplaystudioMP3);

            byte data[] = new byte[1024];

               while ((count = input.read(data)) != -1) {
                     output.write(data, 0, count);
            }

            output.flush();
           output.close();
            input.close();
        } catch (Exception e) {

            Log.e("Downloading Failed",e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(ctx,ctx.getResources().getString(R.string.downloadCompleted),Toast.LENGTH_LONG).show();

    }

}
