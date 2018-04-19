package com.example.benoit.shopify_summer2018mobile;

import android.os.Parcel;
import android.os.Parcelable;

public class Products implements Parcelable {
    private String body_html, title, id;
    private Image image;

    public String getBody_html() {
        return body_html;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image.getSrc();
    }

    // These two following functions are used to be able to compare Products objects
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Products products = (Products) o;

        return id != null ? id.equals(products.id) : products.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Start of implementation of Parcelable
    public int describeContents() {
        return 0;
    }

    // Writes ProductInfo's object to parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(body_html);
        out.writeString(title);
        out.writeString(id);
        out.writeString(image.getSrc());
    }

    public static final Parcelable.Creator<Products> CREATOR = new Parcelable.Creator<Products>() {
        public Products createFromParcel(Parcel in) {
            return new Products(in);
        }

        public Products[] newArray(int size) {
            return new Products[size];
        }
    };

    // Retrieve Products object from parcel
    private Products(Parcel in) {
        body_html = in.readString();
        title = in.readString();
        id = in.readString();
        image.setSrc(in.readString());
    }
}

class Image {
    private String src;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}