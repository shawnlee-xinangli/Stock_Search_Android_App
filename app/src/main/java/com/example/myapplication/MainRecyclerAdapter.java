package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {

    List<Section> sectionList;
    ConstraintLayout constraintLayout;
    private Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public MainRecyclerAdapter(List<Section> sectionList) {
        if (sectionList.size() == 2) {

        }
        this.sectionList = sectionList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        pref = context.getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

        View view = layoutInflater.inflate(R.layout.section_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sectionList.get(position);
        String sectionName = section.getSectionName();
        List<StockItem> items = section.getSectionItems();

        holder.sectionNameTextView.setText(sectionName);


        holder.netWorth.setText(String.format("%.2f",section.getNetWorth()));

        ChildRecyclerAdapter childRecyclerAdapter = new ChildRecyclerAdapter(items);

        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(childRecyclerAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(holder.childRecyclerView);

        holder.childRecyclerView.setAdapter(childRecyclerAdapter);
        holder.childRecyclerView.addItemDecoration(new DividerItemDecoration(holder.childRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        enableSwipeToDeleteAndUndo(childRecyclerAdapter,holder.childRecyclerView,sectionName);

        if (sectionName.equals("FAVOURITES")){
            holder.netWorthTextView.setVisibility(View.GONE);
            holder.netWorth.setVisibility(View.GONE);
        }

    }
    private void enableSwipeToDeleteAndUndo(ChildRecyclerAdapter mAdapter,RecyclerView recyclerView,String sectionName) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(context) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final StockItem item = mAdapter.getData().get(position);

                mAdapter.removeItem(position);
                List<StockItem> stockItems = mAdapter.items;
                String str = listToString(stockItems);
                Log.d("After Remove ", str);


                if (sectionName.equals("FAVOURITES")){
                    Log.d("Portfolio after remove:", pref.getString("portfolio","portfolio is empty"));
                    Log.d("Fav after remove", str);

                    editor.putString("favourites",str);
                    editor.commit();
                }


                Snackbar snackbar = Snackbar
                        .make(constraintLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mAdapter.restoreItem(item, position);
                        recyclerView.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }
    private String listToString(List<StockItem> stockItems){
       String res = "";
       for (StockItem item:stockItems){
           res += item.ticker+":";
           res += item.numberOfShares+",";
       }
       return res;
    }


    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionNameTextView;
        RecyclerView childRecyclerView;
        TextView netWorth;
        TextView netWorthTextView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            netWorth = itemView.findViewById(R.id.netWorthNumberView);
            netWorthTextView = itemView.findViewById(R.id.netWorthTextView);
            sectionNameTextView = itemView.findViewById(R.id.sectionNameTextView);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }

    }
}
