package com.gbbtbb.homehub.todolist;

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

import com.gbbtbb.homehub.R;

public class CleanListMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.cleanlist_popup_menu);
		
		// Attach a pending broadcast intent to the Confirm button, that will get sent to TodoListWidgetMain
		Button buttonConfirm = (Button) findViewById(R.id.button_confirm);
		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(com.gbbtbb.homehub.todolist.CleanListMenuActivity.this, TodoListWidgetService.class);
				intent.setAction(TodoListWidgetMain.CLEANLIST_ACTION);
				com.gbbtbb.homehub.todolist.CleanListMenuActivity.this.startService(intent);

				// close this GUI activity
				finish();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TodoListWidgetMain.TAG, "CleanListMenuActivity: canceled delete list");

				Intent doneIntent = new Intent();
				doneIntent.setAction(TodoListWidgetMain.ACTION_CANCELLED);
				doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
				sendBroadcast(doneIntent);

				// Close this GUI activity
				finish();
			}
		});

		// Register a callback for when the views have been layed out on the screen, and it is now safe to get their dimensions.
		final Window window = this.getWindow();
		final WindowManager.LayoutParams wmlp = this.getWindow().getAttributes();

		final LinearLayout ll = (LinearLayout)findViewById(R.id.CleanListPopUpTopLayout);
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
