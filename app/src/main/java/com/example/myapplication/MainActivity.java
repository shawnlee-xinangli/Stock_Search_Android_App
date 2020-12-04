package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.Cache;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String TAG = "MainActivity";
    public static final String INTERNET = "android.permission.INTERNET";
    private static final String BASEURL1 = "https://glass-memento-289318.wl.r.appspot.com/";

    ProgressBar spinner;
    TextView fetchingTextView;


    private Handler handler;
    private SimpleCursorAdapter mAdapter;

    RecyclerView mainRecyclerView;
    TextView currentDateView;
    TextView tiingoTextView;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    List<Section> sectionList;
    RequestQueue queue;
    double netWorth;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">"+ getString(R.string.app_name)+"</font>"));

        spinner = findViewById(R.id.progressBar1);
        currentDateView = findViewById(R.id.currentDateTextView);
        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        tiingoTextView = findViewById(R.id.tiingoTextView);
        fetchingTextView = findViewById(R.id.fetchingTextView);

        spinner.setVisibility(View.VISIBLE);
        fetchingTextView.setVisibility(View.VISIBLE);
        currentDateView.setVisibility(View.GONE);
        mainRecyclerView.setVisibility(View.GONE);
        tiingoTextView.setVisibility(View.GONE);


        Date date = new Date();


        LocalDate anotherSummerDay = LocalDate.of(date.getYear()+1900,date.getMonth()+1,date.getDate()-1);
        Log.d(TAG,DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(anotherSummerDay));
        String s = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(anotherSummerDay);

        int index = s.indexOf(",");
        String info = s.substring(index+2);
        TextView dateView  = findViewById(R.id.currentDateTextView);
        dateView.setText(info);


        queue = Volley.newRequestQueue(this);
        sectionList = new ArrayList<>();

        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode

        initData();
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                refresh();
                Log.i("Timer", "A Kiss every 15 seconds");
            }
        },0,15000);

        TextView tiingoView = findViewById(R.id.tiingoTextView);

        tiingoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/ "));
                startActivity(browserIntent);
            }
         });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        final String[] from = new String[] {"cityName"};
        final int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);



        // Getting selected (clicked) item suggestion
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) mAdapter.getItem(position);
                String txt = cursor.getString(cursor.getColumnIndex("cityName"));
                int index = txt.indexOf("-");
                String mes = txt.substring(0,index);
                searchView.setQuery(mes, true);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getActivity(), DisplayMessageActivity.class);
                intent.putExtra(EXTRA_MESSAGE,query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String companyLatestPriceURL = BASEURL1 + "autoComplete/"+newText;
                JsonArrayRequest autoCompleteRequest = new JsonArrayRequest
                        (Request.Method.GET, companyLatestPriceURL, null, new Response.Listener<JSONArray>() {

                            @Override
                            public void onResponse(JSONArray response) {
                                try {


                                    final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "cityName" });
                                    for (int i=0; i<response.length(); i++) {
                                        JSONObject obj = response.getJSONObject(i);
                                        String ticker = obj.getString("ticker");
                                        String name = obj.getString("name");
                                        c.addRow(new Object[] {i, ticker+"-"+name});
                                    }
                                    mAdapter.changeCursor(c);
                                    searchView.setSuggestionsAdapter(mAdapter);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.d("autoCompleteError",error.toString());
                                error.printStackTrace();
                            }
                        });

                autoCompleteRequest.setRetryPolicy(new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 50000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 50000;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {

                    }
                });
                queue.add(autoCompleteRequest);

             return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public MainActivity getActivity(){
        return this;
    }

    private void initData(){

        List<StockItem> portfolio = new ArrayList<>();
        List<StockItem> favourites = new ArrayList<>();
        String portfolioString = pref.getString("portfolio","");
        String favouriteString = pref.getString("favourites","");
        Log.d(TAG, "Portfolio: "+portfolioString);
        Log.d(TAG, "Favourites: "+favouriteString);

        if (portfolioString != null) {
            String[] portfolioDataSet = portfolioString.split(",");
            for (String info:portfolioDataSet){
                httpRequest(sectionList,portfolio,info,portfolioDataSet.length,0);
            }
        }
        if (!favouriteString.equals("")) {
            String[] favouriteDataSet = favouriteString.split(",");
            for (String info:favouriteDataSet){
                httpRequest(sectionList,favourites,info,favouriteDataSet.length,1);
            }
        }else {
            editor.putString("favourites","MSFT:0,NVDA:0,");
        }

        if (pref.getString("balance",null).equals(null)){
            editor.putString("balance","20000.0");
            editor.commit();
        }



    }

    private void refresh(){
        for(Section section: sectionList){
            for (int i = 0; i < section.getSectionItems().size(); i++){
               helper(section,i);
            }
        }
    }

    private void helper(Section section,int i){
        StockItem item = section.getSectionItems().get(i);
        String ticker = item.ticker;
        String companyLatestPriceURL = BASEURL1 + "companyLatestPrice/"+ticker;
        JsonArrayRequest companyLatestPriceRequest = new JsonArrayRequest
                (Request.Method.GET, companyLatestPriceURL, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            Log.d("CompanyPriceSuccess", obj.toString());
                            double closePrice = 0.0;
                            double change = 0.0;

                            if(!obj.get("last").equals(null)){
                                closePrice = obj.getDouble("last");
                            }

                            if (!obj.get("prevClose").equals(null)) {
                                change = obj.getDouble("last") - obj.getDouble("prevClose");
                            }
                            item.closePrice = closePrice;
                            item.change = change;
                            if (i == section.getSectionItems().size()-1){
                                MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                                mainRecyclerView.setAdapter(mainRecyclerAdapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("LatestPriceError",error.toString());
                        error.printStackTrace();
                    }
                });

        companyLatestPriceRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(companyLatestPriceRequest);
    }

    private void httpRequest(List<Section> sectionList, List<StockItem> list, String info,int size,int flag){
        int index = info.indexOf(':');
        String ticker = info.substring(0,index);
        double numberOfShares = Double.parseDouble(info.substring(index+1));
        companyInfoReq(sectionList,list,ticker,numberOfShares,size,flag);

    }

    private void companyInfoReq(List<Section> sectionList,List<StockItem> list,String ticker, double numberOfShares,int size,int flag){
        String companyInfoURL = BASEURL1+"companyInfo/"+ticker;

        JsonObjectRequest companyInfoRequest = new JsonObjectRequest
                (Request.Method.GET, companyInfoURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("CompanyInfoSuccess", response.toString());
                            String companyName = response.getString("name");
                            companyPriceReq(sectionList,list,ticker,numberOfShares,companyName,size,flag);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("CompanyInfoError",error.toString());
                        error.printStackTrace();
                    }
                });

        companyInfoRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(companyInfoRequest);
    }

    private void companyPriceReq(List<Section> sectionList,List<StockItem> list, String ticker,double numberOfShares,String companyName,int size,int flag){
        String companyLatestPriceURL = BASEURL1 + "companyLatestPrice/"+ticker;
        JsonArrayRequest companyLatestPriceRequest = new JsonArrayRequest
                (Request.Method.GET, companyLatestPriceURL, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            Log.d("CompanyPriceSuccess", obj.toString());
                            double closePrice = 0.0;
                            double change = 0.0;

                            if(!obj.get("last").equals(null)){
                                closePrice = obj.getDouble("last");
                            }

                            if (!obj.get("prevClose").equals(null)) {
                               change = obj.getDouble("last") - obj.getDouble("prevClose");
                               if (flag == 0) {
                                   netWorth += obj.getDouble("last") * numberOfShares;
                               }

                            }
                            list.add(new StockItem(ticker,closePrice,companyName,change,numberOfShares));
                            if (list.size() == size) {
                                mainRecyclerView = findViewById(R.id.mainRecyclerView);
                                if(flag == 0) {
//                                    Log.d("portfolio", list.toString());
                                    sectionList.add(new Section("PORTFOLIO", netWorth,list));
//                                    Log.d("Net Worth", String.valueOf(netWorth));

                                    if (sectionList.size() == 2) {
                                        if (sectionList.get(0).getSectionName().equals("FAVOURITES")){
                                            Section tmp = sectionList.get(0);
                                            sectionList.set(0,sectionList.get(1));
                                            sectionList.set(1,tmp);
                                        }
                                    }

                                    MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                                    mainRecyclerView.setAdapter(mainRecyclerAdapter);
                                }else {
//                                    Log.d("favourites", list.toString());
                                    sectionList.add(new Section("FAVOURITES", 0,list));
//                                    Log.d(TAG, "SectionList:"+sectionList.toString());
                                    if (sectionList.size() == 2 && sectionList.get(0).getSectionName().equals("FAVOURITES")) {
                                        Section tmp = sectionList.get(0);
                                        sectionList.set(0,sectionList.get(1));
                                        sectionList.set(1,tmp);
                                    }
                                    MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                                    mainRecyclerView.setAdapter(mainRecyclerAdapter);

                                }

                                spinner.setVisibility(View.GONE);
                                fetchingTextView.setVisibility(View.GONE);
                                currentDateView.setVisibility(View.VISIBLE);
                                mainRecyclerView.setVisibility(View.VISIBLE);
                                tiingoTextView.setVisibility(View.VISIBLE);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("LatestPriceError",error.toString());
                        error.printStackTrace();
                    }
                });

        companyLatestPriceRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(companyLatestPriceRequest);
    }

}