package com.mis.palittayo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewItemsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    RecyclerView itemsList;
    EditText search_bar;
    FirebaseFirestore FireInstance;
    ArrayList<String> itemName;
    ArrayList<String> itemPrice;
    ArrayList<String> itemPicture;
    ArrayList<String> itemStatus;
    ArrayList<String> itemSeller;
    ArrayList<String> itemDesc;
    ArrayList<String> documentID;
    ArrayList<String> sellerID;
    ArrayList<String> itemTag;
    ViewHolderAdapter vHa;
    ViewHolderAdapter_MyItems myvHa;
    Integer choosen; //TODO: 1 = All Items, 2 = Find Something, 3 = My Items
    Integer sort;
    String loadName, loadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        itemName = new ArrayList<>();
        itemTag = new ArrayList<>();
        itemPrice = new ArrayList<>();
        itemSeller = new ArrayList<>();
        itemPicture = new ArrayList<>();
        itemStatus = new ArrayList<>();
        documentID = new ArrayList<>();
        itemDesc = new ArrayList<>();
        sellerID = new ArrayList<>();
        FireInstance  = FirebaseFirestore.getInstance();
        search_bar = findViewById(R.id.search_bar);
        itemsList = findViewById(R.id.itemList);
        itemsList.setHasFixedSize(true);
        itemsList.setLayoutManager(new LinearLayoutManager(this));
        itemsList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        choosen = 1;
        sort = 1;
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipelayout2);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh,R.color.refresh1,R.color.refresh2);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if (choosen == 1){
                    loadItemList();
                }
                else if(choosen == 2){
                    loadMyItems();
                }
                else if(choosen == 3){
                    loadCustomList(getIntent().getExtras().getString("userID"));
                }
                swipeRefreshLayout.setRefreshing(false);
            }

        });

        if(getIntent().hasExtra("loadMyItems")) {
            if (getIntent().getExtras().getString("loadMyItems").equals("OK")){
                choosen = 2;
            }
        }
        if(getIntent().hasExtra("loadUserItems")) {
            if (getIntent().getExtras().getString("loadUserItems").equals("OK")){
                loadName = getIntent().getExtras().getString("userName");
                loadID = getIntent().getExtras().getString("userID");
                choosen = 3;
            }
        }
        if (choosen == 1) {
            loadItemList();
            setTitle("Browse Items");
        }
        else if (choosen == 2){
            loadMyItems();
            setTitle("My Items");
        }
        else if (choosen == 3){
            loadCustomList(loadID);
            setTitle(loadName+"'s Items");
        }
        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: ADD ITEM ACTIVITY
                Intent add = new Intent(ViewItemsActivity.this, AddItemActivity.class);
                startActivity(add);
            }
        });
        search_bar.addTextChangedListener(new TextWatcher() {
                                              @Override
                                              public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                              @Override
                                              public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                              @Override
                                              public void afterTextChanged(Editable s) {
                                                  if (!s.toString().isEmpty()) {
                                                      clearAllItems();
                                                      searchItemName(s.toString());
                                                  }
                                                  else{
                                                      if (choosen == 1) { loadItemList(); }
                                                      if (choosen == 3) { loadCustomList(getIntent().getExtras().getString("userID")); }

                                                  }
                                              }
                                          });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }
    public void loadCustomList(String userUID){
        CollectionReference postRef = FireInstance.collection("posts");
        Query query = postRef.whereEqualTo("sellerID", userUID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    clearAllItems();
                    for(DocumentSnapshot document : task.getResult()) {
                        itemName.add(""+document.getString("itemName"));
                        itemSeller.add("Seller: "+document.getString("sellerName"));
                        itemPicture.add(document.getString("itemPicture"));
                        itemPrice.add("Price: "+document.getString("itemPrice"));
                        itemStatus.add("Status: "+document.getString("itemStatus"));
                        sellerID.add(document.getString("sellerID"));
                        itemTag.add(document.getString("itemTags"));
                        documentID.add(document.getId());
                    }
                    vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID, itemTag);
                    itemsList.setAdapter(vHa);

                }
            }
        });

    }
    public void clearAllItems(){
        itemName.clear();
        itemSeller.clear();
        itemPrice.clear();
        itemPicture.clear();
        itemStatus.clear();
        documentID.clear();
        sellerID.clear();
        itemTag.clear();
        itemDesc.clear();
        itemsList.removeAllViews();
    }
    private void searchItemName(final String searchedString) {
        if(choosen == 1){
            CollectionReference postRef = FireInstance.collection("posts");
            Query query = postRef.orderBy("itemName", Query.Direction.ASCENDING);
            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            clearAllItems();
                            for(DocumentSnapshot document : queryDocumentSnapshots){
                                String getItemName = document.getString("itemName");
                                String getItemTag = document.getString("itemTags");
                                if(getItemName.toLowerCase().contains(searchedString.toLowerCase()) || getItemTag.toLowerCase().contains(searchedString.toLowerCase())){
                                    itemName.add(""+document.getString("itemName"));
                                    itemSeller.add("Seller: "+document.getString("sellerName"));
                                    itemPicture.add(document.getString("itemPicture"));
                                    itemPrice.add("Price: "+document.getString("itemPrice"));
                                    itemStatus.add("Status: "+document.getString("itemStatus"));
                                    sellerID.add(document.getString("sellerID"));
                                    itemTag.add(document.getString("itemTags"));
                                    documentID.add(document.getId());
                                }

                            }
                            vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID, itemTag);
                            itemsList.setAdapter(vHa);
                        }
                    });
        }
        else if (choosen == 2){
        //TODO: CHOOSEN : 2
            FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
            FireInstance.collection("posts")
                    .whereEqualTo("sellerID", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            clearAllItems();
                            for(DocumentSnapshot document : queryDocumentSnapshots){
                                String getItemName = document.getString("itemName");
                                String getItemTag = document.getString("itemTags");
                                if(getItemName.toLowerCase().contains(searchedString.toLowerCase()) || getItemTag.toLowerCase().contains(searchedString.toLowerCase())){
                                    itemName.add(""+document.getString("itemName"));
                                    itemPicture.add(document.getString("itemPicture"));
                                    itemPrice.add("Price: "+document.getString("itemPrice"));
                                    itemStatus.add("Status: "+document.getString("itemStatus"));
                                    itemDesc.add(document.getString("itemContent"));
                                    itemTag.add(document.getString("itemTags"));
                                    documentID.add(document.getId());
                                }

                            }
                            myvHa = new ViewHolderAdapter_MyItems(ViewItemsActivity.this, itemName,itemPrice,itemPicture,itemStatus, itemDesc, documentID, itemTag);
                            itemsList.setAdapter(myvHa);
                        }
                    });

        }
        else if(choosen == 3){
            FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
            FireInstance.collection("posts")
                    .whereEqualTo("sellerID", loadID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            clearAllItems();
                            for(DocumentSnapshot document : queryDocumentSnapshots){
                                String getItemName = document.getString("itemName");
                                String getItemTag = document.getString("itemTags");
                                if(getItemName.toLowerCase().contains(searchedString.toLowerCase()) || getItemTag.toLowerCase().contains(searchedString.toLowerCase())){
                                    itemName.add(""+document.getString("itemName"));
                                    itemSeller.add("Seller: "+document.getString("sellerName"));
                                    itemPicture.add(document.getString("itemPicture"));
                                    itemPrice.add("Price: "+document.getString("itemPrice"));
                                    itemStatus.add("Status: "+document.getString("itemStatus"));
                                    sellerID.add(document.getString("sellerID"));
                                    itemTag.add(document.getString("itemTags"));
                                    documentID.add(document.getId());
                                }

                            }
                            vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID, itemTag);
                            itemsList.setAdapter(vHa);
                        }
                    });

        }


    }
    private void loadItemList() {
        if (sort == 1){
            CollectionReference postRef = FireInstance.collection("posts");
            Query query = postRef.orderBy("itemName", Query.Direction.ASCENDING);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        clearAllItems();
                        for(DocumentSnapshot document : task.getResult()) {
                            itemName.add(""+document.getString("itemName"));
                            itemSeller.add("Seller: "+document.getString("sellerName"));
                            itemPicture.add(document.getString("itemPicture"));
                            itemPrice.add("Price: "+document.getString("itemPrice"));
                            itemStatus.add("Status: "+document.getString("itemStatus"));
                            sellerID.add(document.getString("sellerID"));
                            itemTag.add(document.getString("itemTags"));
                            documentID.add(document.getId());
                        }
                        vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID, itemTag);
                        itemsList.setAdapter(vHa);

                    }
                }
            });

        }
        else if(sort == 2){
            CollectionReference postRef = FireInstance.collection("posts");
            Query query = postRef.orderBy("itemName", Query.Direction.DESCENDING);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        clearAllItems();
                        for(DocumentSnapshot document : task.getResult()) {
                            itemName.add(""+document.getString("itemName"));
                            itemSeller.add("Seller: "+document.getString("sellerName"));
                            itemPicture.add(document.getString("itemPicture"));
                            itemPrice.add("Price: "+document.getString("itemPrice"));
                            itemStatus.add("Status: "+document.getString("itemStatus"));
                            sellerID.add(document.getString("sellerID"));
                            itemTag.add(document.getString("itemTags"));
                            documentID.add(document.getId());
                        }
                        vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID,itemTag);
                        itemsList.setAdapter(vHa);

                    }
                }
            });

        }
        else if(sort == 3){
            CollectionReference postRef = FireInstance.collection("posts");
            Query query = postRef.orderBy("price", Query.Direction.ASCENDING);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        clearAllItems();
                        for(DocumentSnapshot document : task.getResult()) {
                            itemName.add(""+document.getString("itemName"));
                            itemSeller.add("Seller: "+document.getString("sellerName"));
                            itemPicture.add(document.getString("itemPicture"));
                            itemPrice.add("Price: "+document.getString("itemPrice"));
                            itemStatus.add("Status: "+document.getString("itemStatus"));
                            sellerID.add(document.getString("sellerID"));
                            itemTag.add(document.getString("itemTags"));
                            documentID.add(document.getId());
                        }
                        vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID, itemTag);
                        itemsList.setAdapter(vHa);

                    }
                }
            });

        }
        else if(sort == 4){
            CollectionReference postRef = FireInstance.collection("posts");
            Query query = postRef.orderBy("price", Query.Direction.DESCENDING);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        clearAllItems();
                        for(DocumentSnapshot document : task.getResult()) {
                            itemName.add(""+document.getString("itemName"));
                            itemSeller.add("Seller: "+document.getString("sellerName"));
                            itemPicture.add(document.getString("itemPicture"));
                            itemPrice.add("Price: "+document.getString("itemPrice"));
                            itemStatus.add("Status: "+document.getString("itemStatus"));
                            sellerID.add(document.getString("sellerID"));
                            itemTag.add(document.getString("itemTags"));
                            documentID.add(document.getId());
                        }
                        vHa = new ViewHolderAdapter(ViewItemsActivity.this, itemName, itemPrice, itemPicture, itemStatus, itemSeller, documentID, sellerID, itemTag);
                        itemsList.setAdapter(vHa);

                    }
                }
            });

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (choosen == 1){
            if (id == R.id.name_asc) {
                //TODO: Name Ascending
                sort = 1;
                loadItemList();
            }
            if (id == R.id.name_desc) {
                //TODO: Name Descending
                sort = 2;
                loadItemList();
            }
            if (id == R.id.price_asc) {
                //TODO: Price Ascending
                sort = 3;
                loadItemList();
            }
            if (id == R.id.price_desc) {
                //TODO: Name Descending
                sort = 4;
                loadItemList();
            }
        }
        else{
            Toast.makeText(this, "Feature not available at the moment!", Toast.LENGTH_SHORT).show();
        }
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       if (id == R.id.nav_allItems) {
            loadItemList();
            choosen = 1;
           setTitle("Browse Items");
        } else if (id == R.id.nav_myItems) {
            loadMyItems();
            choosen = 2;
           setTitle("My Items");
        } else if (id == R.id.nav_viewProfile) {
            Intent go = new Intent(ViewItemsActivity.this, ProfileActivity.class);
            go.putExtra("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(go);
        } else if (id == R.id.nav_manage) {
            Intent editProfile = new Intent(ViewItemsActivity.this, EditProfileActivity.class);
            startActivity(editProfile);
        } else if (id == R.id.nav_logout) {
            Toast.makeText(ViewItemsActivity.this, "See you later, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "!", Toast.LENGTH_SHORT).show();
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent goLogin = new Intent(ViewItemsActivity.this, SplashScreen.class);
                            startActivity(goLogin);
                        }
                    });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadMyItems() {
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FireInstance.collection("posts")
                .whereEqualTo("sellerID", userUID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            clearAllItems();
                            for(DocumentSnapshot document : queryDocumentSnapshots) {
                                itemName.add(""+document.getString("itemName"));
                                itemPicture.add(document.getString("itemPicture"));
                                itemPrice.add("Price: "+document.getString("itemPrice"));
                                itemStatus.add("Status: "+document.getString("itemStatus"));
                                itemDesc.add(document.getString("itemContent"));
                                itemTag.add(document.getString("itemTags"));
                                documentID.add(document.getId());
                            }
                            myvHa = new ViewHolderAdapter_MyItems(ViewItemsActivity.this, itemName,itemPrice,itemPicture,itemStatus, itemDesc, documentID, itemTag);
                            itemsList.setAdapter(myvHa);


                    }
                });
    }
}
