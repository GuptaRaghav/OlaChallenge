package com.baba.olachallenge;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    RelativeLayout main_song_list;
    TextView tv_selected_track_title;
    ImageView imgvw_playercontrol;
    ImageView imgvw_selected_track_image;
    ImageView imgvw_download;
    static View.OnClickListener trackOnClickListener;
    static View.OnClickListener addtoplaylistOnClickListener;

    String selectedTrack;

    private Boolean isplayerPause = false;
    //Player Toolbar
    Toolbar tb_selected_track;

    //Search toolbar
    EditText inputSearch;

    // Audio Player Variable
    static MediaPlayer mMediaPlayer;
    static ArrayList<HashMap<String, String>> data;

    ArrayList<HashMap<String, String>> playlist;
    ArrayList<HashMap<String, String>> bestSongs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        //Initiate SearchBar
        inputSearch = (EditText)findViewById(R.id.inputSearch);

        //Initatiate Main Song list
        main_song_list = findViewById(R.id.main_song_list);

        //Initiate Recylerview
        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Initiate Track Toolbar

        tb_selected_track = (Toolbar)findViewById(R.id.tb_selected_track);
        tv_selected_track_title = (TextView)findViewById(R.id.selected_track_title);
        imgvw_playercontrol = (ImageView) findViewById(R.id.player_control);
        imgvw_selected_track_image= (ImageView)findViewById(R.id.selected_track_image);
        imgvw_download = (ImageView)findViewById(R.id.imgvw_download);

        //Initialise Playlist array

        playlist = new ArrayList<>();

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence searchedtrack, int arg1, int arg2, int arg3) {

                searchedtrack = searchedtrack.toString().toLowerCase();

                final ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();

                for (int i = 0; i < data.size(); i++) {

                    final String text = data.get(i).get("song").toLowerCase();

                    if (text.contains(searchedtrack)) {

                        filteredList.add(data.get(i));
                    }
                }

                listUpdate(filteredList,MainActivity.this);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        imgvw_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String track_URL = null;
                for (HashMap<String, String> trackdetail : data) {
                    if (trackdetail.get("song").equalsIgnoreCase(selectedTrack)){
                        track_URL = trackdetail.get("url");
                        break;
                    }
                }

                DownloadTrack downloadTrack = new DownloadTrack(MainActivity.this,selectedTrack,track_URL);
                downloadTrack.execute();
            }
        });

        //Initialising Audio player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                imgvw_playercontrol.setImageResource(R.drawable.play);
                //Play Music from playlist
                if (playlist.size()>0) {
                    try {
                        for (int i = 0; i < playlist.size(); i++) {

                            String trackUrl = playlist.get(i).get("url");
                            mMediaPlayer.setDataSource(trackUrl);
                            mMediaPlayer.prepareAsync();
                            playlist.remove(i);
                        }
                    }
                    catch(IOException ioexception){

                    }
                }
            }
        });

        trackOnClickListener = new TrackOnClickListener();

        addtoplaylistOnClickListener = new AddToPlaylistOnClickListener();

        imgvw_playercontrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });



        GetTracksList getTracksList = new GetTracksList(MainActivity.this,recyclerView);
        getTracksList.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_home:
                this.setTitle(getResources().getString(R.string.app_name));
                listUpdate(data,MainActivity.this);

                return true;

            case R.id.menu_bestsong:
                this.setTitle(getResources().getString(R.string.menu_bestsong));
                selectBestSongs();
                listUpdate(bestSongs,MainActivity.this);
                return true;

            case R.id.menu_playlist:

                if (playlist.size()>0){
                    this.setTitle(getResources().getString(R.string.title_Playlist));
                    listUpdate(playlist,MainActivity.this);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setPositiveButton(R.string.okbtn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setMessage(R.string.alertDialog_Message);
                    // Create the AlertDialog object and return it
                    builder.create();
                    builder.show();
                }

                return true;

            case R.id.menu_portfolio:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setPositiveButton(R.string.okbtn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //Redirect User to My Work on playstore
                               Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.portfolioURL)));
                                startActivity(i);

                            }
                        });
                builder.setMessage(R.string.portfolioMessage);
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    //To Check Android Version for Permission in 6.0 and above inn runtime
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void selectBestSongs(){
        bestSongs = new ArrayList<>();
        Random random = new Random();
        int index= random.nextInt(data.size());
        if (index>0){

            for (int i=0 ;i<=index;i++){
                HashMap<String,String> bestTrack = data.get(i);
                bestSongs.add(bestTrack);
            }
        }
        else {
            selectBestSongs();
        }

    }
    private void listUpdate(ArrayList<HashMap<String,String>> arrayList,Context ctx){

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        RecyclerView.Adapter mAdapter = new CustomAdapter(arrayList, ctx);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

    private void togglePlayPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isplayerPause = true;
            imgvw_playercontrol.setImageResource(R.drawable.play);
        }
        else {
            mMediaPlayer.start();
            imgvw_playercontrol.setImageResource(R.drawable.pause);
        }
    }
    public void Download_CoverImage(String url,Context ctx,ImageView imgvw){
        Glide.with(ctx).load(url).into(imgvw);
//        Picasso.Builder builder = new Picasso.Builder(ctx);
//        //builder.downloader(new OkHttpDownloader(ctx));
//        builder.listener(new Picasso.Listener()
//        {
//            @Override
//            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
//            {
//                Log.e("Picasso Error",exception.getMessage());
//            }
//        });
        //builder.build().load(url.trim()).into(imgvw);

    }

    private class TrackOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
        //Handle any uncaught Error in runtime
            try {
                //Switch on Player Toolbar
                tb_selected_track.setVisibility(View.VISIBLE);
//                RelativeLayout.LayoutParams relparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                relparams.setMargins(0,0,0,100);
//                main_song_list.setLayoutParams(relparams);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)main_song_list.getLayoutParams();
                params.setMargins(0, 0, 0, 100);
                main_song_list.setLayoutParams(params);
                int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
                TextView textViewName = (TextView) viewHolder.itemView.findViewById(R.id.tv_songName);
                selectedTrack = (String) textViewName.getText();
                tv_selected_track_title.setText(selectedTrack);

                for (HashMap<String, String> hashmap : data) {

                    if (hashmap.get("song") == selectedTrack) {

                        String cover_image = hashmap.get("cover_image");
                        String track_URL = hashmap.get("url");
                        Download_CoverImage(cover_image, MainActivity.this, imgvw_selected_track_image);

                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                            mMediaPlayer.reset();
                        }
                        if (mMediaPlayer.getCurrentPosition()>1 && isplayerPause){
                            isplayerPause = true;
                            mMediaPlayer.stop();
                            mMediaPlayer.reset();
                        }

                        try {
                            mMediaPlayer.setDataSource(track_URL);
                            mMediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException illegalexception) {
                            Log.e("Illegal State Exception", illegalexception.getMessage());
                        }
                        break;
                    }
                }

            } catch(Exception e){

                Log.e("TrackOnClickkListener",e.getMessage());
            }
        }
    }

    private class AddToPlaylistOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            String track_Name = v.getTag().toString();
            Toast.makeText(MainActivity.this,getResources().getText(R.string.playlistAdded),Toast.LENGTH_LONG).show();
            for (HashMap<String, String> trackdetail : data) {
                if (trackdetail.get("song").equalsIgnoreCase(track_Name)) {
                    playlist.add(trackdetail);
                    break;
                }
            }
        }
    }

}
