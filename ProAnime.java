package learn.draw;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import learn.draw.AnimePro.ListItemPro;
import learn.draw.AnimePro.MyAdapter;

public class ProAnime extends AppCompatActivity {
    private static final String URL_DATA = "https://www.learnforall.net/drawing/animepro/animepro.json";
    private RecyclerView recyclerView ;
    private RecyclerView.Adapter adapter ;
    private List<ListItemPro> listItems ;
    MyReceiver myReceiver ;
    private FrameLayout adcontainer;
    private AdView adView;
    private static final String ADUNIT_ID = "ca-app-pub-8225165625315528/9844089299";
    private InterstitialAd mInterstitialAd;
    Button button ;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private boolean isCanceled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvty_recently);

//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getString(R.string.mInterstitialAd).toString());
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());
//        mInterstitialAd.setAdListener(new AdListener() {
//
//            @Override
//            public void onAdClosed() {
//                // Load the next interstitial.
//                requestNewInterstitial();
//            }
//
//        });

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });


        adcontainer = findViewById(R.id.ad_view_container);
        adcontainer.post(new Runnable() {
            @Override
            public void run() {
                loadBanner();
            }
        });

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new  IntentFilter (ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver= new MyReceiver();
      //  registerReceiver(myReceiver,intentFilter);
        recyclerView = findViewById(R.id.recyclerView_rec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.LayoutManager mLayoutManager1 = new GridLayoutManager(this, 2);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setLayoutManager(mLayoutManager1);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        button = findViewById(R.id.button_refresh);
        listItems = new ArrayList<>();
        loadRecyclerViewData();
    }
    private  void  loadRecyclerViewData (){
        isCanceled = false;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        //  progressDialog.setMessage(getString(R.string.loading));
        //  progressDialog.show();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.informationload));
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
            // Set a click listener for progress dialog cancel button
            @Override
            public void onClick(DialogInterface dialog, int which){
                // dismiss the progress dialog
                progressDialog.dismiss();
                // Tell the system about cancellation
                isCanceled = true;
            }
        });
        progressDialog.show();

        // Set the progress status zero on each button click
        progressStatus = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressStatus < progressDialog.getMax()){
                    // If user's click the cancel button from progress dialog
                    if(isCanceled)
                    {
                        // Stop the operation/loop
                        break;
                    }
                    // Update the progress status
                    progressStatus +=1;

                    // Try to sleep the thread for 200 milliseconds
                    try{
                        Thread.sleep(200);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    // Update the progress bar
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Update the progress status
                            progressDialog.setProgress(progressStatus);
                            //  tv.setText(progressStatus+"");
                            // If task execution completed
                            if(progressStatus == progressDialog.getMax()){
                                // Dismiss/hide the progress dialog
                                progressDialog.dismiss();
                                // tv.setText("Operation completed.");
                            }
                        }
                    });
                }
            }
        }).start(); // Start the operation
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                URL_DATA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            JSONArray array = jsonObject.getJSONArray("recently");
                            for (int i = 0 ;  i<array.length(); i++ ){
                                JSONObject o = array.getJSONObject(i);
                                ListItemPro items  = new ListItemPro(
                                        o.getString("name"),
                                        o.getString("about"),
                                        o.getString("image"),
                                        o.getString("title")
                                );
                                listItems.add(items);
                            }

                            adapter = new MyAdapter(listItems , getApplicationContext());
                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext() , getString(R.string.JSONException), Toast.LENGTH_LONG).show();

                        View b = findViewById(R.id.button_refresh);
                        b.setVisibility(View.VISIBLE);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public void Refrch(View view) {
        deleteCache(this);
        finish();
        startActivity(getIntent());

    }


    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }

            }

        }
    }


    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }
//    private void requestNewInterstitial() {
//        AdRequest adRequest = new AdRequest.Builder()
//                .build();
//        mInterstitialAd.loadAd(adRequest);
//    }



    public  void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
    private void loadBanner() {
        adView = new AdView(this);
        adView.setAdUnitId(ADUNIT_ID);
        adcontainer.removeAllViews();
        adcontainer.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float density = displayMetrics.density;
        float adwidthpixels = adcontainer.getWidth();

        if (adwidthpixels == 0 ){
            adwidthpixels = displayMetrics.widthPixels;
        }

        int adWith = (int) (adwidthpixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWith);
    }
}