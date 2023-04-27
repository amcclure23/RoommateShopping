package edu.uga.cs.roommateshopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RegistrationActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Firebase Access
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Text Fields
        firstNameEditText = findViewById(R.id.firstNameRegister);
        lastNameEditText = findViewById(R.id.lastNameRegister);
        emailEditText = findViewById(R.id.emailRegister);
        passwordEditText = findViewById(R.id.passwordRegister);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordRegister);
        registerButton = findViewById(R.id.createAccountButton);

        registerButton.setOnClickListener(view -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // Check if any of the fields are empty
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Context context = getApplicationContext();
                CharSequence text = "Please fill all fields before registering.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            } else if (!password.equals(confirmPassword)) {
                Context context = getApplicationContext();
                CharSequence text = "Password Mismatch! Please ensure your password fields match.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else if (!isValidEmail(email)) {
                Context context = getApplicationContext();
                CharSequence text = "Invalid email address";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                // email is valid, check if user exists
                auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods == null || signInMethods.isEmpty()) {
                            // User does not exist, create account
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // User created successfully
                                            FirebaseUser firebaseUser = auth.getCurrentUser();
                                            String userID = firebaseUser.getUid();

                                            // Create a User object and set its properties
                                            User user = new User();
                                            user.setFirstName(firstName);
                                            user.setLastName(lastName);
                                            user.setEmail(email);
                                            usersRef.child(userID).setValue(user);

                                            Context context = getApplicationContext();
                                            CharSequence text = "Account creation successful!";
                                            int duration = Toast.LENGTH_SHORT;

                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                            Intent intent = new Intent(this, MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            // User creation failed
                                            String errorMessage = task1.getException().getMessage();
                                            // Handle the error
                                        }
                                    });
                        } else {
                            // User exists, display error message
                            Context context = getApplicationContext();
                            CharSequence text = "An account with this email already exists";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    } else {
                        // Error occurred while checking if user exists
                        String errorMessage = task.getException().getMessage();
                        // Handle the error
                    }
                });
            }
        });
    }

    private boolean isValidEmail(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

