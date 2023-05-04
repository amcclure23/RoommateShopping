package edu.uga.cs.roommateshopping;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PurchasedItems implements Parcelable {
    String user;
    String date;
    ArrayList<String> items;
    double price;


    public PurchasedItems() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public PurchasedItems(String user, String date, ArrayList<String> items, double price) {
        this.user = user;
        this.date = date;
        this.items = items;
        this.price = price;
    }

    public String getUser() {
        return user;
    }

    public double getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<String> getItems() { return items; }

    public void setDate(String date) {
        this.date = date;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setUser(String user) {
        this.user = user;
    }

    // implement the Parcelable interface
    protected PurchasedItems(Parcel in) {
        in.readList(items, String.class.getClassLoader());
        date = in.readString();
        user = in.readString();
        price = in.readDouble();
    }

    public static final Parcelable.Creator<ShoppingList> CREATOR = new Parcelable.Creator<ShoppingList>() {
        @Override
        public ShoppingList createFromParcel(Parcel in) {
            return new ShoppingList(in);
        }

        @Override
        public ShoppingList[] newArray(int size) {
            return new ShoppingList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(items);
        dest.writeString(date);
        dest.writeString(user);
        dest.writeDouble(price);
    }
}
