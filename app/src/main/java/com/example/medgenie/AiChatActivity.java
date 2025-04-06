package com.example.medgenie;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText messageInput;
    private ImageButton sendButton;
    private FirebaseFirestore firestore;
    private String userId;

    private static final String API_KEY = "Bearer YOUR_API_KEY_HERE"; // Replace with actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_chat_screen); // Ensure this layout exists

        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            }
        });

        loadChatHistory();
    }

    private void loadChatHistory() {
        firestore.collection("users").document(userId).collection("chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        chatMessages.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                });
    }

    private void sendMessage(String userMessage) {
        ChatMessage userChat = new ChatMessage(userMessage, "user");

        chatMessages.add(userChat);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        saveMessageToFirestore(userChat);

        GeminiRequest request = new GeminiRequest(userMessage);
        GeminiApi api = GeminiClient.getClient();

        api.generateContent(API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeminiResponse> call, @NonNull Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().candidates.isEmpty()) {
                    String aiReply = response.body().candidates.get(0).content.parts.get(0).text;
                    ChatMessage aiMessage = new ChatMessage(aiReply, "ai");
                    chatMessages.add(aiMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                    saveMessageToFirestore(aiMessage);
                } else {
                    Log.e("Gemini", "Failed response: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeminiResponse> call, @NonNull Throwable t) {
                Log.e("Gemini", "API call failed: " + t.getMessage());
            }
        });
    }

    private void saveMessageToFirestore(ChatMessage message) {
        firestore.collection("users").document(userId).collection("chats")
                .add(message)
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving message", e));
    }
}
