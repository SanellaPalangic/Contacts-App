package com.example.contactsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

public class ContactDetailActivity extends AppCompatActivity {

    private TextView textViewName, textViewPhone;//displays contact details
    private Button buttonEdit, buttonDelete; //buttons to edit and delete contacts
    private Contact contact; //current contact being displayed
    private int position; //store contact position in list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        textViewName = findViewById(R.id.textViewName);
        textViewPhone = findViewById(R.id.textViewPhone);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        //get textview  components as variables

        String contactJsonString = getIntent().getStringExtra("contact_json");
        if (contactJsonString != null) {
            try {
                JSONObject contactJson = new JSONObject(contactJsonString);
                int contactId = contactJson.getInt("id");

                // Get contact ID from intent and fetch data from the database
                if (contactId != -1) {
                    MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
                    contact = dbHandler.getContactById(contactId);
                    position = getIntent().getIntExtra("position", -1);

                    if (contact != null) {
                        textViewName.setText(contact.getName());
                        textViewPhone.setText(contact.getPhone());
                    }
                }

                buttonEdit.setOnClickListener(v -> showEditDialog());
                buttonDelete.setOnClickListener(v -> showDeleteConfirmation());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


private void showEditDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Edit Contact");

    final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
    builder.setView(dialogView);

    //find text
    EditText editTextName = dialogView.findViewById(R.id.editTextName);
    EditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);

    // Pre fill fields with current contact data
    editTextName.setText(contact.getName());
    editTextPhone.setText(contact.getPhone());

    builder.setPositiveButton("Save", (dialog, which) -> {
        String newName = editTextName.getText().toString().trim();
        String newPhone = editTextPhone.getText().toString().trim();
// save button
        //get new value from edittext
        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Both fields are required!", Toast.LENGTH_SHORT).show();
        } else {
            MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);

            // Update contact in the database
            contact.setName(newName);
            contact.setPhone(newPhone);
            dbHandler.updateContact(contact);

            // Pass updated contact back to main activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("contact_id", contact.getId());
            resultIntent.putExtra("position", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    });

    builder.setNegativeButton("Cancel", null);
    builder.create().show();
    //Adds a Cancel button to close the dialog without saving changes.
}


private void showDeleteConfirmation() {
    new AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete this contact?")
            .setPositiveButton("Delete", (dialog, which) -> {
                MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
                dbHandler.deleteContact(contact.getId()); // Delete contact from database
//Deletes the contact from the database using its ID.
                Intent resultIntent = new Intent();
                resultIntent.putExtra("position", position);
                setResult(RESULT_FIRST_USER, resultIntent); // Use custom result code for delete
                finish();
                //Sends the position of the deleted contact back to the parent activity and finishes the current activity.
            })
            .setNegativeButton("Cancel", null)
            .create()
            .show();
    //Adds a Cancel button to dismiss the dialog.
}

}
