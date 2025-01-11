package com.example.contactsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList; // Original list of all contacts.
    private List<Contact> filteredList; //Modified list for search functionality
    private OnContactClickListener clickListener; //handle when user clicks anything

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
        this.filteredList = new ArrayList<>(contactList);
        //Creates a copy of contactList in filtered list for search operations.
    }

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = filteredList.get(position);
        holder.name.setText(contact.getName()); //display contacts name in the textview
        holder.phone.setText(contact.getPhone()); //display phone

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                // Use the position from filteredList to map to the original contactList
                int originalPosition = contactList.indexOf(contact);
                clickListener.onContactClick(contact, originalPosition);
            }
        });
    }

    @Override
    public int getItemCount() {

        return filteredList.size();
        //Returns the number of items in filteredList. This determines
        // how many rows the RecyclerView will display.
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(contactList);
        } else {
            for (Contact contact : contactList) {
                if (contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                        contact.getPhone().contains(query)) {
                    filteredList.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }
    //Clears filteredList.
    //If the query is empty, resets filteredList to match contactList.
    //Filters contactList for contacts whose name or phone matches the query.
    //Calls notifyDataSetChanged() to refresh the RecyclerView.

    public void refreshList() {
        filteredList.clear();
        filteredList.addAll(contactList);
        notifyDataSetChanged();
    }
//Resets filteredList to match contactList and refreshes the view.
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone;

        public ContactViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            phone = itemView.findViewById(R.id.textPhone);
        }
    }
    //Represents each item view.
    //name and phone: TextViews displaying contact details.
    //Initializes views using findViewById.

    // OnContactClickListener interface
    public interface OnContactClickListener {
        void onContactClick(Contact contact, int position);
    }

    //handle item clicks, passing the clicked contact and its original position in contactList
}
