package edu.uga.cs.roommateshopping;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
}