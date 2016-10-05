package com.gbbtbb.homehub.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.gbbtbb.homehub.R;

public class DeleteItemMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Log.i(ShoppingListWidgetMain.TAG, "PopupMenuActivity creation, source bounds= "+getIntent().getSourceBounds());

		Bundle b = getIntent().getExtras();
		final String itemName = b.getString(ShoppingListWidgetMain.EXTRA_ITEM_ID);
        final int position = b.getInt(ShoppingListWidgetMain.EXTRA_DELETEDITEM_POSITION);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.deleteitem_popup_menu);

		TextView t =  (TextView)findViewById(R.id.textViewItemName);
		t.setText("Effacer \""+itemName+"\"");

		Button buttonConfirm = (Button) findViewById(R.id.button_confirm);
		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				final Bundle extras = new Bundle();
				Intent intent = new Intent(DeleteItemMenuActivity.this, ShoppingListWidgetService.class);
				intent.setAction(ShoppingListWidgetMain.DELETEITEM_ACTION);
				extras.putString(ShoppingListWidgetMain.EXTRA_ITEM_ID, itemName);
                extras.putInt(ShoppingListWidgetMain.EXTRA_DELETEDITEM_POSITION, position);
				intent.putExtras(extras);
				DeleteItemMenuActivity.this.startService(intent);

				// Close this GUI activity, like a modal dialog would upon clicking on confirmation buttons.
				finish();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(ShoppingListWidgetMain.TAG, "Canceled delete item");

                Intent doneIntent = new Intent();
                doneIntent.setAction(ShoppingListWidgetMain.ACTION_CANCELLED);
                doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
                sendBroadcast(doneIntent);

				// Close this GUI activity
				finish();
			}
		});	
	}

}
