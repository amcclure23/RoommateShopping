package edu.uga.cs.roommateshopping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private User user;
    private TextView welcomeMessageView;
    private FloatingActionButton menuButton;
    private Button createShoppingListButton;

    private LinearLayout shoppingListContainer;
    private DatabaseReference shoppingListRef;
    private Context mContext;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(FirebaseUser user) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            firebaseUser = getArguments().getParcelable("currentUser");
            System.out.println(firebaseUser.getUid());
            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();
            userRef = FirebaseDatabase.getInstance().getReference("users");
            userRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        assert user != null;
                        System.out.println("User was found!");
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        shoppingListContainer = view.findViewById(R.id.shopping_list_container);
        shoppingListRef = database.getReference("shopping_lists");
        createShoppingListButton = view.findViewById(R.id.createShoppingListButton);
        createShoppingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateListDialog();
            }
        });
        menuButton = view.findViewById(R.id.fab);
        menuButton.setOnClickListener(fabView -> {
            PopupMenu popup = new PopupMenu(getContext(), fabView);
            popup.inflate(R.menu.fab_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.account:
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        AccountFragment accountFragment = new AccountFragment();
                        Bundle args = new Bundle();
                        args.putParcelable("currentUser", firebaseUser);
                        accountFragment.setArguments(args);
                        transaction.add(R.id.main_activity_layout, accountFragment);
                        transaction.remove(HomeFragment.this);
                        transaction.commit();
                        return true;
                    case R.id.logout:
                        auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        startActivity(new Intent(getContext(), MainActivity.class));
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachShoppingListListener();
    }

    private void showCreateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Shopping List Name");

        final EditText input = new EditText(getContext());
        input.setHint("Shopping List Name...");
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
                    Toast.makeText(getContext(), "Please enter a shopping list name.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference shoppingListRef = database.getReference("shopping_lists");
                    ArrayList<String> initialList = new ArrayList<>();
                    initialList.add("");
                    ShoppingList newShoppingList = new ShoppingList();
                    newShoppingList.setName(input.getText().toString());
                    newShoppingList.setOwnerID(firebaseUser.getUid());
                    newShoppingList.setUnpurchasedItems(initialList);
                    newShoppingList.setPurchasedItems(initialList);
                    newShoppingList.setShoppingCart(initialList);
                    newShoppingList.setRoommatesID(initialList);
                    shoppingListRef.push().setValue(newShoppingList);

                    alertDialog.dismiss();
                }
            });

            negativeButton.setOnClickListener(v -> alertDialog.dismiss());
        });

        alertDialog.show();
    }

    private void attachShoppingListListener() {
        shoppingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shoppingListContainer.removeAllViews();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShoppingList shoppingList = snapshot.getValue(ShoppingList.class);
                    String listID = snapshot.getKey();
                    if (shoppingList != null && (shoppingList.getOwnerID().equals(firebaseUser.getUid()) || shoppingList.getRoommates().contains(firebaseUser.getUid()))) {
                        View entryView = addShoppingListEntry(shoppingList, listID);
                        if (entryView != null) { // Check if the view is not null before adding it
                            shoppingListContainer.addView(entryView);
                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private View addShoppingListEntry(ShoppingList shoppingList, String listID) {
        if (!isAdded()) {
            return null; // If the fragment is not attached to the activity, return null
        }

        LinearLayout entryLayout = new LinearLayout(getContext());
        entryLayout.setOrientation(LinearLayout.VERTICAL);
        TextView listName = new TextView(getContext());
        listName.setText(shoppingList.getName());
        listName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        listName.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams listNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int listNameMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        listNameParams.setMargins(0, 0, 0, listNameMargin);
        listName.setLayoutParams(listNameParams);

        entryLayout.addView(listName);

        Button openButton = new Button(getContext());
        openButton.setText("Open");
        openButton.setBackgroundColor(Color.BLUE);
        openButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
           // EditListInfoFragment editListInfoFragment = new EditListInfoFragment();
            ListFragment listFragment = new ListFragment();
            Bundle args = new Bundle();
            args.putParcelable("currentUser", firebaseUser);
            args.putString("ShoppingListID", listID);
          //  editListInfoFragment.setArguments(args);
          //  transaction.add(R.id.main_activity_layout, editListInfoFragment);
            listFragment.setArguments(args);
            transaction.add(R.id.main_activity_layout, listFragment);
            transaction.remove(HomeFragment.this);
            transaction.commit();
        });

        LinearLayout.LayoutParams openButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int openButtonMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        openButtonParams.setMargins(0, 0, 0, openButtonMargin);
        openButton.setLayoutParams(openButtonParams);

        entryLayout.addView(openButton);
        return entryLayout;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

}