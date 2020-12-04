package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChildRecyclerAdapter extends RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract{
    public List<StockItem> items;
    private Context context;

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public ChildRecyclerAdapter(List<StockItem> items){
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        View view = layoutInflater.inflate(R.layout.item_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockItem entry = items.get(position);
        holder.tickerTextView.setText(entry.ticker);
        holder.closePriceTextView.setText(String.valueOf(entry.closePrice));

        if (entry.change > 0) {
            holder.uptrendView.setVisibility(View.VISIBLE);
            holder.changeTextView.setTextColor(Color.parseColor("#4CAF50"));
            holder.changeTextView.setText(String.format("%.2f",entry.change));
        }else if (entry.change < 0){
            holder.downtrendView.setVisibility(View.VISIBLE);
            holder.changeTextView.setTextColor(Color.parseColor("#AC1105"));
            holder.changeTextView.setText(String.format("%.2f",Math.abs(entry.change)));
        }
        if (entry.numberOfShares > 0) {
            holder.nameTextView.setText(String.valueOf(entry.numberOfShares) +" shares");
        }else {
            holder.nameTextView.setText(entry.name);
        }

        holder.goToButtonView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DisplayMessageActivity.class);
            Log.d("StockItem", "sendMessage: "+ entry.ticker);
            intent.putExtra(EXTRA_MESSAGE,entry.ticker);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(ViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.WHITE);

    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(StockItem item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public List<StockItem> getData() {
        return items;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tickerTextView;
        TextView closePriceTextView;
        TextView nameTextView;
        TextView changeTextView;
        ImageView uptrendView;
        ImageView downtrendView;
        ImageView goToButtonView;
        View rowView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowView = itemView;
            tickerTextView = itemView.findViewById(R.id.tickerTextView);
            closePriceTextView = itemView.findViewById(R.id.closePriceView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            changeTextView = itemView.findViewById(R.id.changeTextView);
            uptrendView = itemView.findViewById(R.id.uptrendView);
            downtrendView = itemView.findViewById(R.id.downtrendView);
            goToButtonView = itemView.findViewById(R.id.GotoButtonView);


        }
    }
}
