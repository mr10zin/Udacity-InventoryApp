package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ItemContract;

/**
 * Created by Khedup on 1/6/2018.
 */

public class ItemCursorAdapter extends CursorAdapter {
    private Context mContexts;
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContexts = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //load views
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        //find
        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int quantColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);

        final String itemName = cursor.getString(nameColumnIndex);
        final String itemQuant = cursor.getString(quantColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        final int id = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry._ID));

        nameTextView.setText(itemName);
        quantTextView.setText(itemQuant);
        priceTextView.setText(itemPrice);


        final Uri uri = ItemContract.ItemEntry.buildItemUri(id);

        Button mButton = (Button) view.findViewById(R.id.sale);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if(Integer.parseInt(itemQuant) > 0){
                    int newQuant = Integer.parseInt(itemQuant) - 1;

                    values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY,newQuant);
                    resolver.update(uri,
                            values,
                            null,
                            null);
                    mContexts.getContentResolver().notifyChange(uri, null);
                }
            }
        });
    }
}
