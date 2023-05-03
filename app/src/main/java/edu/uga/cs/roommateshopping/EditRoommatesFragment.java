package edu.uga.cs.roommateshopping;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditRoommatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditRoommatesFragment extends Fragment {

    // Firebase Objects
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbReference;
    private String ShoppingListID;
    private ShoppingList shoppingList;
    private DatabaseReference roommatesDBReference;
    private TableLayout roommateList;
    // List Name
    private TextView shoppingListName;

    // Buttons
    private Button addRoommatebutton, removeRoommateButton, returnToSettingsButton;
    // Local User Info
    private User user;

    // Roommates Names
    private LinearLayout roommatesContainer;
    private DatabaseReference roommateListRef;

    private int rowNum = 0;
    private int checked = 0;
    public EditRoommatesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 1.
     * @param listID Parameter 2.
     * @return A new instance of fragment EditRoommatesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditRoommatesFragment newInstance(FirebaseUser user, String listID) {
        EditRoommatesFragment fragment = new EditRoommatesFragment();
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
            roommatesDBReference = database.getReference("shopping_lists").child(ShoppingListID);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_roommates, container, false);
        roommateList = view.findViewById(R.id.roommateList);
        addRoommatebutton = view.findViewById(R.id.addRoommates);
        removeRoommateButton = view.findViewById(R.id.removeRoommates);
        shoppingListName = view.findViewById(R.id.listNameTextView);
        setUpButtons();
        getData();
        addRoommatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRoommate();
            }
        });
        return view;
    }

    private void getData() {

        // calling add value event listener method
        // for getting the values from database.
        roommatesDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roommateList.removeAllViews();
                shoppingList = snapshot.getValue(ShoppingList.class);
                shoppingListName.setText(shoppingList.getName());
                if (shoppingList != null) {
                    ArrayList<String> roommates = shoppingList.getRoommates();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                    rowNum = 0;
                    for (String roommateID : roommates) {
                        if(!roommateID.equals("")) {
                            userRef.child(roommateID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        User roommate = snapshot.getValue(User.class);
                                        if (roommate != null && roommate.getFirstName() != null) {
                                            addItemToTable(roommate.getFullName());
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(roommateList.getContext(),
                        "Fail to get data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //adds item to the list and database
    private void addItemToTable(String value) {

        TableRow row = new TableRow(roommateList.getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        CheckBox box = new CheckBox(roommateList.getContext());
        box.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        box.setPadding(0, 0, 10, 0);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if (isChecked && checked == 0) {
                   int widthInDp = 100;
                   float scale = getResources().getDisplayMetrics().density;
                   int widthInPx = (int) (widthInDp * scale + 0.5f);
                   removeRoommateButton.setClickable(true);
                   removeRoommateButton.setBackgroundColor(Color.parseColor("#6200ee"));
                   removeRoommateButton.setWidth(widthInPx);
                   checked++;

               } else if (isChecked && checked == 1) {
                   removeRoommateButton.setClickable(false);
                   removeRoommateButton.setBackgroundColor(Color.GRAY);
                   checked++;
               } else if (!isChecked
                       && checked == 1) {
                   removeRoommateButton.setClickable(false);
                   removeRoommateButton.setBackgroundColor(Color.GRAY);
                   checked--;
               } else if (!isChecked
                       && checked == 2) {
                   removeRoommateButton.setClickable(true);
                   removeRoommateButton.setBackgroundColor(Color.parseColor("#6200ee"));
                   checked--;
               } else if (isChecked) {
                   checked++;
               } else if (!isChecked) {
                   checked--;
               }

           }
       }
    );

        TextView name = new TextView(roommateList.getContext());
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        name.setPadding(5,5,5,0);
        name.setText(value);
        name.setTextColor(Color.BLACK);
        name.setTextSize(COMPLEX_UNIT_SP, 18);
        row.addView(box);
        row.addView(name);
        roommateList.addView(row, rowNum);
        rowNum++;

    }

    private void setUpButtons()
    {
        addRoommatebutton.setBackgroundColor(Color.parseColor("#6200ee"));
        removeRoommateButton.setClickable(false);
        removeRoommateButton.setBackgroundColor(Color.GRAY);
    }

    private void addRoommate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Roommate to Shopping List");

        final EditText input = new EditText(getContext());
        input.setHint("Enter email...");
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

        builder.setPositiveButton("Add", null);
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
                String email = input.getText().toString();
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult result = task.getResult();
                        if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                            // The email is associated with an existing account
                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .orderByChild("email")
                                    .equalTo(email)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String userID = "";
                                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                    userID = userSnapshot.getKey();
                                                }
                                                addDatatoFirebase(userID);
                                                alertDialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getContext(), "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // The email is not associated with an existing account
                            Toast.makeText(getContext(), "The email you entered is not associated with an existing account.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // An error occurred while checking if the email is associated with an existing account
                        Toast.makeText(getContext(), "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            negativeButton.setOnClickListener(v -> alertDialog.dismiss());
        });

        alertDialog.show();
    }


    private void addDatatoFirebase(String userID) {

        // we use add value event listener method
        // which is called with database reference.
        DatabaseReference shoppingListRef = database.getReference("shopping_lists");
        shoppingListRef.child(ShoppingListID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ShoppingList shoppingList = snapshot.getValue(ShoppingList.class);
                    ArrayList<String> roommates = shoppingList.getRoommates();
                    roommates.add(userID);
                    shoppingList.setRoommatesID(roommates);
                    shoppingListRef.child(ShoppingListID).setValue(shoppingList);
                    Toast.makeText(roommateList.getContext(), userID + " added", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });

    }

}