package edu.uga.cs.roommateshopping;

import java.util.Date;

public class Purchase {
    String fullName;
    Date date;
    String items[];
    String price;


    public Purchase() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Purchase(String fullName, Date date, String items[], String price) {
        this.fullName = fullName;
        this.date = date;
        this.items = items;
        this.price= price;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }



}
