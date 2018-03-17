package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import ui.MainUI;

public class ListenerThread extends Thread {
	private ServerSocket server;
	private int port;

	public ListenerThread(int port) {
		// TODO Auto-generated constructor stub
		this.port = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			server = new ServerSocket(port);
			while (true) {
				System.out.println("server bengin");
				Socket socket = server.accept();
				System.out.println("client connect!!");
				/* 以下是接收来自客户端的IP */
				MainUI.clientIP = (socket.getInetAddress().toString()).replaceAll("/", "");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}