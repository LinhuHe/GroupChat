package com.example.socketdemo;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.Enumeration;

public class SocketThread extends Thread {

    public interface OnClientListener {
        void onNewMessage(String msg);
        void onNewStringList(String clients);
    }

    private OnClientListener onClientListener;

    public void setOnClientListener(OnClientListener onClientListener) {
        this.onClientListener = onClientListener;
    }

    private String minterest = "not yet";
    private Socket socket;
    private boolean isConnected = false;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ObjectInputStream ois;
    private String list[] = new String[255];
    private String myip = "192.168.43.214";

    public SocketThread(OnClientListener onClientListener,String interst,String ip) {
        this.onClientListener = onClientListener;
        this.minterest = interst;
        this.myip = ip;
        if(minterest == null)
        {
         System.out.println("in sockethread now intrest is null");
        }

    }

    public void disconnect() {
        if(socket == null){}
        else {
            try {
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void run() {

        super.run();

        /*myip = getLocalIP();*/
        System.out.println("in sockethread now ip is "+ myip);

        try {
            // 创建一个Socket对象，并指定服务端的IP及端口号
            try {
                switch(minterest) {
                    case "food":{ socket = new Socket(myip, 8888); System.out.println("socekthread food"); break;}
                    case "knowledage": { socket = new Socket(myip, 8888);System.out.println("socekthread knowledage");break;}
                    case "pic_entertainment":{ socket = new Socket(myip, 8888); System.out.println("socekthread entertainment");break;}
                    case "pic_sport":{ socket = new Socket(myip, 8888); System.out.println("socekthread sport");break;}
                    default: { socket = new Socket(myip, 8888); System.out.println("socekthread default");}
                }
               // socket = new Socket("192.168.0.104", 8888);
                System.out.println("成功" + socket.toString());
            }
            catch (Exception e) {
                System.err.println("失败");
            }
            if(socket == null)
            {
                sleep(5000);
            }
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            //ois = new ObjectInputStream(socket.getInputStream());
            list =null;
            System.out.println("~~~~~~~~连接成功~~~~~~~~!");
            isConnected = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            interrupt();
        }

        while (isConnected) {   //读取其他发来的消息
            try {
                while (isConnected) {
                        String str = dataInputStream.readUTF();
                        if (str != null) {
                            if (onClientListener != null) {
                                onClientListener.onNewMessage(str);
                            }
                        }
                }
           } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
                try {
                    if (dataInputStream != null)
                        dataInputStream.close();
                    if (dataOutputStream != null)
                        dataOutputStream.close();
                    if (socket != null) {
                        socket.close();
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public void sendMessage(String message) {
        try {
            if(message == null)
            {
                System.out.println("message is null");
                return;
            }
            System.out.println(message);
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalIP(){
        String clientIP = null;
        Enumeration<NetworkInterface> networks = null;
        try {
            //获取所有网卡设备
            networks = NetworkInterface.getNetworkInterfaces();
            if (networks == null) {
                //没有网卡设备 打印日志  返回null结束
                System.out.println("networks  is null");
                return null;
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        }
        InetAddress ip;
        Enumeration<InetAddress> addrs;
        // 遍历网卡设备
        while (networks.hasMoreElements()) {
            NetworkInterface ni = networks.nextElement();
            try {
                //过滤掉 loopback设备、虚拟网卡
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            }
            addrs = ni.getInetAddresses();
            if (addrs == null) {
                System.out.println("InetAddress is null");
                continue;
            }
            // 遍历InetAddress信息
            while (addrs.hasMoreElements()) {
                ip = addrs.nextElement();
                if (!ip.isLoopbackAddress() && ip.isSiteLocalAddress() && ip.getHostAddress().indexOf(":") == -1) {
                    try {
                        clientIP = ip.toString().split("/")[1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        clientIP = null;
                    }
                }
            }
        }
        return clientIP;
    }

}
