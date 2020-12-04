package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.log;
import static java.lang.Math.round;

public class DisplayMessageActivity extends AppCompatActivity {
    private static final String TAG = "DisplayMessage:";
    private static final String BASEURL1 = "https://glass-memento-289318.wl.r.appspot.com/";
    private static final String BASEURL2 = "https://csci571-xinangli-hw8-nodejs.azurewebsites.net/";

    private String ticker;
    private String companyName;
    private double balance;
    private double currentPrice = 0.0;
    private double sharesOwned = 0.0;
    private Map<String,Double> portfolioMap;
    private Map<String,Double> favouriteMap;
    private boolean isFavourite;
    private boolean isShowMore;

    MenuItem favouriteItem;
    MenuItem nonFavouriteItem;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    WebView webView;
    TextView nameView;
    TextView tickerView;
    TextView closePriceView;
    TextView changeView;

    TextView shareOwnedView;
    TextView marketValueView;
    TextView currentPriceView;
    TextView lowPriceView;
    TextView bidPriceView;
    TextView openPriceView;
    TextView midPriceView;
    TextView highPriceView;
    TextView volumePriceView;
    TextView aboutView;
    TextView showView;

    //4,5,6,textView16
    TextView portFolioTextView;
    TextView statsTextView;
    TextView aboutTextView;
    TextView newsTextView;
    TextView fetchingTextView;

    RecyclerView newsRecyclerView;
    Button tradeButton;
    ProgressBar spinner;

    private List<NewsItem> newsList;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">"+ getString(R.string.app_name)+"</font>"));

        Intent intent = getIntent();
        this.ticker = intent.getStringExtra(MainActivity.EXTRA_MESSAGE).toUpperCase();
        tickerView = findViewById(R.id.companyTickerView);
        tickerView.setText(ticker);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String portfolioString = pref.getString("portfolio","");
        String favouriteString = pref.getString("favourites","");

        balance = Double.parseDouble(pref.getString("balance","0.0"));
        Log.d(TAG, "Balance: "+balance);
        Log.d(TAG, "Portfolio: "+portfolioString);

        portfolioMap = stringToMap(portfolioString);
        favouriteMap = stringToMap(favouriteString);
        if (favouriteMap.containsKey(ticker)){
            isFavourite = true;
        }else{
            isFavourite = false;
        }
        isShowMore = false;

        editor = pref.edit();

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/charts.html?ticker="+ticker);

        newsList = new ArrayList<>();

        spinner = findViewById(R.id.progressBar1);
        nameView = findViewById(R.id.companyNameView);
        closePriceView = findViewById(R.id.companyClosePriceView);
        changeView = findViewById(R.id.companyChangeView);

        shareOwnedView = findViewById(R.id.shareOwnedTextView);
        marketValueView = findViewById(R.id.marketValueTextView);

        tradeButton = (Button) findViewById(R.id.tradeButton);
        currentPriceView = findViewById(R.id.currentPriceView);
        lowPriceView = findViewById(R.id.lowPriceView);
        bidPriceView = findViewById(R.id.bidPriceView);
        openPriceView = findViewById(R.id.openPriceView);
        midPriceView = findViewById(R.id.midPriceView);
        highPriceView = findViewById(R.id.highPriceView);
        volumePriceView = findViewById(R.id.volumePriceView);
        aboutView = findViewById(R.id.aboutView);
        showView = findViewById(R.id.showTextView);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setNestedScrollingEnabled(false);
        portFolioTextView = findViewById(R.id.textView4);
        statsTextView = findViewById(R.id.textView5);
        aboutTextView = findViewById(R.id.textView6);
        newsTextView = findViewById(R.id.textView16);
        fetchingTextView = findViewById(R.id.fetchingTextView);

        hideAll();

        httpRequest(ticker);

        showView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowMore){
                    showView.setText("Show Less");
                    aboutView.setMaxLines(100);
                    isShowMore = true;
                }else {
                    isShowMore = false;
                    aboutView.setMaxLines(2);
                    showView.setText("Show More...");
                }
            }
        });


        // add button listener
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

//                // custom dialog
                final Dialog dialog = new Dialog(DisplayMessageActivity.this);
                dialog.setContentView(R.layout.trade_stock);


//                // set the custom dialog components - text, image and button
                  TextView titleText =  dialog.findViewById(R.id.tradeTitleTextView);
                  EditText inputView = dialog.findViewById(R.id.numberOfSharesToBuyAndSell);
                  TextView totalValueView = dialog.findViewById(R.id.totalValueView);

                  totalValueView.setText("0 x $"+currentPrice+"/share = $0.00");

                  inputView.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {

                        }

                        public void beforeTextChanged(CharSequence s, int start,int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                            String input = String.valueOf(s);
                            String output ="0 x $"+currentPrice+"/share = $0.00";
                            if (isNumeric(input)){
                                double number = Double.parseDouble(input);
                                output = input + " x $"+ currentPrice+"/share = $"+ number*currentPrice;
                            }
                            totalValueView.setText(output);
                        }
                  });


                  TextView balanceView = dialog.findViewById(R.id.balanceView);
                  balanceView.setText("$"+String.format("%.2f",balance)+" available to buy "+ticker);

                  titleText.setText("Trade "+companyName+" shares");


                Button sellButton = dialog.findViewById(R.id.sellButton);
                Button buyButton = dialog.findViewById(R.id.buyButton);


                buyButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        String input = inputView.getText().toString();
                        if (isNumeric(input)){
                            double numberOfshares = Double.parseDouble(input);
                            if(numberOfshares <= 0) {
                                //‘Cannot buy less than 0 shares’
                                Toast.makeText(DisplayMessageActivity.this, "Cannot buy less than 0 shares", Toast.LENGTH_LONG).show();
                            }else{
                                double totalValue = numberOfshares * currentPrice;
                                if (totalValue > balance){
                                    //‘Not enough money to buy’
                                    Toast.makeText(DisplayMessageActivity.this, "Not enough money to buy", Toast.LENGTH_LONG).show();
                                }else {
                                    balance -= totalValue;
                                    sharesOwned += numberOfshares;
                                    Log.d(TAG, "SharesOwenedAfterBuy: "+ sharesOwned);
                                    portfolioMap.put(ticker,sharesOwned);
                                    String newString = mapToString(portfolioMap);
                                    editor.putString("portfolio",newString);
                                    editor.putString("balance",String.valueOf(balance));

                                    if(favouriteMap.containsKey(ticker)) {
                                        favouriteMap.put(ticker,sharesOwned);
                                        String newFavString = mapToString(favouriteMap);
                                        Log.d(TAG, "Favourites after buy: " + newFavString );
                                        editor.putString("favourites",newFavString);
                                    }

                                    Log.d(TAG, "Portfolio after buy: " + newString);
                                    Log.d(TAG, "Balance after buy: "+ balance);
                                    editor.commit();

                                    Dialog transactionSuccess  = new Dialog(DisplayMessageActivity.this);
                                    transactionSuccess.setContentView(R.layout.transaction_success);
                                    String message = "You have successfully bought "+numberOfshares+" shares of "+ticker;
                                    TextView transactionInfo = transactionSuccess.findViewById(R.id.transactionMeassageTextView);
                                    transactionInfo.setText(message);
                                    TextView doneButton = transactionSuccess.findViewById(R.id.doneButton);
                                    doneButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            transactionSuccess.dismiss();
                                        }
                                    });
                                    dialog.dismiss();
                                    transactionSuccess.show();

                                    shareOwnedView.setText("Share owned: "+String.format("%.2f",sharesOwned));
                                    marketValueView.setText("Market Value: $" + String.format("%.2f",currentPrice*portfolioMap.get(ticker)));
                                }
                            }

                        }else{
                            //‘Please enter valid amount’
                            Toast.makeText(DisplayMessageActivity.this, "Please enter valid amount", Toast.LENGTH_LONG).show();
                        }


                    }
                });
                sellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String input = inputView.getText().toString();
                        if (isNumeric(input)) {
                            double numberOfshares = Double.parseDouble(input);
                            if (numberOfshares <= 0){
                                Toast.makeText(DisplayMessageActivity.this, "Cannot sell less than 0 shares", Toast.LENGTH_LONG).show();
                            }else if (numberOfshares > sharesOwned){
                                Toast.makeText(DisplayMessageActivity.this, "Not enough shares to sell", Toast.LENGTH_LONG).show();
                            }else {

                                balance += numberOfshares*currentPrice;
                                sharesOwned -= numberOfshares;
                                Log.d(TAG, "SharesOwned: " + sharesOwned);
                                if (sharesOwned == 0.0){
                                    portfolioMap.remove(ticker);
                                    shareOwnedView.setText("You have 0 shares of " +ticker+".");
                                    marketValueView.setText("Start trading!");
                                }else {
                                    portfolioMap.put(ticker, sharesOwned);
                                    shareOwnedView.setText("Share owned: "+String.format("%.2f",sharesOwned));
                                    marketValueView.setText("Market Value: $" + String.format("%.2f",currentPrice* sharesOwned));
                                }
                                String newString = mapToString(portfolioMap);
                                editor.putString("portfolio",newString);
                                editor.putString("balance",String.valueOf(balance));

                                if(favouriteMap.containsKey(ticker)) {
                                    favouriteMap.put(ticker,sharesOwned);
                                    String newFavString = mapToString(favouriteMap);
                                    Log.d(TAG, "Favourites after sell: " + newFavString );
                                    editor.putString("favourites",newFavString);
                                }
                                Log.d(TAG, "Portfolio after sell: " + newString);
                                Log.d(TAG, "Balance after sell: "+ balance);
                                editor.commit();

                                Dialog transactionSuccess  = new Dialog(DisplayMessageActivity.this);
                                transactionSuccess.setContentView(R.layout.transaction_success);
                                String message = "You have successfully sold "+numberOfshares+" shares of "+ticker;
                                TextView transactionInfo = transactionSuccess.findViewById(R.id.transactionMeassageTextView);
                                transactionInfo.setText(message);
                                TextView doneButton = transactionSuccess.findViewById(R.id.doneButton);
                                doneButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        transactionSuccess.dismiss();
                                    }
                                });
                                dialog.dismiss();
                                transactionSuccess.show();

                            }
                        }else {
                            Toast.makeText(DisplayMessageActivity.this, "Please enter valid amount", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                dialog.show();
            }
        });


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu,menu);
        favouriteItem = menu.findItem(R.id.action_favorite);
        nonFavouriteItem = menu.findItem(R.id.action_not_favorite);

        if (isFavourite){
            favouriteItem.setVisible(true);

        }else {
            nonFavouriteItem.setVisible(true);
        }



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                favouriteMap.remove(ticker);
                Log.d("Favourites after remove", mapToString(favouriteMap));
                editor.putString("favourites",mapToString(favouriteMap));
                editor.commit();
                favouriteItem.setVisible(false);
                nonFavouriteItem.setVisible(true);
                Toast.makeText(DisplayMessageActivity.this, ticker+" was removed from favourites", Toast.LENGTH_LONG).show();
                return true;

            case R.id.action_not_favorite:
                favouriteMap.put(ticker,sharesOwned);

                Log.d("Favourites after add", mapToString(favouriteMap));

                editor.putString("favourites",mapToString(favouriteMap));
                editor.commit();
                nonFavouriteItem.setVisible(false);
                favouriteItem.setVisible(true);
                Toast.makeText(DisplayMessageActivity.this, ticker+" was added to favourites", Toast.LENGTH_LONG).show();
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    public void httpRequest(String ticker){
        RequestQueue queue = Volley.newRequestQueue(this);
        String companyInfoURL = BASEURL1+ "companyInfo/"+ticker;
        String companyLatestPriceURL = BASEURL1+ "companyLatestPrice/"+ticker;
        String newsURL = BASEURL1+ "news/"+ticker;

        JsonObjectRequest companyInfoRequest = new JsonObjectRequest
                (Request.Method.GET, companyInfoURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("CompanyInfoSuccess", response.toString());
                            nameView.setText(response.getString("name"));
                            companyName = response.getString("name");
                            aboutView.setText(response.getString("description"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("CompanyInfoError",error.toString());
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

        JsonArrayRequest companyLatestPriceRequest = new JsonArrayRequest
                (Request.Method.GET, companyLatestPriceURL, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            Log.d("LatestPriceSuccess", obj.toString());

                            currentPrice = 0.0;
                            if(!obj.get("last").equals(null)){
                                currentPrice = obj.getDouble("last");
                            }
                            Log.d("LastPrice", String.valueOf(currentPrice));
                            closePriceView.setText("$"+currentPrice);
                            currentPriceView.setText("Current Price:"+currentPrice);

                            if (portfolioMap.containsKey(ticker)){
                                shareOwnedView.setText("Share owned: "+String.valueOf(portfolioMap.get(ticker)));
                                sharesOwned = portfolioMap.get(ticker);
                                Log.d(TAG, "SharesOwned: " + sharesOwned);
                                marketValueView.setText("Market Value: $" + String.format("%.2f",currentPrice*portfolioMap.get(ticker)));
                            }else {
                                sharesOwned = 0.0;
                                shareOwnedView.setText("You have 0 shares of " +ticker+".");
                                marketValueView.setText("Start trading!");
                            }

                            if (!obj.get("prevClose").equals(null)) {
                                double change = obj.getDouble("last") - obj.getDouble("prevClose");
                                if (change >= 0){
                                    changeView.setText("$"+String.format("%.2f", change));
                                    changeView.setTextColor(Color.parseColor("#4CAF50"));
                                }else {
                                    changeView.setText("-$"+String.format("%.2f", change));
                                    changeView.setTextColor(Color.parseColor("#AC1105"));
                                }

                            }else {
                                changeView.setText(String.format("%.2f", 0));
                            }

                            String openPrice = "0.0";
                            if(!obj.get("open").equals(null)){
                                openPrice = String.valueOf(obj.getDouble("open"));
                            }
                            openPriceView.setText("OpenPrice: "+openPrice);

                            String high = "0.0";

                            if (!obj.get("high").equals(null)) {
                                high = String.valueOf(obj.getDouble("high"));
                            }
                            highPriceView.setText("High: "+ String.valueOf(high));

                            Double volume = 0.0;
                            if (!obj.get("volume").equals(null)) {
                                volume = obj.getDouble("volume");
                            }
                            volumePriceView.setText("Volume: "+ String.format("%.2f", volume));

                            String low = "0.0";

                            if(!obj.get("low").equals(null)){
                                low = String.valueOf(obj.getDouble("low"));
                            }
                            lowPriceView.setText("Low: "+low);


                            String bidPrice = "0.0";
                            if (!obj.get("bidPrice").equals(null)) {
                                bidPrice = String.valueOf(obj.getDouble("bidPrice"));
                            }
                            bidPriceView.setText("Bid Price: " + bidPrice);

                            String midPrice = "0.0";
                            if (!obj.get("mid").equals(null)) {
                                midPrice = String.valueOf(obj.getDouble("midPrice"));
                            }
                            midPriceView.setText("Mid: " + midPrice);



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("LatestPriceError",error.toString());
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

        JsonObjectRequest newsRequest = new JsonObjectRequest
                (Request.Method.GET, newsURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("newsSuccess", response.toString());
                            JSONArray newsArray = response.getJSONArray("articles");
                            Log.d("newsSuccess", newsArray.getJSONObject(0).getString("title"));
                            for (int i = 0; i < newsArray.length(); i++) {
                                JSONObject entry = newsArray.getJSONObject(i);
                                String title = entry.getString("title");
                                String url = entry.getString("url");
                                String date = entry.getString("publishedAt");
                                String source = entry.getJSONObject("source").getString("name");
                                String imageUrl = entry.getString("urlToImage");

                                newsList.add(new NewsItem(title,url,date,source,imageUrl));

                                if (i == newsArray.length()-1){
                                    Log.d("newsSuccess", newsList.toString());

                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(newsRecyclerView.getContext());
                                    newsRecyclerView.setLayoutManager(layoutManager);
                                    NewsRecyclerAdapter newsRecyclerAdapter = new NewsRecyclerAdapter(newsList);
                                    newsRecyclerView.setAdapter(newsRecyclerAdapter);
                                    showAll();
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("NewsError",error.toString());
                    }
                });

        newsRequest.setRetryPolicy(new RetryPolicy() {
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

        queue.add(newsRequest);



    }

    private Map<String,Double> stringToMap(String input){
        Map<String,Double> map = new HashMap<>();
        String[] array = input.split(",");
        for (String info:array) {
            String[] entry = info.split(":");
            map.put(entry[0],Double.valueOf(entry[1]));
        }
        return map;
    }

    private  String mapToString(Map<String, Double> input){
        String res = "";
        for(Map.Entry<String,Double> entry:input.entrySet()){
            res += entry.getKey() +":"+String.valueOf(entry.getValue())+",";
        }
        return res;
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    private void showAll(){
        spinner.setVisibility(View.GONE);
        fetchingTextView.setVisibility(View.GONE);

        webView.setVisibility(View.VISIBLE);
        nameView.setVisibility(View.VISIBLE);
        tickerView.setVisibility(View.VISIBLE);
        closePriceView.setVisibility(View.VISIBLE);
        changeView.setVisibility(View.VISIBLE);
        shareOwnedView.setVisibility(View.VISIBLE);
        marketValueView.setVisibility(View.VISIBLE);
        currentPriceView.setVisibility(View.VISIBLE);
        lowPriceView.setVisibility(View.VISIBLE);
        bidPriceView.setVisibility(View.VISIBLE);
        openPriceView.setVisibility(View.VISIBLE);
        midPriceView.setVisibility(View.VISIBLE);
        highPriceView.setVisibility(View.VISIBLE);
        volumePriceView.setVisibility(View.VISIBLE);
        aboutView.setVisibility(View.VISIBLE);
        showView.setVisibility(View.VISIBLE);

        newsRecyclerView.setVisibility(View.VISIBLE);
        tradeButton.setVisibility(View.VISIBLE);
        portFolioTextView.setVisibility(View.VISIBLE);;
        statsTextView.setVisibility(View.VISIBLE);
        aboutTextView.setVisibility(View.VISIBLE);
        newsTextView.setVisibility(View.VISIBLE);

    }

    private void hideAll(){
        spinner.setVisibility(View.VISIBLE);
        fetchingTextView.setVisibility(View.VISIBLE);

        webView.setVisibility(View.GONE);
        nameView.setVisibility(View.GONE);
        tickerView.setVisibility(View.GONE);
        closePriceView.setVisibility(View.GONE);
        changeView.setVisibility(View.GONE);
        shareOwnedView.setVisibility(View.GONE);
        marketValueView.setVisibility(View.GONE);
        currentPriceView.setVisibility(View.GONE);
        lowPriceView.setVisibility(View.GONE);
        bidPriceView.setVisibility(View.GONE);
        openPriceView.setVisibility(View.GONE);
        midPriceView.setVisibility(View.GONE);
        highPriceView.setVisibility(View.GONE);
        volumePriceView.setVisibility(View.GONE);
        aboutView.setVisibility(View.GONE);
        showView.setVisibility(View.GONE);

        newsRecyclerView.setVisibility(View.GONE);
        tradeButton.setVisibility(View.GONE);
        portFolioTextView.setVisibility(View.GONE);;
        statsTextView.setVisibility(View.GONE);
        aboutTextView.setVisibility(View.GONE);
        newsTextView.setVisibility(View.GONE);
    }


}