package com.gbbtbb.homehub.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

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
	}

}
