package edu.uga.cs.roommateshopping;

import android.app.AlertDialog;
import android.content.Context;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    String TAG = "listFragment";

    private Button addB, editB, deleteB, boughtB, doneB;
    private TableLayout itemlist;
    private int rowNum = 0;
   private int checked = 0;
    private ShoppingList shoppingList;

    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference; //user
    private DatabaseReference unpurchasedDBRReference;
    private String ShoppingListID;
    private User user;
    private DatabaseReference ShoppingListDBRReference;


    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(FirebaseUser user, String listID) {
        ListFragment fragment = new ListFragment();
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
            unpurchasedDBRReference = database.getReference("shopping_lists").child(ShoppingListID).child("unpurchasedItems");
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
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Get references to UI elements
        addB = view.findViewById(R.id.addB);
        editB = view.findViewById(R.id.editB);
        deleteB = view.findViewById(R.id.deleteB);
        boughtB = view.findViewById(R.id.boughtB);
        doneB = view.findViewById(R.id.doneB);
        itemlist = view.findViewById(R.id.itemlist);

        setUpButtons();
        getdata();
        addB.setOnClickListener(v -> {   addItem();   });
        doneB.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            HomeFragment listFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putParcelable("currentUser", firebaseUser);
            listFragment.setArguments(args);
            transaction.add(R.id.main_activity_layout, listFragment);
            transaction.remove(ListFragment.this);
            transaction.commit();
        });
        editB.setOnClickListener(v -> {    editItem();  });
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
        boughtB.setOnClickListener(view12 -> {
            View tableview;
            TableRow row = new TableRow(itemlist.getContext());
            CheckBox box;
            TextView edititem;
            for (int i = 0; i < rowNum; i++) {
                tableview = itemlist.getChildAt(i);
                row = (TableRow) tableview;
                box =(CheckBox) row.getChildAt(0);
                if (box.isChecked()) {
                    box.setChecked(false);
                    edititem = (TextView) row.getChildAt(1);
                    addDatatoShoppingCart(edititem.getText().toString());
                }
            }

        });
        return view;
    }
    private void addItem(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Item to List");

        final EditText input = new EditText(getContext());
        input.setHint("fruit");
        int editTextWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(params);
        FrameLayout.LayoutParams editTextParams = new FrameLayout.LayoutParams(editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        editTextParams.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(editTextParams);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            LinearLayout.LayoutParams positiveButtonLayoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            LinearLayout.LayoutParams negativeButtonLayoutParams = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();

            positiveButtonLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            negativeButtonLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

            int buttonMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            positiveButtonLayoutParams.setMargins(0, 0, buttonMargin, 0);
            negativeButtonLayoutParams.setMargins(buttonMargin, 0, 0, 0);

            positiveButton.setLayoutParams(positiveButtonLayoutParams);
            negativeButton.setLayoutParams(negativeButtonLayoutParams);

            positiveButton.setOnClickListener(v -> {
                String shoppingListName = input.getText().toString();
                if (TextUtils.isEmpty(shoppingListName)) {
                    Toast.makeText(getContext(), "Please enter an item.", Toast.LENGTH_SHORT).show();
                } else {
                    addDatatoFirebase(input.getText().toString());
                    alertDialog.dismiss();

                }
            });

            negativeButton.setOnClickListener(v -> alertDialog.dismiss());
        });

        alertDialog.show();
    }

    private void changeItem(String prev, int prevIndex){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit item");

        final EditText input = new EditText(getContext());
        input.setText(prev);
        int editTextWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(params);
        FrameLayout.LayoutParams editTextParams = new FrameLayout.LayoutParams(editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        editTextParams.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(editTextParams);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("edit", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            LinearLayout.LayoutParams positiveButtonLayoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            LinearLayout.LayoutParams negativeButtonLayoutParams = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();

            positiveButtonLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            negativeButtonLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

            int buttonMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            positiveButtonLayoutParams.setMargins(0, 0, buttonMargin, 0);
            negativeButtonLayoutParams.setMargins(buttonMargin, 0, 0, 0);

            positiveButton.setLayoutParams(positiveButtonLayoutParams);
            negativeButton.setLayoutParams(negativeButtonLayoutParams);

            positiveButton.setOnClickListener(v -> {
                String shoppingListName = input.getText().toString();
                if (TextUtils.isEmpty(shoppingListName)) {
                    Toast.makeText(getContext(), "Please enter an item.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference shoppingListRef = database.getReference("shopping_lists");
                    shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                ShoppingList shoppingList = snapshot.getValue(ShoppingList.class);
                                ArrayList<String> unpurchasedItems = shoppingList.getUnpurchasedItems();
                                unpurchasedItems.set(prevIndex, input.getText().toString());
                                shoppingList.setUnpurchasedItems(unpurchasedItems);
                                shoppingListRef.child(ShoppingListID).setValue(shoppingList);
                                alertDialog.dismiss();
                                Toast.makeText(getContext(), prev + " updated to " + input.getText().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle errors if needed
                        }
                    });
                }
            });

            negativeButton.setOnClickListener(v -> alertDialog.dismiss());
        });

        alertDialog.show();
    }

    private void removeDatafromFirebase(String item, int itemIndex) {
        DatabaseReference shoppingListRef = database.getReference("shopping_lists");
        shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shoppingList = snapshot.getValue(ShoppingList.class);
                    ArrayList<String> unpurchasedItems = shoppingList.getUnpurchasedItems();
                    unpurchasedItems.remove(itemIndex);
                    shoppingList.setUnpurchasedItems(unpurchasedItems);
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


    private void addDatatoFirebase(String item) {

        // we use add value event listener method
        // which is called with database reference.
        DatabaseReference shoppingListRef = database.getReference("shopping_lists");
        shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ShoppingList shoppingList = snapshot.getValue(ShoppingList.class);
                    ArrayList<String> unpurchasedItems = shoppingList.getUnpurchasedItems();
                    unpurchasedItems.add(item);
                    shoppingList.setUnpurchasedItems(unpurchasedItems);
                    shoppingListRef.child(ShoppingListID).setValue(shoppingList);
                    Toast.makeText(itemlist.getContext(), item + " added", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });

    }
    private void addDatatoShoppingCart(String item) {

        // we use add value event listener method
        // which is called with database reference.
        DatabaseReference shoppingListRef = database.getReference("shopping_lists");
        shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shoppingList = snapshot.getValue(ShoppingList.class);
                    ArrayList<String>  shoppingCart= shoppingList.getShoppingCart();
                    shoppingCart.add(item);
                    shoppingList.setShoppingCart(shoppingCart);
                    shoppingListRef.child(ShoppingListID).setValue(shoppingList);
                    //Toast.makeText(itemlist.getContext(), item + " added", Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    ShoppingCartFragment shoppingCartFragment = new ShoppingCartFragment();
                    Bundle args = new Bundle();
                    args.putParcelable("currentUser", firebaseUser);
                    args.putString("ShoppingListID", ShoppingListID);
                    shoppingCartFragment.setArguments(args);
                    transaction.add(R.id.main_activity_layout, shoppingCartFragment);
                    transaction.remove(ListFragment.this);
                    transaction.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });

    }
    private void getdata() {

        // calling add value event listener method
        // for getting the values from database.
        ShoppingListDBRReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemlist.removeAllViews();
                shoppingList = snapshot.getValue(ShoppingList.class);
                if (shoppingList != null) {
                    ArrayList<String> unpurchaseditems = shoppingList.getUnpurchasedItems();
                    rowNum = 0;
                    for (String s : unpurchaseditems) {
                        if(!s.equals("")) {
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
                                                    editB.setClickable(true);
                                                    editB.setBackgroundColor(Color.BLUE);
                                                    deleteB.setClickable(true);
                                                    deleteB.setBackgroundColor(Color.BLUE);
                                                    boughtB.setClickable(true);
                                                    boughtB.setBackgroundColor(Color.BLUE);
                                                    checked++;

                                                } else if (isChecked && checked == 1)
                                                {
                                                    editB.setClickable(false);
                                                    editB.setBackgroundColor(Color.GRAY);
                                                    deleteB.setClickable(false);
                                                    deleteB.setBackgroundColor(Color.GRAY);
                                                    checked++;
                                                }else if (!isChecked
                                                        && checked == 1)
                                                {
                                                    editB.setClickable(false);
                                                    editB.setBackgroundColor(Color.GRAY);
                                                    deleteB.setClickable(false);
                                                    deleteB.setBackgroundColor(Color.GRAY);
                                                    boughtB.setClickable(false);
                                                    boughtB.setBackgroundColor(Color.GRAY);
                                                    checked--;
                                                }else if (!isChecked
                                                       && checked == 2)
                                                {
                                                    deleteB.setClickable(true);
                                                    deleteB.setBackgroundColor(Color.BLUE);
                                                    editB.setClickable(true);
                                                    editB.setBackgroundColor(Color.BLUE);
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
        name.setPadding(5,5,5,0);
        name.setText(value);
        row.addView(box);
        row.addView(name);
        row.setBackgroundColor(Color.GRAY);
        itemlist.addView(row, rowNum);
        rowNum++;

    }

    private TableRow editItem()
    {
        View tableview;
        TableRow row = new TableRow(itemlist.getContext());
        CheckBox box;
        TextView edititem;
        for (int i = 0; i < rowNum; i++) {
            tableview = itemlist.getChildAt(i);
            row = (TableRow) tableview;
            box =(CheckBox) row.getChildAt(0);
            if (box.isChecked()) {
                box.setChecked(false);
                edititem = (TextView) row.getChildAt(1);
                changeItem(edititem.getText().toString(), i );
                break;
            }
        }

        return row;
    }
    private void setUpButtons()
    {
        addB.setBackgroundColor(Color.BLUE);
        editB.setClickable(false);
        editB.setBackgroundColor(Color.GRAY);
        deleteB.setClickable(false);
        deleteB.setBackgroundColor(Color.GRAY);
        boughtB.setClickable(false);
        boughtB.setBackgroundColor(Color.GRAY);
        doneB.setBackgroundColor(Color.BLUE);
    }


}
