package com.example.benoit.shopify_summer2018mobile;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ProductInfo implements Parcelable {

    private String vendor, tags;
    private List<Variants> variants;

    public String getVendor() {
        return vendor;
    }

    public String getTags() {
        return tags;
    }

    public List<Variants> getVariants() {
        return variants;
    }

    // Start of implementation of Parcelable
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ProductInfo> CREATOR = new Parcelable.Creator<ProductInfo>() {
        public ProductInfo createFromParcel(Parcel in) {
            return new ProductInfo(in);
        }

        public ProductInfo[] newArray(int size) {
            return new ProductInfo[size];
        }
    };

    // Writes ProductInfo's object to parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(vendor);
        out.writeString(tags);
        out.writeList(variants);
    }

    // Retrieve ProductInfo's object from parcel
    private ProductInfo(Parcel in) {
        vendor = in.readString();
        tags = in.readString();
        in.readTypedList(variants, Variants.CREATOR);
    }
}

class Variants implements Parcelable {
    private String price, title, inventory_quantity, weight_unit, weight;

    public String getPrice() {
        return price;
    }

    public String getInventory_quantity() {
        return inventory_quantity;
    }

    public String getWeight() {
        return weight;
    }

    public String getWeight_unit() {
        return weight_unit;
    }

    public String getTitle() {
        return title;
    }

    // Start of implementation of Parcelable
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Variants> CREATOR
            = new Parcelable.Creator<Variants>() {
        public Variants createFromParcel(Parcel in) {
            return new Variants(in);
        }

        public Variants[] newArray(int size) {
            return new Variants[size];
        }
    };

    // Writes Variants's object to parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(price);
        out.writeString(title);
        out.writeString(inventory_quantity);
        out.writeString(weight_unit);
        out.writeString(weight);
    }

    // Retrieve Variants's object from parcel
    private Variants(Parcel in) {
        price = in.readString();
        title = in.readString();
        inventory_quantity = in.readString();
        weight_unit = in.readString();
        weight = in.readString();
    }
}