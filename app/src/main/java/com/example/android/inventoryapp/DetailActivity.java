package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;

import java.io.ByteArrayOutputStream;

/**
 * Created by Khedup on 1/6/2018.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    private EditText mNameEditText;
    private Button mQuantIncButton;
    private Button mQuantDecButton;
    private EditText mPriceEditText;
    private TextView mQuantText;
    private EditText mPhoneEditText;
    private Button mDailButton;
    private ImageView mIcon;
    private Button mCamButton;
    //dialogue flag
    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        //assign views
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantText = (TextView) findViewById(R.id.quantity_value);
        mQuantIncButton = (Button) findViewById(R.id.edit_quant_inc);
        mQuantDecButton = (Button) findViewById(R.id.edit_quant_dec);
        mPhoneEditText = (EditText) findViewById(R.id.edit_phone_num);
        mDailButton = (Button) findViewById(R.id.dial);
        mIcon = (ImageView) findViewById(R.id.icon);
        mCamButton = (Button) findViewById(R.id.camButton);


        //parse intent
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle("Add an Item");
            invalidateOptionsMenu();
            mDailButton.setVisibility(View.GONE);
        } else {
            setTitle("Edit Item");
            mDailButton.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);

        }

        mCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        mQuantDecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView quant = (TextView) findViewById(R.id.quantity_value);
                if (quant.getText().toString().isEmpty()) {
                    return;
                } else {
                    int q = Integer.parseInt(quant.getText().toString());
                    if (q == 0) {
                        Toast.makeText(getApplicationContext(), "Error: Quantity Cannot be negative.", Toast.LENGTH_SHORT).show();
                    } else {
                        q--;
                        String qText = Integer.toString(q);
                        quant.setText(qText);
                    }
                }
            }
        });

        mQuantIncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView quant = (TextView) findViewById(R.id.quantity_value);

                if (quant.getText().toString().isEmpty()) {
                    quant.setText("1");
                } else {
                    int q = Integer.parseInt(quant.getText().toString());
                    q++;
                    String qText = Integer.toString(q);
                    quant.setText(qText);
                }
            }
        });

        //check if edits made
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantDecButton.setOnTouchListener(mTouchListener);
        mQuantIncButton.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);

        mDailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String dailUri = "tel:"+mPhoneEditText.getText().toString().trim();
              Intent dailIntent = new Intent(Intent.ACTION_DIAL);
              dailIntent.setData(Uri.parse(dailUri));
              startActivity(dailIntent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        mIcon.setImageBitmap(bitmap);
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }
    // convert from byte array to bitmap



    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantString = mQuantText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String contactString = mPhoneEditText.getText().toString().trim();



        if (mCurrentItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(contactString)) {
            return;
        }

        if( TextUtils.isEmpty(nameString)|| TextUtils.isEmpty(priceString) || TextUtils.isEmpty(contactString)){
            Toast.makeText(this,"Item Detail not complete \nItem not saved",Toast.LENGTH_LONG).show();
            return;
        }

        if(mIcon.getDrawable() == null){
            Toast.makeText(this,"Item Detail not complete \nItem not saved",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(quantString)){
            quantString = "0";
        }
        Bitmap bitmap = ((BitmapDrawable) mIcon.getDrawable()).getBitmap();
        byte[] imgData = getBytes(bitmap);


        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME,nameString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PIC,imgData);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY,quantString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE,priceString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_CONTACT,contactString);


        Log.v("TEST", "Image: "+ imgData.toString());
        //new user.
        if(mCurrentItemUri == null){
            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI,values);

            if (newUri == null){
                Toast.makeText(this,"Error saving item",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Item Saved",Toast.LENGTH_SHORT).show();
            }
        }else{
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if(rowsAffected == 0){
                Toast.makeText(this,"Error updating item",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Item updated",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:

                if(!mItemHasChanged){
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard our changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void deleteItem() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error Deleting Item",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Error Deleting Item",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemContract.ItemEntry.COLUMN_ITEM_PIC,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_CONTACT};

        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int quantColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            int contactColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_CONTACT);
            int picColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PIC);


            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quant = cursor.getInt(quantColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int phone = cursor.getInt(contactColumnIndex);
            byte[] image = cursor.getBlob(picColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantText.setText(Integer.toString(quant));
            mPriceEditText.setText(Integer.toString(price));
            mPhoneEditText.setText(Integer.toString(phone));
            Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
            mIcon.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantIncButton.setText((""));
        mPriceEditText.setText((""));
        mPhoneEditText.setText((""));

    }
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
