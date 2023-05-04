package edu.uga.cs.roommateshopping;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingCartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingCartFragment extends Fragment {


    private Button purchaseButton, deleteB,  doneB;
    private EditText price;
    private TableLayout itemlist;
    private int rowNum = 0;
    private int checked = 0;

    private ShoppingList shoppingList;
    private List<PurchasedItems> purchasedItems = new ArrayList<>();
    private PurchasedItems newItems;

    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference; //user
    private DatabaseReference ShoppingCartDBRReference;
    private String ShoppingListID;
    private User user;
    private DatabaseReference ShoppingListDBRReference;


    public ShoppingCartFragment() {
        // Required empty public constructor
    }

    public static ShoppingCartFragment newInstance(FirebaseUser user, String listID) {
        ShoppingCartFragment fragment = new ShoppingCartFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", user);
        args.putString("ShoppingListID", listID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            firebaseUser = getArguments().getParcelable("currentUser");
            ShoppingListID = getArguments().getString("ShoppingListID");
            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("users");
            ShoppingListDBRReference = database.getReference("shopping_lists").child(ShoppingListID);
            ShoppingCartDBRReference = database.getReference("shopping_lists").child(ShoppingListID).child("shoppingCart");
            databaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.err.println("User not found.");
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shoppingcart, container, false);

        // Get references to UI elements
        price = view.findViewById(R.id.price);
        deleteB = view.findViewById(R.id.deleteB);
        purchaseButton = view.findViewById(R.id.purchase);
        doneB = view.findViewById(R.id.doneB);
        itemlist = view.findViewById(R.id.itemlist);
        newItems = new PurchasedItems();
        ArrayList<String> initialList = new ArrayList<>();
        newItems.setItems(initialList);
        newItems.setUser(firebaseUser.getUid());
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        String dateString = formatter.format(currentDate);
        newItems.setDate(dateString);
        setUpButtons();
        getdata();

        doneB.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putParcelable("currentUser", firebaseUser);
            homeFragment.setArguments(args);
            transaction.add(R.id.main_activity_layout, homeFragment);
            transaction.remove(ShoppingCartFragment.this);
            transaction.commit();
        });
        deleteB.setOnClickListener(view1 -> {
            View tableview;
            TableRow row = new TableRow(itemlist.getContext());
            CheckBox box;
            TextView edititem;
            for (int i = 0; i < rowNum; i++) {
                tableview = itemlist.getChildAt(i);
                row = (TableRow) tableview;
                box =(CheckBox) row.getChildAt(0);
                if (box.isChecked()) {
                    edititem = (TextView) row.getChildAt(1);
                    removeDatafromFirebase(edititem.getText().toString(), i);
                }
            }
        });
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!price.getText().toString().equals("")) {
                    purchaseItems();
                } else {
                    Toast.makeText(view.getContext(),
                            "Please enter the amount you spent.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
    private void getdata() {
        if (ShoppingListDBRReference == null) {
            return;
        }
        // calling add value event listener method
        // for getting the values from database.
        ShoppingListDBRReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemlist.removeAllViews();
                ArrayList<String> initializePurchasedItem = new ArrayList<>();
                newItems.setItems(initializePurchasedItem);
                shoppingList = snapshot.getValue(ShoppingList.class);
                if (shoppingList != null) {
                    ArrayList<String> shoppingCart = shoppingList.getShoppingCart();
                    rowNum = 0;
                    for (String s : shoppingCart) {
                        if(!s.equals("")) {
                            ArrayList<String> purchasedItemsList = newItems.getItems();
                            purchasedItemsList.add(s);
                            newItems.setItems(purchasedItemsList);
                            addItemToTable(s);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(itemlist.getContext(),
                        "Fail to get data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //adds item to the list and database
    private void addItemToTable(String value) {

        TableRow row = new TableRow(itemlist.getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        CheckBox box = new CheckBox(itemlist.getContext());
        box.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                           @Override
                                           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                               if(isChecked && checked == 0) {
                                                   deleteB.setClickable(true);
                                                   deleteB.setBackgroundColor(Color.parseColor("#6200ee"));
                                                   checked++;

                                               } else if (isChecked && checked == 1)
                                               {
                                                   deleteB.setClickable(false);
                                                   deleteB.setBackgroundColor(Color.GRAY);
                                                   checked++;
                                               }else if (!isChecked
                                                       && checked == 1)
                                               {
                                                   checked--;
                                               }else if (!isChecked
                                                       && checked == 2)
                                               {
                                                   deleteB.setClickable(false);
                                                   deleteB.setBackgroundColor(Color.GRAY);
                                                   checked--;
                                               }else if (isChecked)
                                               {
                                                   checked++;
                                               }else if (!isChecked)
                                               {
                                                   checked--;
                                               }

                                           }
                                       }
        );

        TextView name = new TextView(itemlist.getContext());
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        name.setPadding(5,5,5,0);
        name.setText(value);
        name.setTextColor(Color.BLACK);
        name.setTextSize(COMPLEX_UNIT_SP, 18);
        row.addView(box);
        row.addView(name);
        itemlist.addView(row, rowNum);
        rowNum++;

    }
    private void setUpButtons()
    {
        deleteB.setClickable(false);
        deleteB.setBackgroundColor(Color.GRAY);
        doneB.setBackgroundColor(Color.parseColor("#6200ee"));
    }
    private void removeDatafromFirebase(String item, int itemIndex) {
        DatabaseReference shoppingListRef = database.getReference("shopping_lists");
        shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shoppingList = snapshot.getValue(ShoppingList.class);
                    ArrayList<String> shoppingCart = shoppingList.getShoppingCart();
                    shoppingCart.remove(item);
                    shoppingList.setShoppingCart(shoppingCart);
                    ArrayList<String> purchasedItemsList = newItems.getItems();
                    purchasedItemsList.remove(item);
                    newItems.setItems(purchasedItemsList);
                    shoppingListRef.child(ShoppingListID).setValue(shoppingList);
                    Toast.makeText(itemlist.getContext(),  item + " removed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });
    }
    private void purchaseItems() {
        String temp = price.getText().toString();
        double purchasePrice = Double.parseDouble(temp);
        DatabaseReference shoppingListRef = database.getReference("shopping_lists").child(ShoppingListID);
        DatabaseReference purchasedItemsRef = shoppingListRef.child("purchasedItems");

        purchasedItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<String> shoppingCartList = new ArrayList<>();
                    ArrayList<PurchasedItems> purchasedItemsList = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        PurchasedItems purchasedItem = itemSnapshot.getValue(PurchasedItems.class);
                        purchasedItemsList.add(purchasedItem);
                    }
                    newItems.setPrice(purchasePrice);
                    purchasedItemsList.add(newItems);

                    // Remove purchased items from unpurchasedItems list
                    DatabaseReference unpurchasedItemsRef = shoppingListRef.child("unpurchasedItems");
                    ArrayList<String> currentUnpurchasedItems = shoppingList.getUnpurchasedItems();
                    ArrayList<String> cartItems = newItems.getItems();
                    for (int i = 0; i < currentUnpurchasedItems.size(); i++) {
                        if (currentUnpurchasedItems.contains(cartItems.get(i))) {
                            currentUnpurchasedItems.remove(cartItems.get(i));
                        }
                    }
                    unpurchasedItemsRef.setValue(currentUnpurchasedItems);


                    // Remove purchased items from shoppingCart list
                    DatabaseReference shoppingCartRef = shoppingListRef.child("shoppingCart");
                    shoppingCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                ArrayList<String> shoppingCartList = new ArrayList<>();
                                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                    String shoppingCartItem = itemSnapshot.getValue(String.class);
                                    boolean found = false;
                                    if (purchasedItemsList != null) {
                                        for (PurchasedItems purchasedItem : purchasedItemsList) {
                                            if (purchasedItem.getItems() != null && purchasedItem.getItems().contains(shoppingCartItem)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!found) {
                                        shoppingCartList.add(shoppingCartItem);
                                    }
                                }
                                shoppingListRef.child("shoppingCart").setValue(shoppingCartList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle errors if needed
                        }
                    });

                    ArrayList<String> initialList = new ArrayList<>();
                    initialList.add("");
                    // Clear the shopping cart
                    shoppingListRef.child("shoppingCart").setValue(initialList);

                    // Update purchasedItems list
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("/purchasedItems", purchasedItemsList);
                    shoppingListRef.updateChildren(updates);
                    Toast.makeText(itemlist.getContext(), "Purchase has been processed.", Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    HomeFragment homeFragment = new HomeFragment();
                    Bundle args = new Bundle();
                    args.putParcelable("currentUser", firebaseUser);
                    homeFragment.setArguments(args);
                    transaction.add(R.id.main_activity_layout, homeFragment);
                    transaction.remove(ShoppingCartFragment.this);
                    transaction.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });
    }



}
