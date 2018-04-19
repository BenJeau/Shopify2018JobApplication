package com.example.benoit.shopify_summer2018mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private ActivityOptionsCompat activityOptionsCompat;
    private ArrayList<Products> pos1;
    private Intent intent;
    private Pair<View, String> pair;
    private Products product;
    private Vibrator vibrator;

    // Here, we're using a SortedList to ease the filtering of the elements in the RecyclerView
    // all while being able to have animations on the views update
    private final SortedList<Products> productsSortedList = new SortedList<>(Products.class, new SortedList.Callback<Products>() {
        @Override
        public int compare(Products a, Products b) {
            return a.getTitle().compareTo(b.getTitle());
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Products oldItem, Products newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Products item1, Products item2) {
            return item1.getId() == item2.getId();
        }
    });

    // The following 5 functions are for the manipulation of the SortedList
    public void add(Products product) {
        productsSortedList.add(product);
    }

    public void remove(Products product) {
        productsSortedList.remove(product);
    }

    public void add(List<Products> products) {
        productsSortedList.addAll(products);
    }

    public void remove(List<Products> products) {
        productsSortedList.beginBatchedUpdates();
        for (Products product : products) {
            productsSortedList.remove(product);
        }
        productsSortedList.endBatchedUpdates();
    }

    public void replaceAll(List<Products> products) {
        productsSortedList.beginBatchedUpdates();
        for (int i = productsSortedList.size() - 1; i >= 0; i--) {
            product = productsSortedList.get(i);
            if (!products.contains(product)) {
                int temp = productsSortedList.indexOf(product);
                productsSortedList.remove(product);

                // Updates the position of the children of RecyclerView when items are removed
                this.notifyItemRemoved(temp);
                this.notifyItemRangeChanged(temp, productsSortedList.size());
            }
        }
        pos1 = new ArrayList<>(products);
        productsSortedList.addAll(products);
        for (int i = 0; i < pos1.size(); i++){
            // Updates the position of the children of RecyclerView when items are moved
            this.notifyItemMoved(i, productsSortedList.indexOf(pos1.get(i)));
            this.notifyItemRangeChanged(i, productsSortedList.size());
        }
        productsSortedList.endBatchedUpdates();
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_children, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataAdapter.ViewHolder viewHolder, final int i) {
        // Sets the products title, description and image to every recycler card view
        viewHolder.text_title.setText(productsSortedList.get(i).getTitle());
        viewHolder.text_description.setText(productsSortedList.get(i).getBody_html());
        Picasso.with(viewHolder.itemView.getContext()).load(productsSortedList.get(i).getImage()).into(viewHolder.image_product);

        viewHolder.relative_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vibrates device when clicking on CardView
                vibrator = (Vibrator) viewHolder.itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(50);

                // Adds shared elements transition to the products image
                pair = Pair.create((View) viewHolder.image_product, viewHolder.image_product.getTransitionName());
                activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) viewHolder.itemView.getContext(), pair);

                // Puts some information in the intent for the next activity, less work
                intent = new Intent(viewHolder.itemView.getContext(), ProductInfoActivity.class);
                intent.putExtra("id", productsSortedList.get(i).getId());
                intent.putExtra("title", productsSortedList.get(i).getTitle());
                intent.putExtra("body_html", productsSortedList.get(i).getBody_html());
                intent.putExtra("image", productsSortedList.get(i).getImage());
                viewHolder.itemView.getContext().startActivity(intent, activityOptionsCompat.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsSortedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text_title, text_description;
        private ImageView image_product;
        private RelativeLayout relative_layout;

        public ViewHolder(View view) {
            super(view);

            // Initializes every view of each CardView
            text_title = view.findViewById(R.id.text_title);
            text_description = view.findViewById(R.id.test_description);
            image_product = view.findViewById(R.id.image_product);
            relative_layout = view.findViewById(R.id.linear_layout);
        }
    }


}