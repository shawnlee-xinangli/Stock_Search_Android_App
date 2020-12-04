package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class NewsRecyclerAdapter extends  RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder>{
    List<NewsItem> newsList;



    private Context context;
    public NewsRecyclerAdapter(List<NewsItem> newsList){ this.newsList = newsList;}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        View view;
        if (viewType == 1) {
            // inflate your first item layout & return that viewHolder
            view = layoutInflater.inflate(R.layout.first_news_row,parent,false);
        }else  {
            view = layoutInflater.inflate(R.layout.news_row,parent,false);
        }

        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 1;
        }else {
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NewsRecyclerAdapter.ViewHolder holder, int position) {
        NewsItem news = newsList.get(position);

        Picasso.get().load(news.imageUrl).fit().into(holder.imageView);
        holder.sourceTextView.setText(news.source);
        holder.publishedDataTextView.setText(news.publishedDate);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        try {
            long dateMill = format.parse(news.publishedDate).getTime() ;
            Date currentDate = new Date();
            long currentMill = currentDate.getTime();
            long diff = currentMill - dateMill;
            if (diff > 24*60*60*1000){
                long day = diff / (24*60*60*1000);
                if (day == 1) {
                    holder.publishedDataTextView.setText("1 day ago");
                }else {
                    holder.publishedDataTextView.setText(day + " days ago");
                }
            }else {
                long minutes = diff / (60*1000);
                holder.publishedDataTextView.setText(minutes + " minutes ago");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


        holder.newsTitleTextView.setText(news.title);

        holder.itemView.setLongClickable(true);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.url));
                context.startActivity(browserIntent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.news_share);


                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.dialogueTitleTextView);
                text.setText(news.title);
                ImageView imageView = (ImageView) dialog.findViewById(R.id.dialogueNewsImageView);


                Picasso.get().load(news.imageUrl).fit().into(imageView);
                ImageView twitterView = (ImageView) dialog.findViewById(R.id.twitterImageView);
                ImageView chromeView = (ImageView) dialog.findViewById(R.id.chromeImageView);
                twitterView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String twitterURL= "https://twitter.com/intent/tweet?text=Check out this Link:&url=" +news.url;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterURL));
                        context.startActivity(browserIntent);
                    }
                });

                chromeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.url));
                        context.startActivity(browserIntent);
                    }
                });



//                Button sellButton = (Button) dialog.findViewById(R.id.sellButton);
//                // if button is clicked, close the custom dialog
                dialog.show();
//
//                sellButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView sourceTextView ;

        TextView publishedDataTextView ;
        TextView newsTitleTextView;
        ImageView imageView;
        View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            publishedDataTextView = itemView.findViewById(R.id.publishedDateTextView);
            newsTitleTextView = itemView.findViewById(R.id.newsTitleTextView);
            imageView =  itemView.findViewById(R.id.newsImage);


        }
    }
}
