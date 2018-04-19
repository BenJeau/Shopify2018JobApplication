package com.example.benoit.shopify_summer2018mobile;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductInfoActivity extends AppCompatActivity {

    private final int ANIMATION_DELAY_INTERVAL = 100;
    private final int CARD_VIEW_MARGINS_DP = 10;
    private final int NAVIGATION_BAR_ANIMATION_TIME = 700;
    private final int VIBRATION_LENGTH = 50;
    private final int MAIN_GRAY = Color.rgb(21, 21, 21);
    private final String BASE_URL = "https://shopicruit.myshopify.com";
    private final String DATA_PARCELABLE = "productInfo";
    private ActivityManager.TaskDescription taskDescription;
    private Animation animation1, animation2, animation3, animation4, animation5, animation6, animation7, animation8;
    private AppBarLayout appBarLayout;
    private Bitmap bitmap;
    private Call<JSONResponseProductInfo> call;
    private CardView viewCard;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Drawable drawable;
    private HttpURLConnection httpURLConnection;
    private ImageView imageProduct, imageTags, imageVendor, imageVariants, imagePrice, imageWeight, imageQuantity;
    private InputStream inputStream;
    private int alpha, red, green, blue, darkerMainColor, mainColor, navigationBarHeight, resourceId, cardViewMarginsPixels;
    private JSONResponseProductInfo jsonResponseProductInfo;
    private ProductInfo data;
    private RelativeLayout viewTags, viewVendor, viewVariants, viewPrice, viewQuantity, viewWeight, viewDescription;
    private RequestInterfaceProductInfo requestInterfaceProductInfo;
    private Retrofit retrofit;
    private String id, productTitle, productDescription, imageUrl, priceText, weightText;
    private TextView description, tags, vendor, variants, price, weight, quantity;
    private Toolbar toolbar;
    private URL url;
    private ValueAnimator colorAnimation;
    private Vibrator vibrator;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Saves product information on rotation (does not need to get information from servers again)
        outState.putParcelable(DATA_PARCELABLE, data);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If back button on toolbar is pressed, vibrate and go to the previous activity
        if (item.getItemId() == item.getItemId()){
            vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATION_LENGTH);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);
        setViews();
        setAnimations();
        getIntentInformation();
        configureToolbars();

        // Creates colour palette
        bitmap = getBitmapFromURL(imageUrl);
        createPaletteAsync(bitmap);

        // Sets information from previous activity
        description.setText(productDescription);
        Picasso.with(getApplicationContext()).load(imageUrl).into(imageProduct);

        // Checks if activity has already been initiated (for rotation)
        if (savedInstanceState != null) {
            // Restores data
            data = savedInstanceState.getParcelable(DATA_PARCELABLE);
            setData();
        } else {// Restricts orientation of device to its original state
            // (to not interrupt the action of getting the data)
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            loadJSON();
        }

        // If the device is in portrait mode, adds padding to bottom to compensate the
        // on screen navigation bar (if there is one, especially useful for small devices)
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPaddingNavigationBar(viewCard, CARD_VIEW_MARGINS_DP);
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

    public void configureToolbars() {
        // Sets toolbar, back button and empty title
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");

        // Sets CollapsingToolbarLayout title and its animations
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setTitle(productTitle);
    }

    public void getIntentInformation() {
        // Gets intent information
        id = getIntent().getExtras().getString("id");
        productTitle = getIntent().getExtras().getString("title");
        productDescription = getIntent().getExtras().getString("body_html");
        imageUrl = getIntent().getExtras().getString("image");
    }

    public void setViews() {
        // Initializes TextViews
        description = findViewById(R.id.text_description);
        tags = findViewById(R.id.text_tags);
        vendor = findViewById(R.id.text_vendor);
        variants = findViewById(R.id.text_variants);
        price = findViewById(R.id.text_price);
        quantity = findViewById(R.id.text_quantity);
        weight = findViewById(R.id.text_weight);

        // Initializes ImageViews
        imageProduct = findViewById(R.id.image_product_info);
        imageTags = findViewById(R.id.image_tags);
        imageVendor = findViewById(R.id.image_vendor);
        imageVariants = findViewById(R.id.image_variants);
        imagePrice = findViewById(R.id.image_price);
        imageQuantity = findViewById(R.id.image_quantity);
        imageWeight = findViewById(R.id.image_weight);

        // Initializes Layouts
        viewTags = findViewById(R.id.view_tags);
        viewVendor = findViewById(R.id.view_vendor);
        viewVariants = findViewById(R.id.view_variants);
        viewPrice = findViewById(R.id.view_price);
        viewQuantity = findViewById(R.id.view_quantity);
        viewWeight = findViewById(R.id.view_weight);
        viewDescription = findViewById(R.id.view_description);
        viewCard = findViewById(R.id.card_view_product_info);

        // Initializes Application toolbars
        toolbar = findViewById(R.id.user_toolbar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        appBarLayout = findViewById(R.id.app_bar);
    }

    public void setAnimations() {
        // Initializes animations to the animation from left
        animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation5 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation6 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation7 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);
        animation8 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_from_left);

        // Sets delay between animations
        animation2.setStartOffset(ANIMATION_DELAY_INTERVAL);
        animation3.setStartOffset(ANIMATION_DELAY_INTERVAL * 2);
        animation4.setStartOffset(ANIMATION_DELAY_INTERVAL * 3);
        animation5.setStartOffset(ANIMATION_DELAY_INTERVAL * 4);
        animation6.setStartOffset(ANIMATION_DELAY_INTERVAL * 5);
        animation7.setStartOffset(ANIMATION_DELAY_INTERVAL * 6);
        animation8.setStartOffset(ANIMATION_DELAY_INTERVAL * 7);

        // Sets animations to items
        toolbar.startAnimation(animation1);
        viewDescription.startAnimation(animation2);
        viewTags.startAnimation(animation3);
        viewVendor.startAnimation(animation4);
        viewVariants.startAnimation(animation5);
        viewPrice.startAnimation(animation6);
        viewQuantity.startAnimation(animation7);
        viewWeight.startAnimation(animation8);
    }

    public Bitmap getBitmapFromURL(String urlSrc) {
        try {
            // Connects to URL, establishes a connection and creates a Bitmap
            url = new URL(urlSrc);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    public int darkenColor(int color, float factor) {
        // Darkens the color specified, from a factor of 1F to 0F
        alpha = Color.alpha(color);
        red = Math.min(Math.round(Color.red(color) * factor), 255);
        green = Math.min(Math.round(Color.green(color) * factor), 255);
        blue = Math.min(Math.round(Color.blue(color) * factor), 255);
        return Color.argb(alpha, red, green, blue);
    }

    public void createPaletteAsync(final Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(final Palette p) {
                // Defines main colors
                mainColor = p.getDarkVibrantColor(0x000000);
                darkerMainColor = darkenColor(mainColor, 0.6F);

                // Sets the color to the icons, description, toolbar and navigation bar
                imageTags.setColorFilter(mainColor);
                imageVendor.setColorFilter(mainColor);
                imageVariants.setColorFilter(mainColor);
                imagePrice.setColorFilter(mainColor);
                imageWeight.setColorFilter(mainColor);
                imageQuantity.setColorFilter(mainColor);
                description.setTextColor(mainColor);
                getWindow().setNavigationBarColor(darkerMainColor);
                collapsingToolbarLayout.setBackgroundColor(mainColor);

                // Changes recent apps tab to the mainColor of the activity
                try {
                    drawable = getPackageManager().getApplicationIcon(getApplicationContext().getPackageName());
                    taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), ((BitmapDrawable) drawable).getBitmap(), mainColor);
                    setTaskDescription(taskDescription);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("Error", e.getMessage());
                }

                // Animates the change of color on the navigation bar on rotation (from darkenMainColor to mainGray and vice-versa)
                // by using a ValueAnimator and listening if the CollapsingToolbarLayout is collapsed or expanded
                colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), MAIN_GRAY, darkerMainColor);
                colorAnimation.setDuration(NAVIGATION_BAR_ANIMATION_TIME);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        int color = (int) animator.getAnimatedValue();
                        getWindow().setNavigationBarColor(color);
                    }
                });
                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    boolean isShow = false;
                    int scrollRange = -1;

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (scrollRange == -1) {
                            scrollRange = appBarLayout.getTotalScrollRange();
                        }
                        if (scrollRange + verticalOffset == 0) {
                            // CollapsingToolbarLayout is collapsed
                            colorAnimation.reverse();
                            isShow = true;
                        } else if (isShow) {
                            // CollapsingToolbarLayout is expanded
                            colorAnimation.start();
                            isShow = false;
                        }
                    }
                });
            }
        });
    }

    private void loadJSON() {
        // Uses Retrofit to get JSON, to create a requestInterface and to call that website
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();
        requestInterfaceProductInfo = retrofit.create(RequestInterfaceProductInfo.class);
        call = requestInterfaceProductInfo.getJSON(id);
        call.enqueue(new Callback<JSONResponseProductInfo>() {
            @Override
            public void onResponse(Call<JSONResponseProductInfo> call, Response<JSONResponseProductInfo> response) {
                // Gets data from JSON
                jsonResponseProductInfo = response.body();
                data = jsonResponseProductInfo.getProductInfo();
                setData();

                // Lets the device rotate
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }

            @Override
            public void onFailure(Call<JSONResponseProductInfo> call, Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    public void setData() {
        // Creates string variables for TextViews
        priceText = "$ " + data.getVariants().get(0).getPrice();
        weightText = data.getVariants().get(0).getWeight() + " " + data.getVariants().get(0).getWeight_unit();

        // Sets TextViews text
        vendor.setText(data.getVendor());
        tags.setText(data.getTags());
        variants.setText(data.getVariants().get(0).getTitle());
        price.setText(priceText);
        weight.setText(weightText);
        quantity.setText(data.getVariants().get(0).getInventory_quantity());
    }
}
