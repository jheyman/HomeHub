package com.gbbtbb.homehub.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.gbbtbb.homehub.R;

/*
 * Activity implementing the pop-up menu for adding an item to the shopping list
 */
public class AddItemMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Log.i(ShoppingListWidgetMain.TAG, "AddItemMenuActivity creation, source bounds= "+getIntent().getSourceBounds());

		setContentView(R.layout.additem_popup_menu);

		final EditText input =  (EditText)findViewById(R.id.textViewItemName);

		Button buttonConfirm = (Button) findViewById(R.id.button_confirm);

		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Editable value = input.getText();
				Log.i(ShoppingListWidgetMain.TAG, "AddItemMenuActivity: new item to add is " + value.toString());

				final Bundle extras = new Bundle();
				Intent intent = new Intent(AddItemMenuActivity.this, ShoppingListWidgetService.class);
				intent.setAction(ShoppingListWidgetMain.ADDITEM_ACTION);
				extras.putString(ShoppingListWidgetMain.EXTRA_ITEM_ID, value.toString());
				intent.putExtras(extras);
				AddItemMenuActivity.this.startService(intent);

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

		WindowManager.LayoutParams wmlp = this.getWindow().getAttributes();
		wmlp.gravity = Gravity.TOP | Gravity.LEFT;

		// Get the x,y coordinates of the button that triggered this dialog, and align the dialog to it.
		//Rect r = getIntent().getSourceBounds();

		wmlp.x = 0;//r.left;  //x position
		wmlp.y = 0;//r.top;   //y position
		this.getWindow().setAttributes(wmlp);  	
	}
}
