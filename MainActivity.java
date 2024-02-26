package com.example.nfc_expo_ivan;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nfc_expo_ivan.R;


public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private TextView textView;
    private ImageView imageView;

    // ID esperado de la tarjeta Mifare
    private static final byte[] EXPECTED_ID = {0x01, 0x02, 0x03, 0x04};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Activity created");

        textView = findViewById(R.id.instructionsTextView);
        imageView = findViewById(R.id.resultImageView);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFiltersArray = new IntentFilter[]{intentFilter};
        techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            MifareClassic mifareClassic = MifareClassic.get(tag);
            try {
                mifareClassic.connect();
                byte[] id = mifareClassic.getTag().getId();

                if (compareIds(id, EXPECTED_ID)) {
                    // Mostrar tick verde
                    textView.setText("Tarjeta válida");
                    imageView.setImageResource(R.drawable.ic_tick_green);
                } else {
                    // Mostrar tick rojo
                    textView.setText("Tarjeta inválida");
                    imageView.setImageResource(R.drawable.ic_cross_red);
                }

                imageView.setVisibility(ImageView.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mifareClassic.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean compareIds(byte[] id1, byte[] id2) {
        if (id1.length != id2.length) {
            return false;
        }
        for (int i = 0; i < id1.length; i++) {
            if (id1[i] != id2[i]) {
                return false;
            }
        }
        return true;
    }
}
