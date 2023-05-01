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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // Firebase Objects
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    // Local User Info
    private User user;

    // Application UI
    private EditText editUserFirstName, editUserLastName;
    private Button updateUserInfoButton, changePasswordButton, homeButton;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Firebase User object.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(FirebaseUser user) {
        AccountFragment fragment = new AccountFragment();
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
            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();
            userRef = FirebaseDatabase.getInstance().getReference("users");
            userRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        assert user != null;
                        editUserFirstName.setText(user.getFirstName());
                        editUserLastName.setText(user.getLastName());
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
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        editUserFirstName = view.findViewById(R.id.editUserFirstName);
        editUserLastName = view.findViewById(R.id.editUserLastName);
        updateUserInfoButton = view.findViewById(R.id.submitChangePasswordButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        homeButton = view.findViewById(R.id.returnHomeButton);
        updateUserInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Updates user information
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users");
                String firstName = editUserFirstName.getText().toString();
                String lastName = editUserLastName.getText().toString();
                String fullName = firstName + " " + lastName;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", firstName);
                updates.put("lastName", lastName);
                updates.put("fullName", fullName);
                myRef.child(firebaseUser.getUid()).updateChildren(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Account information updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Could not find user.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
                Bundle args = new Bundle();
                args.putParcelable("currentUser", currentUser);
                changePasswordFragment.setArguments(args);
                transaction.add(R.id.main_activity_layout, changePasswordFragment);
                transaction.addToBackStack(null);
                transaction.remove(AccountFragment.this);
                transaction.commit();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                transaction.remove(AccountFragment.this);
                transaction.commit();
            }
        });
        return view;
    }
}