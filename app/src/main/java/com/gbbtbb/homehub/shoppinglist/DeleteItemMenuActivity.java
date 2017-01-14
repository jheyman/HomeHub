package com.gbbtbb.homehub.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.todolist.TodoListWidgetMain;

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

		// Register a callback for when the views have been layed out on the screen, and it is now safe to get their dimensions.
		final Window window = this.getWindow();
		final WindowManager.LayoutParams wmlp = this.getWindow().getAttributes();

		final LinearLayout ll = (LinearLayout)findViewById(R.id.DeleteItemPopupTopLayout);
		ll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {

				Rect r = new Rect();
				ll.getLocalVisibleRect(r);
				Log.i(TodoListWidgetMain.TAG, "DeleteItemMenuActivity LinearLayout bounds= " + r);

				ll.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int screen_width = size.x;
				int screen_height = size.y;

				// center pop-up horizontally
				wmlp.x = screen_width / 2 - r.width() / 2;

				// center pop-up vertically on the top 1/4 line, so that the virtual keyboard that will show up from the
				// bottom of the screen does not overlap us.
				wmlp.y = screen_height / 4 - r.height() / 2;

				wmlp.gravity = Gravity.TOP | Gravity.LEFT;
				window.setAttributes(wmlp);
			}
		});
	}

}
