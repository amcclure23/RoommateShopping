package edu.uga.cs.roommateshopping;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    String TAG = "listFragment";

    private Button addB, editB, deleteB, boughtB, doneB;
    private TableLayout itemlist;
    private int rowNum = 0;
   private FirebaseAuth mAuth;
   private String action;
   private int checked = 0;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Data");
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
        addB.setOnClickListener(v -> {
            disableall();
            doneB.setClickable(true);
            doneB.setBackgroundColor(Color.BLUE);
            addItem();
            action = "add";
        });
        doneB.setOnClickListener(v -> {
            if (action.equals("add")){
                View tableview = itemlist.getChildAt(rowNum);
                TableRow row = (TableRow) tableview;
                EditText edititem = (EditText) row.getChildAt(1);
                System.out.println("table");
                edititem.setFocusable(false);
                row.setBackgroundColor(Color.GRAY);
                if (TextUtils.isEmpty(edititem.getText().toString())) {
                    Toast.makeText(itemlist.getContext(), "Please add some data.", Toast.LENGTH_SHORT).show();
                } else {
                    addDatatoFirebase(edititem.getText().toString());
                }
                rowNum++;
            } else if (action.equals("edit")) {
                TableRow row = editItem();
                EditText edititem = (EditText) row.getChildAt(1);
                if (TextUtils.isEmpty(edititem.getText().toString())) {
                    Toast.makeText(itemlist.getContext(), "Please add some data.", Toast.LENGTH_SHORT).show();
                } else {
                    addDatatoFirebase(edititem.getText().toString());
                }
                CheckBox box = (CheckBox) row.getChildAt(0);
                box.setChecked(false);
                row.setBackgroundColor(Color.GRAY);
            }
            addB.setClickable(true);
            addB.setBackgroundColor(Color.BLUE);
            doneB.setClickable(false);
            doneB.setBackgroundColor(Color.GRAY);

        });
        editB.setOnClickListener(v -> {
            disableall();
            doneB.setClickable(true);
            doneB.setBackgroundColor(Color.BLUE);
            editItem();
        });
        deleteB.setOnClickListener(view1 -> {
            View tableview;
            TableRow row = new TableRow(itemlist.getContext());
            CheckBox box;
            EditText edititem;
            for (int i = 0; i < rowNum; i++) {
                tableview = itemlist.getChildAt(rowNum);
                row = (TableRow) tableview;
                box =(CheckBox) row.getChildAt(0);
                if (box.isChecked()) {
                    edititem = (EditText) row.getChildAt(1);
                    delete(edititem.getText().toString());
                    itemlist.removeView(row);
                    rowNum--;
                }
            }
        });
        boughtB.setOnClickListener(view12 -> {
            View tableview;
            TableRow row = new TableRow(itemlist.getContext());
            CheckBox box;
            EditText edititem;
            for (int i = 0; i < rowNum; i++) {
                tableview = itemlist.getChildAt(rowNum);
                row = (TableRow) tableview;
                box =(CheckBox) row.getChildAt(0);
                if (box.isChecked()) {
                    edititem = (EditText) row.getChildAt(1);
                    addDatatoFirebase(edititem.getText().toString());
                    delete(edititem.getText().toString());
                    itemlist.removeView(row);
                    rowNum--;
                }
            }
        });
        return view;
    }
    private void addDatatoFirebase(String item) {

        // we are use add value event listener method
        // which is called with database reference.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // inside the method of on Data change we are setting
                // our object class to our database reference.
                // data base reference will sends data to firebase.
                databaseReference.setValue(item);

                // after adding this data we are showing toast message.
                Toast.makeText(itemlist.getContext(), "data added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if the data is not added or it is cancelled then
                // we are displaying a failure toast message.
                Toast.makeText(itemlist.getContext(), "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getdata() {

        // calling add value event listener method
        // for getting the values from database.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                addItem();
                View tableview = itemlist.getChildAt(rowNum);
                TableRow row = (TableRow) tableview;
                EditText edititem = (EditText) row.getChildAt(1);
                edititem.setText(value);
                edititem.setFocusable(false);
                row.setBackgroundColor(Color.GRAY);
                rowNum++;
                // after getting the value we are setting
                // our value to our text view in below line.

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText( itemlist.getContext(),
                        "Fail to get data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableall(){
        addB.setClickable(false);
        addB.setBackgroundColor(Color.GRAY);
        doneB.setClickable(true);
        editB.setClickable(false);
        editB.setBackgroundColor(Color.GRAY);
        deleteB.setClickable(false);
        deleteB.setBackgroundColor(Color.GRAY);
        boughtB.setClickable(false);
        boughtB.setBackgroundColor(Color.GRAY);
    }
    //adds item to the list and database
    private void addItem() {

        TableRow row = new TableRow(itemlist.getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        CheckBox box = new CheckBox(itemlist.getContext());
        box.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                           @Override
                                           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                                if(isChecked
                                                        && !doneB.isClickable()
                                                        && checked == 0
                                                ) {
                                                    editB.setClickable(true);
                                                    editB.setBackgroundColor(Color.BLUE);
                                                    deleteB.setClickable(true);
                                                    deleteB.setBackgroundColor(Color.BLUE);
                                                    boughtB.setClickable(true);
                                                    boughtB.setBackgroundColor(Color.BLUE);
                                                    checked++;

                                                } else if (isChecked
                                                        && !doneB.isClickable()
                                                        && checked == 1)
                                                {
                                                    editB.setClickable(false);
                                                    editB.setBackgroundColor(Color.GRAY);
                                                    checked++;
                                                }else if (!isChecked
                                                        && !doneB.isClickable()&& checked == 1)
                                                {
                                                    editB.setClickable(false);
                                                    editB.setBackgroundColor(Color.GRAY);
                                                    deleteB.setClickable(false);
                                                    deleteB.setBackgroundColor(Color.GRAY);
                                                    boughtB.setClickable(false);
                                                    boughtB.setBackgroundColor(Color.GRAY);
                                                    checked--;
                                                }else if (!isChecked
                                                        && !doneB.isClickable()&& checked == 2)
                                                {
                                                    editB.setClickable(true);
                                                    editB.setBackgroundColor(Color.BLUE);
                                                    checked--;
                                                }else if (isChecked
                                                        && !doneB.isClickable())
                                                {
                                                    checked++;
                                                }else if (!isChecked
                                                        && !doneB.isClickable())
                                                {
                                                        checked--;
                                                }

                                           }
                                       }
        );

        EditText name = new EditText(itemlist.getContext());
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        name.setText("Please Enter item");
        name.setPadding(5,5,5,0);
        name.setFocusable(true);

        row.addView(box);
        row.addView(name);
        itemlist.addView(row, rowNum);
    }

    private TableRow editItem()
    {
        View tableview;
        TableRow row = new TableRow(itemlist.getContext());
        CheckBox box;
        EditText edititem;
        for (int i = 0; i < rowNum; i++) {
            tableview = itemlist.getChildAt(rowNum);
            row = (TableRow) tableview;
            box =(CheckBox) row.getChildAt(0);
            if (box.isChecked()) {
                edititem = (EditText) row.getChildAt(1);
                row.setBackgroundColor(Color.WHITE);
                delete(edititem.getText().toString());
                edititem.setFocusable(true);
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
        doneB.setClickable(false);
        doneB.setBackgroundColor(Color.GRAY);
    }

    private void delete(String item)
    {
        databaseReference.collection("shopping list").document(item)
                .delete()
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener((OnFailureListener) e -> Log.w(TAG, "Error deleting document", e));
        rowNum--;
    }

}
