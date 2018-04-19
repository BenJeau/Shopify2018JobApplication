package com.example.benoit.shopify_summer2018mobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private final int CARD_VIEW_MARGINS_DP = 10;
    private final int COUNTDOWN_INTERVAL = 1000;
    private final int COUNTDOWN_TIME = 1000;
    private final int MAIN_GRAY = Color.rgb(21, 21, 21);
    private final int VIBRATION_LENGTH = 50;
    private final String BASE_URL = "https://shopicruit.myshopify.com";
    private final String DATA_SERIALIZABLE = "products";
    private AppBarLayout appBarLayout;
    private ArrayList<Products> data;
    private Call<JSONResponseMain> call;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private DataAdapter adapter, ad;
    private final String PROGRESS_DIALOG_MESSAGE = "Loading data. Please wait...";
    private FloatingActionButton floatingActionButton;
    private InputMethodManager inputMethodManager;
    private int alpha, red, green, blue, navigationBarHeight, resourceId, cardViewMarginsPixels;
    private JSONResponseMain jsonResponseMain;
    private LayoutAnimationController layoutAnimationController;
    private List<Products> filteredProductList;
    private MenuItem menuItem;
    private ProgressDialog dialog;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RequestInterfaceMain requestInterfaceMain;
    private Retrofit retrofit;
    private SearchView searchView;
    private String lowerCaseQuery;
    private Toolbar toolbar;
    private Vibrator vibrator;

    public int darkenColor(int color, float factor) {
        // Darkens the color specified, from a factor of 1F to 0F
        alpha = Color.alpha(color);
        red = Math.min(Math.round(Color.red(color) * factor), 255);
        green = Math.min(Math.round(Color.green(color) * factor), 255);
        blue = Math.min(Math.round(Color.blue(color) * factor), 255);
        return Color.argb(alpha, red, green, blue);
    }

    private List<Products> filter(List<Products> products, String query) {
        // Puts query to lower case letters for better comparision
        lowerCaseQuery = query.toLowerCase();

        filteredProductList = new ArrayList<>();

        // Loops through the products to see if the title or description contains a part of the query
        for (Products product : products) {
            if (product.getTitle().toLowerCase().contains(lowerCaseQuery) || product.getBody_html().toLowerCase().contains(lowerCaseQuery)) {
                filteredProductList.add(product);
            }
        }
        return filteredProductList;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Saves data on rotation (does not need to get information from servers again)
        outState.putParcelableArrayList(DATA_SERIALIZABLE, data);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();
        configureToolbars();

        // Initializes objects depending on System Services
        inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Checks if activity has already been initiated (for rotation)
        if (savedInstanceState != null) {
            // Restore data
            data = savedInstanceState.getParcelableArrayList(DATA_SERIALIZABLE);

            // Sets up RecyclerView
            recyclerViewSetUp();
            adapter.add(data);
        } else {
            // Restricts orientation of device to its original state
            // (to not interrupt the action of getting the data)
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            recyclerViewSetUp();
            loadJSON();

            // Hides RecyclerView and shows a progress dialog
            recyclerView.setAlpha(0);
            dialog = ProgressDialog.show(this, "", PROGRESS_DIALOG_MESSAGE, true);
        }

        // If the device is in portrait mode, add padding to bottom
        // to compensate the on screen navigation bar (if there is one)
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPaddingNavigationBar(recyclerView, CARD_VIEW_MARGINS_DP);
        }
    }

    public void setPaddingNavigationBar(View view, int marginsDP) {
        // Gets the height of the navigation bar in pixels
        navigationBarHeight = 0;
        resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // Converts DPs to pixels (margins)
        cardViewMarginsPixels = Math.round(marginsDP * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

        // Adds margins to the navigation bar height and set bottom padding to view
        navigationBarHeight += cardViewMarginsPixels;
        view.setPadding(0, 0, 0, navigationBarHeight);
    }

    private void runLayoutAnimation(RecyclerView recyclerView) {
        // Creates animation for RecyclerView
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(), R.anim.layout_animation_from_left);

        // Sets animation for RecyclerView
        recyclerView.setLayoutAnimation(layoutAnimationController);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public void setViews() {
        // Initializes Layout
        recyclerView = findViewById(R.id.list_recycler_view);

        // Initializes Application toolbars
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        // Initializes Buttons
        floatingActionButton = findViewById(R.id.fab);
    }

    public void configureToolbars() {
        // Sets the color to the navigation bar
        getWindow().setNavigationBarColor(darkenColor(MAIN_GRAY, 1F));

        // Sets toolbar
        setSupportActionBar(toolbar);

        // Sets CollapsingToolbarLayout animations
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.MainCollapsedAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.MainExpendedAppBar);

        // When the FloatingActionButton is pressed, it vibrates, collapses AppBar, hides
        // FloatingActionButton and shows the menu search icon to show the SearchView
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(VIBRATION_LENGTH);
                appBarLayout.setExpanded(Boolean.FALSE);
                floatingActionButton.hide();
                menuItem.setVisible(Boolean.TRUE);
                searchView.setIconified(Boolean.FALSE);
            }
        });
    }

    private void recyclerViewSetUp() {
        // Sets up RecyclerView and its LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DataAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadJSON() {
        // Uses Retrofit to get JSON, to create a requestInterface and to call that website
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();
        requestInterfaceMain = retrofit.create(RequestInterfaceMain.class);
        call = requestInterfaceMain.getJSON();
        call.enqueue(new Callback<JSONResponseMain>() {
            @Override
            public void onResponse(Call<JSONResponseMain> call, Response<JSONResponseMain> response) {
                // Gets data from JSON and adds it to the adapter
                jsonResponseMain = response.body();
                data = new ArrayList<>(Arrays.asList(jsonResponseMain.getProducts()));
                adapter.add(data);

                // Creates a countdown timer to let the data load in the RecyclerView
                new CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_INTERVAL) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        // Lets the device rotate, removes the dialog, shows the RecyclerView and runs the animation
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        dialog.dismiss();
                        recyclerView.setAlpha(1);
                        runLayoutAnimation(recyclerView);
                    }
                }.start();
            }

            @Override
            public void onFailure(Call<JSONResponseMain> call, Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get menu search icon and create a SearchView
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) menuItem.getActionView();

        // Listens when SearchView field is changing to filter the products
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                onQueryChange(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                onQueryChange(s);
                return false;
            }
        });

        // Listens when SearchView is closed with the 'X'
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Vibrates, hides the menu search icon, shows the FloatingActionButton,
                // expands the AppBar and closes the keyboard
                vibrator.vibrate(VIBRATION_LENGTH);
                menuItem.setVisible(Boolean.FALSE);
                floatingActionButton.show();
                appBarLayout.setExpanded(Boolean.TRUE);
                inputMethodManager.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void onQueryChange(String s) {
        filteredProductList = filter(data, s);
        adapter.replaceAll(filteredProductList);
        recyclerView.scrollToPosition(0);
    }
}
