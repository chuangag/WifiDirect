package com.example.d4561.wifidirect;

/**
 * Created by d4561 on 2017/2/25.
 Saving the world
 */

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.d4561.wifidirect.DeviceListFragment.DeviceActionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private ListView infoList;
    private WifiP2pDevice device;
    private WifiP2pInfo info=null;
    ProgressDialog progressDialog = null;
    private InfoSendedDB infoSendedDB;
    public static String deviceName;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //infoListView=(ListView) inflater.inflate(R.layout.activity_wi_fi_direct,null);

        mContentView = inflater.inflate(R.layout.device_detail, null);
        View connectBtn=mContentView.findViewById(R.id.btn_connect);

        infoSendedDB = new InfoSendedDB(getActivity().getApplicationContext());


        connectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                //record the device name when click, MUST BE CONNECT FROM THE SERVER, NOT A GOOD WAY ACTUALLY
                deviceName=device.deviceName;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                /*new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ((DeviceActionListener) getActivity()).cancelDisconnect();
                    }
                }*/
                );
                ((DeviceActionListener) getActivity()).connect(config);


            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
/*
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);*/
                        Log.d(WiFiDirectActivity.TAG, "sendMessage");
                        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);

                        Log.d(WiFiDirectActivity.TAG, "Sending Info ");
                        Intent serviceIntent = new Intent(getActivity(), InfoTransferService.class);
                        serviceIntent.setAction(InfoTransferService.ACTION_SEND_INFO);
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        statusText.setText("Info sended: "+currentDateTimeString);
                        serviceIntent.putExtra(InfoTransferService.CONNECTION_INFO, currentDateTimeString);
                        serviceIntent.putExtra(InfoTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
                        serviceIntent.putExtra(InfoTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);

                        getActivity().startService(serviceIntent);
                        Log.d(WiFiDirectActivity.TAG, "Service started ");
                    }
                });

        return mContentView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(WiFiDirectActivity.TAG, "start activity");
        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending Info....... ");
        Log.d(WiFiDirectActivity.TAG, "Sending Info ");
        Intent serviceIntent = new Intent(getActivity(), InfoTransferService.class);
        serviceIntent.setAction(InfoTransferService.ACTION_SEND_INFO);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        serviceIntent.putExtra(InfoTransferService.CONNECTION_INFO, currentDateTimeString);
        serviceIntent.putExtra(InfoTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(InfoTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
        /*Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);

        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);*/
    }
    /*
    private void sendConnectionInfo(){
        Socket socket = new Socket();

        try {
            Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

            Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
            OutputStream stream = socket.getOutputStream();
            ContentResolver cr = context.getContentResolver();
            InputStream is = null;
            try {
                is = cr.openInputStream(Uri.parse(fileUri));
            } catch (FileNotFoundException e) {
                Log.d(WiFiDirectActivity.TAG, e.toString());
            }
            DeviceDetailFragment.copyFile(is, stream);
            Log.d(WiFiDirectActivity.TAG, "Client: Data written");
        } catch (IOException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
        } finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Give up
                        e.printStackTrace();
                    }
                }
            }
        }
    }
*/
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        Log.d(WiFiDirectActivity.TAG, "Name"+deviceName);
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            //new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
            InfoServerAsyncTask task =new InfoServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text));
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Log.d(WiFiDirectActivity.TAG, "get here?");
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }



    /**
     *  A simple server socket that accepts connection and writes some data on
     * the stream.
     */

    public class InfoServerAsyncTask extends AsyncTask<Void,Void,String>{

        private Context context;
        private TextView statusText;

        public InfoServerAsyncTask(Context context, View statusText) {
            Log.d(WiFiDirectActivity.TAG, "Asyctask created");
            this.context = context;
            this.statusText = (TextView) statusText;
        }
        @Override
        protected String doInBackground(Void... params) {
            Log.d(WiFiDirectActivity.TAG, "entered");
            try {

                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                //InputStream inputstream = new ByteArrayInputStream(currentDateTimeString.getBytes("UTF-8"));
                InputStream inputstream =client.getInputStream();
                ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

                //Log.d(WiFiDirectActivity.TAG, currentDateTimeString);
                copyMsg(inputstream,outputStream);
                serverSocket.close();
                //client.close();


                return outputStream.toString();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return "fail";
            }
            //return "test";
        }


        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("Connection:  " + result);

                Info item=new Info(0,result,device.deviceName,"ME");
                infoSendedDB.insert(item);
                List<Info> items = infoSendedDB.getAll();


                infoList= (ListView) ((WiFiDirectActivity)getActivity()).findViewById(R.id.info_list);

                InfoAdapter infoAdapter=new InfoAdapter(((WiFiDirectActivity)getActivity()),R.layout.singleinfo,items);
                if(infoList!=null){
                    infoList.setAdapter(infoAdapter);
                    Log.d(WiFiDirectActivity.TAG, "Infolist is not null");

                }
                else
                    Log.d(WiFiDirectActivity.TAG, "Infolist is null");
                infoAdapter.notifyDataSetChanged();
                Toast.makeText(((WiFiDirectActivity)getActivity()),
                        "Received:  " + result,
                        Toast.LENGTH_LONG).show();
                ((WiFiDirectActivity)getActivity()).disconnect();
                //infoList.invalidate();
               /* Intent intent = new Intent();
                ntent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);*/
            }

        }

        public void  addRecord(String outputStream){
            Info item=new Info(0,outputStream,device.deviceName,"ME");
            infoSendedDB.insert(item);
            return ;
        }

    }




    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
    public static boolean copyMsg(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }







// no use codes

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                /**
                                 * Create a server socket and wait for client connections. This
                                 * call blocks until a connection is accepted from a client
                                 */
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Log.d(WiFiDirectActivity.TAG, "Stopped here?");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                //InputStream inputstream = new ByteArrayInputStream(currentDateTimeString.getBytes("UTF-8"));
                InputStream inputstream =client.getInputStream();
                ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

                //Log.d(WiFiDirectActivity.TAG, currentDateTimeString);
                copyMsg(inputstream,outputStream);
                /*
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));*/
                serverSocket.close();
                return "Info received"+outputStream.toString();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("Connection:  " + result);
                Intent intent = new Intent();
                /*intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");*/
                context.startActivity(intent);
            }

        }

        protected void execute(){
            statusText.setText("connected!!!");
        }
        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }
}
