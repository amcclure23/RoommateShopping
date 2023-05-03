package edu.uga.cs.roommateshopping;

import android.graphics.Color;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditListInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditListInfoFragment extends Fragment {

    // Firebase Objects
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbReference;
    private String ShoppingListID;
    private ShoppingList shoppingList;


    // List Name
    private TextView shoppingListName;

    // Buttons
    private Button editRoommatesButton, deleteListButton, returnToHomeButton;
    // Local User Info
    private User user;

    // Roommates Names
    private LinearLayout roommatesContainer;
    private DatabaseReference roommateListRef;

    public EditListInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 1.
     * @return A new instance of fragment EditListInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditListInfoFragment newInstance(FirebaseUser user, String listID) {
        EditListInfoFragment fragment = new EditListInfoFragment();
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
            dbReference = FirebaseDatabase.getInstance().getReference("users");
            dbReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
        View view = inflater.inflate(R.layout.fragment_edit_list_info, container, false);
        roommatesContainer = view.findViewById(R.id.roommates_container);
        shoppingListName = view.findViewById(R.id.listNameTextView);
        editRoommatesButton = view.findViewById(R.id.editRoommatesButton);
        deleteListButton = view.findViewById(R.id.deleteListButton);
        returnToHomeButton = view.findViewById(R.id.returnToHomePageButton);
        returnToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                HomeFragment homeFragment = new HomeFragment();
                Bundle args = new Bundle();
                args.putParcelable("currentUser", firebaseUser);
                homeFragment.setArguments(args);
                transaction.add(R.id.main_activity_layout, homeFragment);
                transaction.remove(EditListInfoFragment.this);
                transaction.commit();
            }
        });
        roommateListRef = database.getReference("shopping_lists").child(ShoppingListID);
        attachRoommatesListener();
        editRoommatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                EditRoommatesFragment editRoommatesFragment = new EditRoommatesFragment();
                Bundle args = new Bundle();
                args.putParcelable("currentUser", firebaseUser);
                args.putString("ShoppingListID", ShoppingListID);
                editRoommatesFragment.setArguments(args);
                transaction.add(R.id.main_activity_layout, editRoommatesFragment);
                transaction.remove(EditListInfoFragment.this);
                transaction.commit();
            }
        });
        deleteListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteListDialog();
            }
        });
        return view;
    }

    private void showDeleteListDialog() {
        DatabaseReference listRef = database.getReference("shopping_lists");
        DatabaseReference thisList = listRef.child(ShoppingListID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Delete");

        final EditText input = new EditText(getContext());
        input.setHint("Enter list name to confirm");
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

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String listName = input.getText().toString();
                if (listName.equals(shoppingList.getName())) {
                    thisList.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                HomeFragment homeFragment = new HomeFragment();
                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                FirebaseUser currentUser = auth.getCurrentUser();
                                Bundle args = new Bundle();
                                args.putParcelable("currentUser", currentUser);
                                homeFragment.setArguments(args);
                                transaction.add(R.id.main_activity_layout, homeFragment);
                                transaction.addToBackStack(null);
                                transaction.remove(EditListInfoFragment.this);
                                transaction.commit();
                                System.out.println("Shopping list was deleted.");
                            } else {
                                // An error occurred while trying to delete the entry
                                System.err.println("Failed to delete shopping list. Error: " + task.getException());
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Entry does not match list name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void attachRoommatesListener() {
        roommateListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shoppingList = dataSnapshot.getValue(ShoppingList.class);
                if (shoppingList != null) {
                    if (shoppingList.getOwnerID().equals(firebaseUser.getUid())) {
                        deleteListButton.setVisibility(View.VISIBLE);
                    } else {
                        deleteListButton.setVisibility(View.GONE);
                    }
                    shoppingListName.setText(shoppingList.getName());
                    ArrayList<String> roommateIDs = shoppingList.getRoommates();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                    userRef.child(shoppingList.getOwnerID()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                user = snapshot.getValue(User.class);
                                addRoommateEntry(user);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            System.err.println("User not found.");
                        }
                    });
                    for (String roommateID : roommateIDs) {
                        userRef.child(roommateID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User roommate = snapshot.getValue(User.class);
                                    if (roommate != null && roommate.getFirstName() != null) {
                                        addRoommateEntry(roommate);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                System.err.println("User not found.");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    private void addRoommateEntry(User user) {
        if (!isAdded()) {

        } else {
            TextView roommateName = new TextView(getContext());
            roommateName.setText(user.getFullName());
            roommateName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            roommateName.setTextColor(Color.BLACK);

            LinearLayout.LayoutParams listNameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int listNameMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            listNameParams.setMargins(0, 0, 0, listNameMargin);
            roommateName.setLayoutParams(listNameParams);

            roommatesContainer.addView(roommateName);
        }
    }
}