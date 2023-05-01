package edu.uga.cs.roommateshopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoommatesAdapter extends RecyclerView.Adapter<RoommatesAdapter.ViewHolder> {

    private List<String> roommates;

    public RoommatesAdapter(List<String> roommates) {
        this.roommates = roommates;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roommate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String roommate = roommates.get(position);
        holder.nameTextView.setText(roommate);
    }

    @Override
    public int getItemCount() {
        return roommates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
        }
    }
}

