package com.gbbtbb.homehub.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.todolist.TodoListWidgetMain;

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

		// Register a callback for when the views have been layed out on the screen, and it is now safe to get their dimensions.
		final Window window = this.getWindow();
		final WindowManager.LayoutParams wmlp = this.getWindow().getAttributes();

		final LinearLayout ll = (LinearLayout)findViewById(R.id.ShoppingAddItemTopLayout);
		ll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {

				Rect r = new Rect();
				ll.getLocalVisibleRect(r);
				Log.i(TodoListWidgetMain.TAG, "DeleteItemMenuActivity LinearLayout bounds= " + r);

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					ll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else
					ll.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int screen_width = size.x;
				int screen_height = size.y;

				wmlp.x = screen_width / 2 - r.width() / 2;
				wmlp.y = screen_height / 2 - r.height() / 2;

				wmlp.gravity = Gravity.TOP | Gravity.LEFT;
				window.setAttributes(wmlp);
			}
		});
	}
}
