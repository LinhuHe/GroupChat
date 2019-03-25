package com.example;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private Socket s;

    public interface OnServiceListener {
        void onClientChanged(List<Client> clients);

        void onNewMessage(String message, Client client);
        void onNewStringList(String clients, Client client);
    }

    private OnServiceListener listener;

    public void setOnServiceListener(OnServiceListener listener) {
        this.listener = listener;
    }


    boolean started = false;
    ServerSocket ss = null;
    List<Client> clients = new ArrayList<Client>();
    private String [] templist = new String[clients.size()];
    private String [] list;

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
           ss = new ServerSocket(8888);

            System.out.println(ss.toString());

            started = true;
            System.out.println("server is started");
        } catch (BindException e) {
            System.out.println("port is not available....");
            System.out.println("please restart");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (started) {
                s = ss.accept();
                Client c = new Client(s, Server.this);
                System.out.println("a client connected!");
                new Thread(c).start();
                addClient(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public synchronized void snedMessage(String msg) {
        for (Client client1 : clients) {
            client1.send(msg);
        }
    }

        public synchronized void newStringClients(String myclients, Client client) { //将所有连接的用户发送到客户端
        if (listener != null) {
            listener.onNewStringList(myclients, client);
            for (Client client1 : clients) {   //遍历clients所有元素
                if (!client1.equals(client)) {
                    client1.send(client1.getSocket().getInetAddress() + "#" + myclients);
                }
            }
        }
    }

    public synchronized void newMessage(String msg, Client client) {
        if (listener != null) {
            listener.onNewMessage(msg, client);  //这里对应最上面的 interface OnServiceListener
            for (Client client1 : clients) {   //遍历clients所有元素
                if (!client1.equals(client)) {
                    client1.send(client1.getSocket().getInetAddress() + "#" + msg);
                }
            }
        }
    }

    public synchronized void addClient(Client client) {
        clients.add(client);

        /*list = clients.toArray(templist);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(list);
            oos.flush();
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }*/


        if (listener != null) {
            listener.onClientChanged(clients);
        }
    }


    public synchronized void removeClient(Client client) {
        clients.remove(client);
        if (listener != null) {
            listener.onClientChanged(clients);
        }
    }

}
