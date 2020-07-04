package com.mis.palittayo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewHolderAdapter extends RecyclerView.Adapter<ViewHolderAdapter.SectionViewHolder> {
    Context context;
    ArrayList<String> itemName;
    ArrayList<String> itemPrice;
    ArrayList<String> itemPicture;
    ArrayList<String> itemStatus;
    ArrayList<String> itemSeller;
    ArrayList<String> documentID;
    ArrayList<String> sellerID;
    ArrayList<String> itemTag;

    class SectionViewHolder extends RecyclerView.ViewHolder {
        ImageView item_picture;
        TextView itemName, itemPrice, itemStatus, itemSeller;
        RelativeLayout holder;

        public SectionViewHolder(View itemView) {
            super(itemView);
            item_picture = itemView.findViewById(R.id.item_picture);
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemStatus = itemView.findViewById(R.id.item_status);
            itemSeller = itemView.findViewById(R.id.seller_name);
            holder = itemView.findViewById(R.id.holder);
        }
    }
    public ViewHolderAdapter(Context context, ArrayList<String> itemName, ArrayList<String> itemPrice, ArrayList<String> itemPicture, ArrayList<String> itemStatus, ArrayList<String> itemSeller, ArrayList<String> documentID, ArrayList<String> sellerID, ArrayList<String> itemTag) {
        this.context = context;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemPicture = itemPicture;
        this.itemStatus = itemStatus;
        this.itemSeller = itemSeller;
        this.documentID = documentID;
        this.sellerID = sellerID;
        this.itemTag = itemTag;
    }

    @Override
    public ViewHolderAdapter.SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_panel, parent, false);
        return new ViewHolderAdapter.SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, final int position) {
        holder.itemName.setText(itemName.get(position));
        holder.itemSeller.setText(itemSeller.get(position));
        holder.itemStatus.setText(itemStatus.get(position));
        holder.itemPrice.setText(itemPrice.get(position));
        Glide.with(context)
                .load(itemPicture.get(position))
                .into(holder.item_picture);
        holder.holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: View items in full!
                if (itemStatus.get(position).contains("Available")){
                    //Item is available, intent must be done.
                    Intent view = new Intent(context, ItemContentActivity.class);
                    view.putExtra("sellerID", sellerID.get(position));
                    view.putExtra("documentID", documentID.get(position));
                    view.putExtra("itemPicture", itemPicture.get(position));
                    v.getContext().startActivity(view);
                }
                else{
                    //Sold!
                    Toast.makeText(context, "Sorry! This item was already sold.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemName.size();
    }
}
