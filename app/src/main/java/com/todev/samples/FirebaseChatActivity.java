package com.todev.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;

public class FirebaseChatActivity extends AppCompatActivity {

  public static final String FIREBASE_URL = BuildConfig.FIREBASE_URL;
  public static final int MESSAGES_LIMIT = 5;

  private Firebase ref;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_firebase);

    Firebase.setAndroidContext(this);
    ref = new Firebase(FIREBASE_URL);

    final EditText editTextMessage = (EditText) findViewById(R.id.edit_text_message);
    final ImageButton buttonSend = (ImageButton) findViewById(R.id.button_send);
    final ListView listViewMessages = (ListView) findViewById(R.id.list_view_messages);

    buttonSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage(new Message(editTextMessage.getText().toString()));
        editTextMessage.setText("");
      }
    });

    Query recent = ref.child("messages").limitToLast(MESSAGES_LIMIT);

    FirebaseListAdapter<Message> adapter =
        new FirebaseListAdapter<Message>(this, Message.class, android.R.layout.simple_list_item_1, recent) {
          @Override
          protected void populateView(View view, Message model, int position) {
            final TextView item = (TextView) view.findViewById(android.R.id.text1);
            item.setText(model.getMessage());
          }
        };

    listViewMessages.setAdapter(adapter);
  }

  private void sendMessage(Message message) {
    ref.child("messages").push().setValue(message);
  }

  static class Message {
    private String message;

    public Message() {
      // Required.
    }

    public Message(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
