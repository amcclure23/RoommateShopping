package edu.uga.cs.roommateshopping;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ShoppingList implements Parcelable {

    private ArrayList<String> unpurchasedItems;
    private ArrayList<String> purchasedItems;
    private String ownerID;
    private ArrayList<String> roommates;


    public ShoppingList() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingList.class)
    }

    public void setUnpurchasedItems(ArrayList<String> unpurchasedItems) {
        this.unpurchasedItems = unpurchasedItems;
    }

    public void setPurchasedItems(ArrayList<String> purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setRoommatesID(ArrayList<String> roommates) {
        this.roommates = roommates;
    }

    public ArrayList<String> getUnpurchasedItems() {
        return unpurchasedItems;
    }

    public ArrayList<String> getPurchasedItems() {
        return purchasedItems;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public ArrayList<String> getRoommates() {
        return roommates;
    }

    // implement the Parcelable interface
    protected ShoppingList(Parcel in) {
        in.readList(unpurchasedItems, String.class.getClassLoader());
        in.readList(purchasedItems, String.class.getClassLoader());
        ownerID = in.readString();
        in.readList(roommates, String.class.getClassLoader());
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
        dest.writeList(unpurchasedItems);
        dest.writeList(purchasedItems);
        dest.writeString(ownerID);
        dest.writeList(roommates);
    }
}
