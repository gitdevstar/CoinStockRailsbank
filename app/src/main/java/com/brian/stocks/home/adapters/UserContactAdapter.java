package com.brian.stocks.home.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brian.stocks.R;
import com.brian.stocks.model.ContactUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserContactAdapter extends RecyclerView.Adapter<UserContactAdapter.OrderViewHolder> {
    private List<ContactUser> arrItems;
    private Listener listener;

    public UserContactAdapter(List<ContactUser> arrItems) {
        this.arrItems = arrItems;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvContactName, tvContactEmail;


        public OrderViewHolder(View view) {
            super(view);

            tvContactName = view.findViewById(R.id.contact_name);
            tvContactEmail = view.findViewById(R.id.contact_email);

        }
    }

    @NonNull
    @Override
    public UserContactAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_coin, parent, false);
        UserContactAdapter.OrderViewHolder vh = new UserContactAdapter.OrderViewHolder(mView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserContactAdapter.OrderViewHolder holder, final int position) {
        ContactUser item = arrItems.get(position);

        holder.tvContactName.setText(item.getName());
        holder.tvContactEmail.setText(item.getEmail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSelect(position);
            }
        });

    }

    public void setListener(UserContactAdapter.Listener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return arrItems != null ? arrItems.size(): 0;
    }

    public Object getItem(int i) {
        return arrItems.get(i);
    }

    public interface Listener {
        /**
         * @param position
         */
        void onSelect(int position);
    }
}

