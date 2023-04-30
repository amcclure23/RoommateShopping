package edu.uga.cs.roommateshopping;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                        String welcomeMessage = "Welcome " + user.getFirstName() + "!";
                        welcomeMessageView.setText(welcomeMessage);
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
        welcomeMessageView = view.findViewById(R.id.welcome_message);
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
}