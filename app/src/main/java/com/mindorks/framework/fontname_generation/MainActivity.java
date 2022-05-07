package com.mindorks.framework.fontname_generation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IAemCardScanner, IAemScrybe  {
    int effectivePrintWidth = 48;
    AEMScrybeDevice m_AemScrybeDevice;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    CardReader m_cardReader = null;
    AEMPrinter m_AemPrinter = null;
    ArrayList<String> printerList;
    String creditData;
    ProgressDialog m_WaitDialogue;
    CardReader.CARD_TRACK cardTrackType;
    int glbPrinterWidth;
    int numChars = glbPrinterWidth;
    int labelPrinterWidth;
    EditText edittext_printerSeries;
    private PrintWriter printOut;
    private Socket socketConnection;
    private final String txtIP = "";
    Spinner spinner;
    String encoding = "US-ASCII";
    EditText edtName, edtPin;
    String data;
    private final String strShow = "";
    Bitmap mBitmap;
    String[] responseArray = new String[1];
    char[] batteryStatusCommand=new char[]{0x1B,0x7E,0x42,0x50,0x7C,0x47,0x45,0x54,0x7C,0x42,0x41,0x54,0x5F,0x53,0x54,0x5E};

    char[] end = new char[]{0x1B, 0x7E, 0x42, 0x50, 0x7C, 0x47, 0x45, 0x54, 0x7C, 0x45, 0x4E, 0x44, 0x5F, 0x50, 0x52, 0x5E};
    char[] start = new char[]{0x1B, 0x7E, 0x42, 0x50, 0x7C, 0x47, 0x45, 0x54, 0x7C, 0x53, 0x54, 0x41, 0x52, 0x54, 0x50, 0x5E};
    char[] labelFullCut = new char[]{0x1D, 0x56, 0x00};
    char[] labelHalfCut = new char[]{0x1D, 0x56, 0x01};
    char[] labelBarCodeCut = new char[]{0x1D, 0x48, 0x00};
    public Handler mHandler;

    String response ,replacedData,batteryStatus,tempdata,responseString;
    TextView txtBatteryStatus;

    public static final byte FONT_001 = 0X03;
    public static final byte FONT_002 = 0X14;
    public static final byte FONT_003 = 0X16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        printerList = new ArrayList<String>();
        creditData = "";

        txtBatteryStatus= findViewById(R.id.editText);
        spinner= findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.Printer_List, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0) {
                    glbPrinterWidth=0; // Normal
                    onSetPrinterType(view);

                } else if (position==1){ // Tahoma
                    glbPrinterWidth=1;
                    onSetPrinterType(view);

                } else if (position==2){ // Calibri
                    glbPrinterWidth=2;
                    onSetPrinterType(view);

                } else if (position==3){ // Verdana
                    glbPrinterWidth=3;
                    onSetPrinterType(view);
                }
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        m_AemScrybeDevice = new AEMScrybeDevice(this);
        Button discoverButton = findViewById(R.id.pairing);
        registerForContextMenu(discoverButton);

    }


    private void onSetPrinterType(View view) {
        if(glbPrinterWidth == 0) {
            glbPrinterWidth = 0;
        }else if (glbPrinterWidth==1){
            glbPrinterWidth = 1;
        }else if (glbPrinterWidth==2){
            glbPrinterWidth = 2;
        }else if (glbPrinterWidth==3){
            glbPrinterWidth = 3;
        }else if (glbPrinterWidth==4){
            glbPrinterWidth =4;
        }/*else if (glbPrinterWidth==5){
            glbPrinterWidth =5;
        }else if (glbPrinterWidth==6){
            glbPrinterWidth =6;
        }*/

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select Printer to connect");
        for (int i = 0; i < printerList.size(); i++) {
            menu.add(0, v.getId(), 0, printerList.get(i));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        String printerName = item.getTitle().toString();
        try {
            m_AemScrybeDevice.connectToPrinter(printerName);
            m_cardReader = m_AemScrybeDevice.getCardReader(this);
            m_AemPrinter = m_AemScrybeDevice.getAemPrinter();
            Toast.makeText(MainActivity.this, "Connected with " + printerName, Toast.LENGTH_SHORT).show();
            String data=new String(batteryStatusCommand);
            m_AemPrinter.print(data);

            //  m_cardReader.readMSR();
        } catch (IOException e) {
            if (e.getMessage().contains("Service discovery failed")) {
                Toast.makeText(MainActivity.this, "Not Connected\n" + printerName + " is unreachable or off otherwise it is connected with other device", Toast.LENGTH_SHORT).show();
            } else if (e.getMessage().contains("Device or resource busy")) {
                Toast.makeText(MainActivity.this, "the device is already connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Printer not connect......", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }


    public void onShowPairedPrinters(View view) {
        String p = m_AemScrybeDevice.pairPrinter("BTprinter0314");
        // showAlert(p);
        printerList = m_AemScrybeDevice.getPairedPrinters();
        if (printerList.size() > 0)
            openContextMenu(view);

    }

    public void fonttext(View view) throws IOException {
        onfontprint(glbPrinterWidth);
    }

    private void onfontprint(int glbPrinterWidth) throws IOException {
        if (m_AemPrinter == null) {
            Toast.makeText(MainActivity.this, "Printer not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (glbPrinterWidth==0){ //NORMAL
            char[] prn_cfg_cmd=new char[]{0x1C,0x67,0x63,0x01};
            String data=new String(prn_cfg_cmd);
            try {
                m_AemPrinter.print(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            data =  "DEF_FONT=NORMAL\n";
            try {
                m_AemPrinter.print(data);
                data ="This is NORMAL Font\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                data =   "13|ColgateGel |35.00|02|70.00\n"+
                        "29|Pears Soap |25.00|01|25.00\n"+
                        "88|Lux Shower |46.00|01|46.00\n"+
                        "15|Dabur Honey|65.00|01|65.00\n"+
                        "52|Dairy Milk |20.00|10|200.00\n"+
                        "128|Maggie TS |36.00|04|144.00\n"+
                        "_______________________________\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        else if (glbPrinterWidth==1){ // TAHOMA
          char[] prn_cfg_cmd=new char[]{0x1C,0x67,0x63,0x01};
          String data=new String(prn_cfg_cmd);
            try {
                m_AemPrinter.print(data);

            } catch (IOException e) {
                e.printStackTrace();
            }
            data =  "DEF_FONT=TAHOMA\n";
            try {
                m_AemPrinter.print(data);
                data ="This is TAHOMA Font\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                data =   "13|ColgateGel |35.00|02|70.00\n"+
                        "29|Pears Soap |25.00|01|25.00\n"+
                        "88|Lux Shower |46.00|01|46.00\n"+
                        "15|Dabur Honey|65.00|01|65.00\n"+
                        "52|Dairy Milk |20.00|10|200.00\n"+
                        "128|Maggie TS |36.00|04|144.00\n"+
                        "_______________________________\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (glbPrinterWidth==2){ // CALIBRI
            char[] prn_cfg_cmd=new char[]{0x1C,0x67,0x63,0x01};
            String data=new String(prn_cfg_cmd);
            try {
                m_AemPrinter.print(data);

            } catch (IOException e) {
                e.printStackTrace();
            }
            data =  "DEF_FONT=CALIBRI\n";
            try {
                m_AemPrinter.print(data);
                data =   "This is CALIBRI Font\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                data =   "13|ColgateGel |35.00|02|70.00\n"+
                        "29|Pears Soap |25.00|01|25.00\n"+
                        "88|Lux Shower |46.00|01|46.00\n"+
                        "15|Dabur Honey|65.00|01|65.00\n"+
                        "52|Dairy Milk |20.00|10|200.00\n"+
                        "128|Maggie TS |36.00|04|144.00\n"+
                        "_______________________________\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (glbPrinterWidth==3){
            char[] prn_cfg_cmd=new char[]{0x1C,0x67,0x63,0x01};
            String data=new String(prn_cfg_cmd);
            try {
                m_AemPrinter.print(data);

            } catch (IOException e) {
                e.printStackTrace();
            }
            data =  "DEF_FONT=VERDHANA\n";
            try {
                m_AemPrinter.print(data);
                data ="This is VERDHANA Font\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();

                data =   "13|ColgateGel |35.00|02|70.00\n"+
                        "29|Pears Soap |25.00|01|25.00\n"+
                        "88|Lux Shower |46.00|01|46.00\n"+
                        "15|Dabur Honey|65.00|01|65.00\n"+
                        "52|Dairy Milk |20.00|10|200.00\n"+
                        "128|Maggie TS |36.00|04|144.00\n"+
                        "_______________________________\n";
                m_AemPrinter.POS_FontThreeInchCENTER();
                m_AemPrinter.print(data);
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onScanMSR(String buffer, CardReader.CARD_TRACK cardtrack) {

    }

    @Override
    public void onScanDLCard(String buffer) {

    }

    @Override
    public void onScanRCCard(String buffer) {

    }

    @Override
    public void onScanRFD(String buffer) {

    }

    @Override
    public void onScanPacket(String buffer) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(buffer);
        String temp = "";
        try {
            temp = stringBuffer.toString();
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        data = temp;
        final String strData=data.replace("|","&");
        //Log.e("BufferData",data);
        String[] formattedData=strData.split("&",3);
        // Log.e("Response Data",formattedData[2]);
        String responseString =formattedData[2];
        responseArray[0]=responseString.replace("^","");
        Log.e("Response Array",responseArray[0]);
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run()
            {
                String replacedData=data.replace("|","&");
                String[] formattedData=replacedData.split("&",3);
                //Log.e("Response Data",formattedData[2]);
                // response=formattedData[2];

                String response=formattedData[2];
                //  Log.e("Response",response);
                if(response.contains("BAT")){
                    txtBatteryStatus.setText(response.replace("^","").replace("BAT","")+"%");
                }
                //  Log.e("Edited Response : ",response.replace("^",""));
                txtBatteryStatus.setText(response.replace("^","").replace("BAT","")+"%");

             //   txtBatteryStatus.setText(response.replace("^",""));
                /* if(response.contains("NOPAPER")){
                     try {
                         m_AemScrybeDevice.disConnectPrinter();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }*/
            }
        });

        // thread = new MyThread();
        // thread.start();

    }




/*
    @Override
    public void onScanPacket(String buffer) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(buffer);
        String temp = "";
        try
        {
            temp = stringBuffer.toString();
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        final String data = temp;
        txtBatteryStatus.setText(data);

*/
/*
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View viewDialog = inflater.inflate(R.layout.popup, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.Base_Theme_AppCompat_Dialog).create();
                alertDialog.setView(viewDialog);
                alertDialog.setTitle("Printer Packet");
                alertDialog.setCancelable(false);
                final TextView printData = (TextView) viewDialog.findViewById(R.id.printData);
                txtBatteryStatus.setText(data);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });
*//*
    }
*/

    @Override
    public void onDiscoveryComplete(ArrayList<String> aemPrinterList) {

    }


    public void Tahoma(View view) throws IOException {

        char[] prn_cfg_cmd=new char[]{0x1C,0x67,0x63,0x01};
        String data=new String(prn_cfg_cmd);
        try {
            m_AemPrinter.print(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        data =  "DEF_FONT=TAHOMA\n";
        m_AemPrinter.print(data);

        data =   "13|ColgateGel |35.00|02|70.00\n"+
                "29|Pears Soap |25.00|01|25.00\n"+
                "88|Lux Shower |46.00|01|46.00\n"+
                "15|Dabur Honey|65.00|01|65.00\n"+
                "52|Dairy Milk |20.00|10|200.00\n"+
                "128|Maggie TS |36.00|04|144.00\n"+
                "_______________________________\n";

        m_AemPrinter.print(data);
    }
}