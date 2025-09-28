package com.example.shiksharemastered;

import static java.lang.String.valueOf;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String[] availableUserTypes = {"Student", "Teacher"};
    private TextView nameText, motherNameText, fatherNameText, emailTextSignUp, emailTextLogin, phoneNumberText, passwordTextSignUp, passwordTextLogin, confirmPasswordText, passwordRulesSignUp, passwordRulesLogin;
    private EditText nameEditText, motherNameEditText, fatherNameEditText, emailEditTextSignUp, emailEditTextLogin, phoneNumberEditText, passwordEditTextSignUp, passwordEditTextLogin, confirmPasswordEditText;
    private String userType, userName, usersMothersName, usersFathersName, usersEmail, usersPhoneNumber, usersPassword, usersConfirmPassword, usersGeneratedId;
    private String[] userDetails;
    private Button signUpButton, loginButton, signUpSection, loginSection;
    private ScrollView signUpFrame, loginFrame;
    private Spinner userTypeSpinnerSignUp;
    private int currentFrame = 1;
    private FirebaseDatabase database;
    private Intent toMainMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        userTypeSpinnerSignUp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    motherNameText.setVisibility(View.VISIBLE);
                    motherNameEditText.setVisibility(View.VISIBLE);

                    fatherNameText.setVisibility(View.VISIBLE);
                    fatherNameEditText.setVisibility(View.VISIBLE);

                    nameEditText.requestFocus();
                } else {
                    motherNameText.setVisibility(View.GONE);
                    motherNameEditText.setVisibility(View.GONE);

                    fatherNameText.setVisibility(View.GONE);
                    fatherNameEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("spinner", "onNothingSelected: Hm Idk");
            }
        });

        signUpSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFrame != 0) {
                    loginFrame.setVisibility(View.GONE);
                    signUpFrame.setVisibility(View.VISIBLE);
                    signUpSection.setBackgroundColor(getResources().getColor(R.color.gray));
                    loginSection.setBackgroundColor(getResources().getColor(R.color.dark_gray));
                    resetPage();
                    currentFrame = 0;
                }
            }
        });

        loginSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFrame != 1) {
                    signUpFrame.setVisibility(View.GONE);
                    loginFrame.setVisibility(View.VISIBLE);
                    signUpSection.setBackgroundColor(getResources().getColor(R.color.dark_gray));
                    loginSection.setBackgroundColor(getResources().getColor(R.color.gray));
                    resetPage();
                    currentFrame = 1;
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpRequested();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginRequested();
            }
        });
    }

    public void signUpRequested() {
        if (verifiedEntriesForSignUp()) {
            compileData();

            toMainMenu.putExtra("iAmAStringOfUserDetailsToGameMenu", userDetails);

            uploadData();
        }
    }

    public void loginRequested() {
        if (verifiedEntriesForLogin()) {
            generateId();
            searchInData();
        }
    }

    private void searchInData() {
        final Boolean[] flag = {true};
        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()) {
                    if (snapshot1.child(usersGeneratedId).exists()) {
                        if (usersPassword.equals(valueOf(snapshot1.child(usersGeneratedId).child("userDetails").child("Password").getValue()))) {
                            userType = snapshot1.getKey();
                            userName = Objects.requireNonNull(snapshot1.child(usersGeneratedId).child("userDetails").child("Name").getValue()).toString();
                            usersEmail = Objects.requireNonNull(snapshot1.child(usersGeneratedId).child("userDetails").child("Email").getValue()).toString();
                            usersPhoneNumber = Objects.requireNonNull(snapshot1.child(usersGeneratedId).child("userDetails").child("PhoneNumber").getValue()).toString();

                            compileData();
                            flag[0] = false;

                            toMainMenu.putExtra("iAmAStringOfUserDetailsToGameMenu", userDetails);
                            startActivity(toMainMenu);
                            Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
                if (flag[0]) {
                    Toast.makeText(getApplicationContext(), "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("read", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void compileData() {
        if (userType.equals("Student")) {
            userDetails[0] = "Standard" + userType;
        } else {
            userDetails[0] = userType;
        }
        userDetails[1] = usersGeneratedId;
        userDetails[2] = userName;
        userDetails[3] = usersEmail;
        userDetails[4] = usersPhoneNumber;
    }

    private void init() {
        database = FirebaseDatabase.getInstance("https://shiksha-37914-default-rtdb.asia-southeast1.firebasedatabase.app/");

        nameText = findViewById(R.id.nameText);
        nameEditText = findViewById(R.id.nameEditText);

        motherNameText = findViewById(R.id.motherNameText);
        motherNameEditText = findViewById(R.id.motherNameEditText);

        fatherNameText = findViewById(R.id.fatherNameText);
        fatherNameEditText = findViewById(R.id.fatherNameEditText);

        emailTextSignUp = findViewById(R.id.emailTextSignUp);
        emailTextLogin = findViewById(R.id.emailTextLogin);

        emailEditTextSignUp = findViewById(R.id.emailEditTextSignUp);
        emailEditTextLogin = findViewById(R.id.emailEditTextLogin);

        phoneNumberText = findViewById(R.id.phoneNumberText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);

        passwordTextSignUp = findViewById(R.id.passwordTextSignUp);
        passwordTextLogin = findViewById(R.id.passwordTextLogin);
        passwordEditTextSignUp = findViewById(R.id.passwordEditTextSignUp);
        passwordEditTextLogin = findViewById(R.id.passwordEditTextLogin);

        confirmPasswordText = findViewById(R.id.confirmPasswordText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        passwordRulesSignUp = findViewById(R.id.passwordRulesSignUp);
        passwordRulesLogin = findViewById(R.id.passwordRulesLogin);

        signUpButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.loginButton);
        signUpSection = findViewById(R.id.signUpSection);
        loginSection = findViewById(R.id.loginSection);

        signUpFrame = findViewById(R.id.signUpFrame);
        loginFrame = findViewById(R.id.loginFrame);

        userTypeSpinnerSignUp = findViewById(R.id.userTypeSpinnerSignUp);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_text_view, availableUserTypes);
        userAdapter.setDropDownViewResource(R.layout.spinner_item_text_view);
        userTypeSpinnerSignUp.setAdapter(userAdapter);

        toMainMenu = new Intent(getApplicationContext(), MainMenu.class);

        userDetails = new String[5];
    }

    private boolean verifiedEntriesForSignUp() {
        boolean flag = true;

        userType = valueOf(userTypeSpinnerSignUp.getSelectedItem());

        userName = valueOf(nameEditText.getText()).trim();
        if (userName.equals("") || !(DataValidator.validateName(userName))) {
            nameText.setText(R.string.name_compulsory);
            flag = false;
        } else {
            nameText.setText(R.string.name);
        }

        if (userType.equals("Student")) {
            usersMothersName = valueOf(motherNameEditText.getText()).trim();
            if (usersMothersName.equals("") || !(DataValidator.validateName(usersMothersName))) {
                motherNameText.setText(R.string.mother_name_compulsory);
                flag = false;
            } else {
                motherNameText.setText(R.string.mother_name);
            }

            usersFathersName = valueOf(fatherNameEditText.getText()).trim();
            if (usersFathersName.equals("") || !(DataValidator.validateName(usersFathersName))) {
                fatherNameText.setText(R.string.father_name_compulsory);
                flag = false;
            } else {
                fatherNameText.setText(R.string.father_name);
            }
        }

        usersEmail = valueOf(emailEditTextSignUp.getText()).trim();
        if (usersEmail.equals("") || !(DataValidator.validateEmail(usersEmail))) {
            emailTextSignUp.setText(R.string.email_compulsory);
            flag = false;
        } else {
            emailTextSignUp.setText(R.string.email);
        }

        usersPhoneNumber = valueOf(phoneNumberEditText.getText()).trim();
        if (usersPhoneNumber.equals("") || !(DataValidator.validatePhoneNumber(usersPhoneNumber))) {
            phoneNumberText.setText(R.string.phone_number_compulsory);
            flag = false;
        } else {
            phoneNumberText.setText(R.string.phone_number);
        }

        usersPassword = valueOf(passwordEditTextSignUp.getText()).trim();
        if (usersPassword.equals("") || !(DataValidator.validatePassword(usersPassword))) {
            passwordTextSignUp.setText(R.string.password_compulsory);
            passwordRulesSignUp.setVisibility(View.VISIBLE);
            flag = false;
        } else {
            passwordTextSignUp.setText(R.string.password);
            passwordRulesSignUp.setVisibility(View.GONE);
        }

        usersConfirmPassword = valueOf(confirmPasswordEditText.getText()).trim();
        if (usersConfirmPassword.equals("") || !(DataValidator.validatePassword(usersConfirmPassword))) {
            confirmPasswordText.setText(R.string.confirmPassword_compulsory);
            flag = false;
        } else {
            confirmPasswordText.setText(R.string.confirmPassword);
        }

        if (!(usersConfirmPassword.equals(usersPassword))) {
            passwordEditTextSignUp.setText("");
            confirmPasswordEditText.setText("");
            Toast.makeText(this, "Password didn't match with Confirmed Password", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        return flag;
    }

    private boolean verifiedEntriesForLogin() {
        boolean flag = true;

        usersEmail = valueOf(emailEditTextLogin.getText()).trim();
        if (usersEmail.equals("") || !(DataValidator.validateEmail(usersEmail))) {
            emailTextLogin.setText(R.string.email_compulsory);
            flag = false;
        } else {
            emailTextLogin.setText(R.string.email);
        }

        usersPassword = valueOf(passwordEditTextLogin.getText()).trim();
        if (usersPassword.equals("") || !(DataValidator.validatePassword(usersPassword))) {
            passwordTextLogin.setText(R.string.password_compulsory);
            passwordRulesLogin.setVisibility(View.VISIBLE);
            flag = false;
        } else {
            passwordTextLogin.setText(R.string.password);
            passwordRulesLogin.setVisibility(View.GONE);
        }

        return flag;
    }

    public void resetPage() {
        userName = "";
        usersEmail = "";
        usersPhoneNumber = "";
        usersPassword = "";
        usersConfirmPassword = "";
        usersGeneratedId = "";

        nameEditText.setText("");
        motherNameEditText.setText("");
        fatherNameEditText.setText("");
        emailEditTextSignUp.setText("");
        phoneNumberEditText.setText("");
        passwordEditTextSignUp.setText("");
        confirmPasswordEditText.setText("");

        emailEditTextLogin.setText("");
        passwordEditTextLogin.setText("");

        nameText.setText(R.string.name);
        motherNameText.setText(R.string.mother_name);
        fatherNameText.setText(R.string.father_name);
        emailTextSignUp.setText(R.string.email);
        phoneNumberText.setText(R.string.phone_number);
        passwordTextSignUp.setText(R.string.password);
        confirmPasswordText.setText(R.string.confirmPassword);
        emailTextLogin.setText(R.string.email);
        passwordTextLogin.setText(R.string.password);

        Arrays.fill(userDetails, "");
    }

    private void generateId() {
        int index = 0;
        char charToBeReplaced;
        usersGeneratedId = usersEmail;
        while (index < usersGeneratedId.length()) {
            charToBeReplaced = usersGeneratedId.charAt(index);
            if (charToBeReplaced == '.' || charToBeReplaced == '$' || charToBeReplaced == '#' || charToBeReplaced == '[' || charToBeReplaced == ']') {
                usersGeneratedId = usersGeneratedId.replace(charToBeReplaced, '@');
            }
            index++;
        }
    }

    private void uploadData() {
        HashMap<String, Object> map = new HashMap<>();
        if (userType.equals("Teacher")) {
            map.put("Name", userName);
            map.put("Email", usersEmail);
            map.put("PhoneNumber", usersPhoneNumber);
            map.put("Password", usersPassword);
        } else {
            map.put("Name", userName);
            map.put("Mother's Name", usersMothersName);
            map.put("Father's Name", usersFathersName);
            map.put("Email", usersEmail);
            map.put("PhoneNumber", usersPhoneNumber);
            map.put("Password", usersPassword);
        }

        generateId();
        String f_address;
        if (userType.equals("Student")) {
            f_address = "Standard" + userType;
        } else {
            f_address = userType;
        }
        database.getReference().child("Users").child("Teacher").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()) {
                    if (!snapshot1.getKey().toString().equals(usersGeneratedId)) {
                        database.getReference().child("Users").child("StandardStudent").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                    if (!snapshot3.getKey().toString().equals(usersGeneratedId)) {
                                        DatabaseReference myRef = database.getReference().child("Users").child(f_address).child(usersGeneratedId).child("userDetails");
                                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (!snapshot.exists()) {
                                                    myRef.setValue(map)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d("upload", "onComplete: Completed");
                                                                }
                                                            })
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            HashMap<String, Object> map = new HashMap<>();
                                                                            int value = snapshot.child("userCount").getValue(Integer.class) + 1;
                                                                            map.put("userCount", value);
                                                                            database.getReference().child("Users").updateChildren(map);
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });
                                                                    Toast.makeText(getApplicationContext(), "SignUp Success", Toast.LENGTH_SHORT).show();
                                                                    startActivity(toMainMenu);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getApplicationContext(), "SignUp Failed", Toast.LENGTH_SHORT).show();
                                                                    Log.d("upload", "onFailure: Failed" + e.getMessage());
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Email Already Used", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d("upload", "onCancelled: " + error.getMessage());
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Email Already Used", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("upload", "onCancelled: " + error.getMessage());
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Email Already Used", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("upload", "onCancelled: " + error.getMessage());
            }
        });
    }
}