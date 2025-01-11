package com.example.contactsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView; //display list of contacts
    private ContactAdapter adapter;
    private List<Contact> contactList = new ArrayList<>(); //store list contacts
    private FloatingActionButton fabAddContact; //adding new contacts
    private SearchView searchView; //filter search contacts
    private MyDBHandler dbHandler; //manage database

    // Register the ActivityResultLauncher
    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
//Initializes a detailLauncher to handle results from ContactDetailActivity when it finishes.
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int contactId = result.getData().getIntExtra("contact_id", -1);
                    int position = result.getData().getIntExtra("position", -1);

                    if (contactId != -1 && position != -1) {
                        Contact updatedContact = dbHandler.getContactById(contactId);
                        contactList.set(position, updatedContact);
                        adapter.refreshList();
                    }
  //If a contact is updated (RESULT_OK), fetches the contact from the database, updates it in contactList, and refreshes the adapter.
                } else if (result.getResultCode() == RESULT_FIRST_USER && result.getData() != null) {
                    int position = result.getData().getIntExtra("position", -1);

                    if (position != -1) {
                        // Remove contact from the list and refresh
                        contactList.remove(position);
                        adapter.refreshList();
                    }
       //If a contact is deleted (RESULT_FIRST_USER), removes it from the list and refreshes the adapter.
                }

                // Refresh the list with the latest data from the database
                refreshContactList();
            }
    );

    private void refreshContactList() {
        contactList.clear();
        contactList.addAll(dbHandler.getAllContacts());
        adapter.refreshList();
    }
//Clears contactList, fetches all contacts from the database, and refreshes the adapter to display updated data


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHandler = new MyDBHandler(this, null, null, 1);
        // Initialize dbhandler
        recyclerView = findViewById(R.id.recyclerView);
        fabAddContact = findViewById(R.id.fabAddContact);
        searchView = findViewById(R.id.searchView);
        contactList = dbHandler.getAllContacts();
        // Set up RecyclerView
        adapter = new ContactAdapter(contactList);

        adapter.setOnContactClickListener((contact, position) -> {
            Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
            JSONObject contactJson = new JSONObject();
            try {
                contactJson.put("id", contact.getId());
                contactJson.put("name", contact.getName());
                contactJson.put("phone", contact.getPhone());
                intent.putExtra("contact_json", contactJson.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            detailLauncher.launch(intent);
 //Creates an Intent to launch ContactDetailActivity.
            //makes contact data as JSON and passes it via the Intent.
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Assigns a click listener to the FAB opening option to add a new contact
        fabAddContact.setOnClickListener(view -> showAddContactDialog());

        // Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }


    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Contact");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
//Finds the input fields and sets up the "Add" button to retrieve and validate user input
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Both fields are required!", Toast.LENGTH_SHORT).show();
            } else {
                Contact contact = new Contact(name, phone);
                dbHandler.addContact(contact);

                // Fetch the newly added contact's ID and add it to the list
                List<Contact> updatedList = dbHandler.getAllContacts();
                contactList.clear();
                contactList.addAll(updatedList);
                adapter.refreshList();

                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
        //Adds a "Cancel" button to dismiss the dialog
    }

}
