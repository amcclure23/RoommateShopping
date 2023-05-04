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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.security.AccessController;
import java.util.ArrayList;

public class SettleFragment extends Fragment {
    private ShoppingList shoppingList;
    private PurchasedItems purchase;
    private Button done, list, settleCost;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference; //user
    private DatabaseReference purchasedDBRReference;
    private String ShoppingListID;
    private User user;
    private DatabaseReference ShoppingListDBRReference;
    private int rowNum;
    private TableLayout userpurcases;

    public SettleFragment() {
        // Required empty public constructor
    }

    public static SettleFragment newInstance(FirebaseUser user, String listID) {
        SettleFragment fragment = new SettleFragment();
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
            purchasedDBRReference = database.getReference("shopping_lists").child(ShoppingListID).child("purchasedItems");
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
        View view = inflater.inflate(R.layout.fragment_settle, container, false);

        // Get references to UI elements
        done = view.findViewById(R.id.done);
        settleCost = view.findViewById(R.id.settlecosts);
        list = view.findViewById(R.id.list);
        userpurcases = view.findViewById(R.id.userpurchases);
        inputDatatoView();
        done.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putParcelable("currentUser", firebaseUser);
            homeFragment.setArguments(args);
            transaction.add(R.id.main_activity_layout, homeFragment);
            transaction.remove(SettleFragment.this);
            transaction.commit();
        });
        list.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            ListFragment listFragment = new ListFragment();
            Bundle args = new Bundle();
            args.putParcelable("currentUser", firebaseUser);
            args.putString("ShoppingListID", ShoppingListID);
            listFragment.setArguments(args);
            transaction.add(R.id.main_activity_layout, listFragment);
            transaction.remove(SettleFragment.this);
            transaction.commit();
        });

        return view;
    }

    private void inputDatatoView()
    {
        ShoppingListDBRReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userpurcases.removeAllViews();
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
                Toast.makeText(userpurcases.getContext(),
                        "Fail to get data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addItemToTable(String value) {

        TableRow row = new TableRow(userpurcases.getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView name = new TextView(userpurcases.getContext());
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        name.setPadding(5,5,5,0);
        name.setText(value);
        name.setTextColor(Color.BLACK);
        name.setTextSize(COMPLEX_UNIT_SP, 18);

        Spinner items = new Spinner(userpurcases.getContext());
        items.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        ArrayList<String> itemsList =purchase.getItems();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(userpurcases.getContext(), android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        items.setAdapter(spinnerAdapter);

        for (String s : itemsList) {
            if(!s.equals("")) {
                spinnerAdapter.add(s);
            }
        }
        items.setAdapter(spinnerAdapter);
        items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                popOut(selectedItemView.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        row.addView(name);
        row.addView(items);
        userpurcases.addView(row, rowNum);
        rowNum++;
    }
    private void popOut(String value)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("remove item: "+ value);

        final EditText input = new EditText(getContext());
        input.setHint("update cost");
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

        builder.setPositiveButton("Confirm", null);
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
                    Toast.makeText(getContext(), "Please enter correct price.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference shoppingListRef = database.getReference("shopping_lists");
                    shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                            //remove item from purchased add to list & update price
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
}