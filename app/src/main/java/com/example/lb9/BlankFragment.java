package com.example.lb9;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private final String DIR_NAME="dir1";

    private ActivityResultLauncher<Intent> lauch;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String FILE_SETTINGS = "UriLink";
    private final String URI = "uri";

    private String filename;
    EditText editText;
    private Uri currentUri;

    public BlankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

                lauch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result ->{
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent data = result.getData();
                        if(data!=null)
                        {
                            Uri uri = data.getData();
                            if(uri != null)
                            {
                                currentUri = uri;
                                readFile(uri);
                            }
                        }
                    }
                });
        return view;
    }
    private void openFIlePicker()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"text/plain"});
        lauch.launch(intent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences file = getContext().getSharedPreferences(FILE_SETTINGS,MODE_PRIVATE);
        String uriString = file.getString(URI, null);
        if(uriString !=null) currentUri = Uri.parse(uriString);
        if(currentUri != null) readFile(currentUri);

        editText = view.findViewById(R.id.editText);
        Button opemBtn = view.findViewById(R.id.openBtn);
        opemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFIlePicker();
            }
        });



        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(currentUri == null) return;
                try {
                    OutputStream os = getContext().getContentResolver().openOutputStream(currentUri);
                    byte []byffer = editText.getText().toString().getBytes();
                    os.write(byffer, 0 ,byffer.length);
                    os.close();
                }
                catch (Exception e) {
                    Toast.makeText(getContext(), "Не возможно записать в файл", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void readFile(Uri uri)
    {
        SharedPreferences file = getContext().getSharedPreferences(FILE_SETTINGS,MODE_PRIVATE);
        SharedPreferences.Editor editor = file.edit();
        editor.putString(URI,uri.toString());
        editor.apply();
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line= reader.readLine())!=null)
            {
                stringBuilder.append(line);
            }
            String content = stringBuilder.toString();
            editText.setText(content);
            is.close();
            reader.close();
        }
        catch (Exception ex)
        {
            Toast.makeText(getContext(), "Ошибка отрытия файла", Toast.LENGTH_SHORT).show();
        }

    }
    private File getFile()
    {
        File file = null;
        File directory;
        try {
            if (DIR_NAME.isEmpty()) {
                directory = getContext().getFilesDir();
                getContext().getExternalFilesDir(DIR_NAME);
            }
            else {
                directory = getContext().getDir(DIR_NAME, MODE_PRIVATE);
            }
            File[] files = directory.listFiles();
            for (File i:files) {
                if(i.getName().equals(DIR_NAME))
                    file = i;
            }

        }
        catch (Exception ex)
        {
            return null;
        }
        return file;
    }
}