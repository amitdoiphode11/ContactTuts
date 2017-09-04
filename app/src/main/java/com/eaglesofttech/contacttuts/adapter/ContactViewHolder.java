package com.eaglesofttech.contacttuts.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eaglesofttech.contacttuts.R;
import com.eaglesofttech.contacttuts.activity.ContactList;
import com.squareup.picasso.Picasso;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    ImageView ivContactImage;
    TextView tvContactName;
    TextView tvPhoneNumber;
    ContactList contactList;
    Context context;

    public ContactViewHolder(Context context, final View itemView) {
        super(itemView);
        this.context = context;
        contactList = new ContactList();
        ivContactImage = (ImageView) itemView.findViewById(R.id.ivContactImage);
        tvContactName = (TextView) itemView.findViewById(R.id.tvContactName);
        tvPhoneNumber = (TextView) itemView.findViewById(R.id.tvPhoneNumber);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }


    public void bind(Contact contact) {

        tvContactName.setText(contact.getContactName());
        tvPhoneNumber.setText(contact.getContactNumber());
        Picasso.with(itemView.getContext())
                .load(contact.getContactImage())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(ivContactImage);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View v) {
        contactList.showPopup(1, tvContactName.getText().toString(), tvPhoneNumber.getText().toString());
        return false;
    }

}