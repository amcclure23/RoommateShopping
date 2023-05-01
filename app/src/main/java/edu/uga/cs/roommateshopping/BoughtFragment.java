package edu.uga.cs.roommateshopping;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BoughtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BoughtFragment extends Fragment {


    public BoughtFragment() {
        // Required empty public constructor
    }

    String TAG = "listFragment";

    private Button submit;
    private TableLayout itemlist;
    private EditText price;
    private int rowNum = 0;
    Purchase purchase;
    String items[];

    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public static BoughtFragment newInstance(FirebaseUser user, String[] list) {
        BoughtFragment fragment = new BoughtFragment();
        Bundle args = new Bundle();
        args.putParcelable("currentUser", user);
        //args.putParcelable("list", list );
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           // firebaseUser = getArguments().getParcelable("currentUser");
          //  System.out.println(firebaseUser.getUid());
          //  database = FirebaseDatabase.getInstance();
           // auth = FirebaseAuth.getInstance();
          //  userRef = FirebaseDatabase.getInstance().getReference("users");
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Data");
        }
        // Get Firebase authentication instance

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Get references to UI elements

        submit = view.findViewById(R.id.submit);
        itemlist = view.findViewById(R.id.itemlist);
        price = view.findViewById(R.id.price);
        getdata();
        submit.setOnClickListener(v -> {
            if(!price.getText().toString().equals("")) {
                addDatatoFirebase(price.getText().toString());
                /**
                 * add stuff to jump to the purchased list
                 */
            }

        });
        return view;
    }
    private void addDatatoFirebase(String price) {
        //purchase = new Purchase(User.getFullName(),new Date(),items, price);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              //  databaseReference.setValue(purchase);
                Toast.makeText(itemlist.getContext(), "data added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(itemlist.getContext(), "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getdata() {
        TableRow row;
        TextView name;
        for(int i = 0; i<items.length;i++) {
            row = new TableRow(itemlist.getContext());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            name = new TextView(itemlist.getContext());
            name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            name.setPadding(5, 5, 5, 0);
            row.addView(name);
            name.setText(items[i]);
            row.setBackgroundColor(Color.GRAY);
            itemlist.addView(row, rowNum);
        }
    }
}
