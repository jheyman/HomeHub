package com.gbbtbb.homehub.todolist;

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

		//Log.i(TodoListWidgetMain.TAG, "PopupMenuActivity creation, source bounds= "+getIntent().getSourceBounds());

		Bundle b = getIntent().getExtras();
		final String itemName = b.getString(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_ITEM_ID);
        final int position = b.getInt(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_DELETEDITEM_POSITION);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.deleteitem_popup_menu);

		TextView t =  (TextView)findViewById(R.id.textViewItemName);
		t.setText("Effacer \""+itemName+"\"");

		Button buttonConfirm = (Button) findViewById(R.id.button_confirm);
		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				final Bundle extras = new Bundle();
				Intent intent = new Intent(com.gbbtbb.homehub.todolist.DeleteItemMenuActivity.this, TodoListWidgetService.class);
				intent.setAction(com.gbbtbb.homehub.todolist.TodoListWidgetMain.DELETEITEM_ACTION);
				extras.putString(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_ITEM_ID, itemName);
                extras.putInt(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_DELETEDITEM_POSITION, position);
				intent.putExtras(extras);
				com.gbbtbb.homehub.todolist.DeleteItemMenuActivity.this.startService(intent);

				// Close this GUI activity, like a modal dialog would upon clicking on confirmation buttons.
				finish();
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(com.gbbtbb.homehub.todolist.TodoListWidgetMain.TAG, "Canceled delete item");

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
