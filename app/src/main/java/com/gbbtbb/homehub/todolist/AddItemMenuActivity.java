package com.gbbtbb.homehub.todolist;

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
import android.widget.RadioButton;

import com.gbbtbb.homehub.R;

/*
 * Activity implementing the pop-up menu for adding an item to the todo list
 */
public class AddItemMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.todolist_additem_popup_menu);

		final EditText input =  (EditText)findViewById(R.id.textViewItemName);
		final RadioButton rb_prio1 = (RadioButton)findViewById(R.id.button_priority1);
		final RadioButton rb_prio2 = (RadioButton)findViewById(R.id.button_priority2);
		final RadioButton rb_prio3 = (RadioButton)findViewById(R.id.button_priority3);

		Button buttonConfirm = (Button) findViewById(R.id.button_confirm);

		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Editable value = input.getText();
				Log.i(TodoListWidgetMain.TAG, "AddItemMenuActivity: new item to add is " + value.toString());

                boolean prio1=rb_prio1.isChecked();
                boolean prio2=rb_prio2.isChecked();
                boolean prio3=rb_prio3.isChecked();

				int priority = prio1 ? 1: (prio2 ? 2 : (prio3 ? 3: 0));

				final Bundle extras = new Bundle();
				Intent intent = new Intent(com.gbbtbb.homehub.todolist.AddItemMenuActivity.this, TodoListWidgetService.class);
				intent.setAction(TodoListWidgetMain.ADDITEM_ACTION);
				extras.putString(TodoListWidgetMain.EXTRA_ITEM_ID, value.toString());
				intent.putExtra(TodoListWidgetMain.EXTRA_ITEM_PRIO, priority);
				intent.putExtras(extras);
				com.gbbtbb.homehub.todolist.AddItemMenuActivity.this.startService(intent);

				finish();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.button_cancel);

		buttonCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TodoListWidgetMain.TAG, "Canceled delete item");

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

        final LinearLayout ll = (LinearLayout)findViewById(R.id.TodoAddItemTopLayout);
        ll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {

                Rect r = new Rect();
                ll.getLocalVisibleRect(r);
                //Log.i(TodoListWidgetMain.TAG, "AddItemMenuActivity LinearLayout bounds= " + r);

                ll.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screen_width = size.x;
                int screen_height = size.y;

                wmlp.x = screen_width/2 - r.width()/2;
                wmlp.y = screen_height/2 - r.height()/2;

                wmlp.gravity = Gravity.TOP | Gravity.LEFT;
                window.setAttributes(wmlp);
            }
        });

	}
}
