package com.example.feco.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodoFragment extends Fragment {
    private List<TodoElement> todo_list;
    private ListView listView;
    private ImageView todoButton;
    private EditText bevitelText;
    private TodoAdapter todoAdapter = new TodoAdapter();

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String mUserId;



    public TodoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_todo, container, false);
        mAuth = FirebaseAuth.getInstance();
        mUserId = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        listView = (ListView) view.findViewById(R.id.todo_listview);
        todoButton = (ImageView) view.findViewById(R.id.todo_submit_btn);
        bevitelText = (EditText) view.findViewById(R.id.todo_bevitel);
        todo_list = new ArrayList<>();
        listView.setAdapter(todoAdapter);

        // todo lista lekérése az adatbázisból

        firebaseFirestore.collection("Todo").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("FECO", "Hiba " + e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Boolean keszV = doc.getDocument().getBoolean("kesz");
                            String leirasV = doc.getDocument().getString("leiras");
                            String docId = doc.getDocument().getId();
                            TodoElement todo = new TodoElement(keszV,leirasV, docId);
                            todo_list.add(todo);
                            todoAdapter.notifyDataSetChanged();
                        }
                    }
                }

            }
        });


        // todo bepakolása az AB-ba és a lsitába

        todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String be = bevitelText.getText().toString().trim();
                if (!TextUtils.isEmpty(be)) {
                    Map<String, Object> todoMap = new HashMap<>();
                    todoMap.put("kesz", false);
                    todoMap.put("leiras", be);

                    firebaseFirestore.collection("Todo").add(todoMap);

                }
            }
        });

        return view;
    }

    class TodoElement {
        private Boolean kesz;
        private String leiras;
        private String docId;

        public TodoElement() {
        }

        public TodoElement(Boolean kesz, String leiras, String docId) {
            this.kesz = kesz;
            this.leiras = leiras;
            this.docId=docId;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public Boolean getKesz() {
            return kesz;
        }

        public void setKesz(Boolean kesz) {
            this.kesz = kesz;
        }

        public String getLeiras() {
            return leiras;
        }

        public void setLeiras(String leiras) {
            this.leiras = leiras;
        }
    }

    class TodoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return todo_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View mView, ViewGroup parent) {
            mView = getLayoutInflater().inflate(R.layout.todo_list_item, null);

            final Switch keszGomb = (Switch) mView.findViewById(R.id.todo_list_switch);
            TextView todoSzoveg = (TextView) mView.findViewById(R.id.todo_list_text);

            keszGomb.setChecked(todo_list.get(position).getKesz());
            todoSzoveg.setText(todo_list.get(position).getLeiras());
            final String docId = todo_list.get(position).getDocId();

            keszGomb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> todoMap = new HashMap<>();
                    todoMap.put("kesz", keszGomb.isChecked());
                    todoMap.put("leiras", todo_list.get(position).getLeiras());

                    firebaseFirestore.collection("Todo").document(docId).set(todoMap);
                }
            });

            return mView;
        }
    }
}