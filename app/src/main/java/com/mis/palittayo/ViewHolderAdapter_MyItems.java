package com.mis.palittayo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

//TODO: 1. Put extends
public class ViewHolderAdapter_MyItems extends RecyclerView.Adapter<ViewHolderAdapter_MyItems.ViewHolder>{
//TODO: 2. Declare ArrayList and Context
    Context context;
    ArrayList<String> itemName;
    ArrayList<String> itemPrice;
    ArrayList<String> itemPicture;
    ArrayList<String> itemStatus;
    ArrayList<String> itemDesc;
    ArrayList<String> itemTag;
    ArrayList<String> documentID;

    class ViewHolder extends RecyclerView.ViewHolder {
    //TODO: 3. Declare objects in single_rows.xml
        ImageView my_items_picture, my_items_update,my_items_delete;
        TextView my_items_name, my_items_status, my_edit_label, my_delete_label;

        public ViewHolder(View itemView) {
            super(itemView);
            //TODO: 4. Find objects ID
          my_items_picture = itemView.findViewById(R.id.my_items_picture);
          my_items_update = itemView.findViewById(R.id.my_items_edit);
          my_items_delete = itemView.findViewById(R.id.my_items_delete);
          my_items_name = itemView.findViewById(R.id.my_item_name);
          my_items_status = itemView.findViewById(R.id.my_item_status);
          my_edit_label = itemView.findViewById(R.id.my_edit_label);
          my_delete_label = itemView.findViewById(R.id.my_delete_label);

        }
    }
    //TODO: 5. Constructor
    public ViewHolderAdapter_MyItems(Context context, ArrayList<String> itemName, ArrayList<String> itemPrice, ArrayList<String> itemPicture, ArrayList<String> itemStatus, ArrayList<String> itemDesc, ArrayList<String> documentID, ArrayList<String> itemTag) {
        this.context = context;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemPicture = itemPicture;
        this.itemStatus = itemStatus;
        this.itemDesc = itemDesc;
        this.documentID = documentID;
        this.itemTag = itemTag;
    }
    //TODO: 6. Copy Paste onCreateViewHolder Override
    //TODO: 6_1. R.layout.single_row_panel should be change
    @Override
    public ViewHolderAdapter_MyItems.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_item_view, parent, false);
        return new ViewHolderAdapter_MyItems.ViewHolder(view);
    }
    //TODO: 7. CopyPaste object manipulator
    @Override
    public void onBindViewHolder(ViewHolderAdapter_MyItems.ViewHolder holder, final int position) {
        //All objects functions can be manipulated here!
        holder.my_items_name.setText(itemName.get(position));
        holder.my_items_status.setText(itemStatus.get(position));
        Glide.with(getApplicationContext())
                .load(itemPicture.get(position))
                .into(holder.my_items_picture);
        holder.my_items_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsEdit(itemTag.get(position), documentID.get(position), itemName.get(position), itemPrice.get(position), itemDesc.get(position), itemPicture.get(position), itemStatus.get(position));
            }
        });
        holder.my_items_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsDelete(documentID.get(position), itemPicture.get(position));
            }
        });
        holder.my_edit_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsEdit(itemTag.get(position), documentID.get(position), itemName.get(position), itemPrice.get(position), itemDesc.get(position), itemPicture.get(position), itemStatus.get(position));
            }
        });
        holder.my_delete_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsDelete(documentID.get(position), itemPicture.get(position));
            }
        });

    }

    public void itemsEdit(String tags, String documentID, String name, String price, String desc, String picture, String status){
        //TODO: Edit section
        Intent goEdit = new Intent(context, UpdateItemsActivity.class);
        goEdit.putExtra("documentID", documentID);
        goEdit.putExtra("name", name);
        goEdit.putExtra("price", price);
        goEdit.putExtra("desc", desc);
        goEdit.putExtra("picture", picture);
        goEdit.putExtra("status", status);
        goEdit.putExtra("tags", tags);
        context.startActivity(goEdit);

    }
    public void itemsDelete(final String documentID, final String picturePath){
        //TODO: Delete section
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage("Deleting item...");
                        progressDialog.setTitle("Please Wait...");
                        progressDialog.show();
                        final FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                        final FirebaseStorage delRef = FirebaseStorage.getInstance().getReference().getStorage();
                        StorageReference photoRef = delRef.getReferenceFromUrl(picturePath);
                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FireInstance.collection("posts").document(documentID)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context, "Deleted successfully.", Toast.LENGTH_LONG).show();
                                                Intent go = new Intent(context, ViewItemsActivity.class);
                                                go.putExtra("loadMyItems", "OK");
                                                context.startActivity(go);
                                            }
                                        });
                            }
                        });

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this item?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    //TODO: 8. Lastly, return counts by arraylist sizes.
    @Override
    public int getItemCount() {
        return itemName.size();
    }


}


