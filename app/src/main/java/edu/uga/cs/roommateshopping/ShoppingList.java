package edu.uga.cs.roommateshopping;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList implements Parcelable {

    private String name;
    private ArrayList<String> unpurchasedItems;
    private ArrayList<String> shoppingCart;
    private ArrayList<PurchasedItems> purchasedItems;
    private String ownerID;
    private ArrayList<String> roommates;


    public ShoppingList() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingList.class)
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnpurchasedItems(ArrayList<String> unpurchasedItems) {
        this.unpurchasedItems = unpurchasedItems;
    }
    public void setShoppingCart(ArrayList<String> shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
    public void setPurchasedItems(ArrayList<PurchasedItems> purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setRoommatesID(ArrayList<String> roommates) {
        this.roommates = roommates;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getUnpurchasedItems() {
        return unpurchasedItems;
    }

    public ArrayList<String> getShoppingCart() {
        return shoppingCart;
    }
    public ArrayList<PurchasedItems> getPurchasedItems() {
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
        in.readList(shoppingCart, String.class.getClassLoader());
        in.readList(purchasedItems, PurchasedItems.class.getClassLoader());
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
        dest.writeList(shoppingCart);
        dest.writeTypedList(purchasedItems);
        dest.writeString(ownerID);
        dest.writeList(roommates);
    }
}
