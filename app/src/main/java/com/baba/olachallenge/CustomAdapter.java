package com.baba.olachallenge;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.OkHttpDownloader;
import okhttp3.OkHttpClient;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RaghuBaba on 16-12-2017.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.TrackViewHolder> {

    private ArrayList<HashMap<String, String>> dataSet;

    Context context;


    public static class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView tvSongName;
        TextView tvArtistName;
        ImageView imgvw_coverimg;
        ImageView imgvw_playlist;




        public TrackViewHolder(View itemView) {
            super(itemView);
            this.tvSongName = (TextView) itemView.findViewById(R.id.tv_songName);
            this.tvArtistName = (TextView) itemView.findViewById(R.id.tv_artistName);
            this.imgvw_coverimg = (ImageView) itemView.findViewById(R.id.imgvw_coverimg);
            this.imgvw_playlist = (ImageView) itemView.findViewById(R.id.imgvw_playlist);
        }
    }

    public CustomAdapter(ArrayList<HashMap<String, String>> data,Context ctx) {
        this.dataSet = data;
        this.context = ctx;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public CustomAdapter.TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);


        view.setOnClickListener(MainActivity.trackOnClickListener);

        TrackViewHolder songViewHolder = new TrackViewHolder(view);
        return songViewHolder;

    }

    @Override
    public void onBindViewHolder(CustomAdapter.TrackViewHolder holder, int position) {

        TextView tvSongName = holder.tvSongName;
        TextView tvArtistName = holder.tvArtistName;
        ImageView imgvw_coverimg = holder.imgvw_coverimg;
        String url;

        tvSongName.setText(dataSet.get(position).get("song"));
        tvArtistName.setText(dataSet.get(position).get("artists"));

        //Load images in imgvw
        url = dataSet.get(position).get("cover_image");
        String track_Name = dataSet.get(position).get("song");

        Glide.with(context).load(url).into(imgvw_coverimg);
//        Picasso.Builder builder = new Picasso.Builder(context);
//        builder.downloader(new OkHttpDownloader(context));
//        builder.build().load(url.trim()).into(imgvw_coverimg);

        holder.imgvw_playlist.setTag(track_Name);
        holder.imgvw_playlist.setOnClickListener(MainActivity.addtoplaylistOnClickListener);


    }


}
