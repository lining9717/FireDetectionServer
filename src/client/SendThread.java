package client;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.imageio.ImageIO;
import util.ImageUtil;

public class SendThread extends Thread {
	private Image image;
	private String serverIP;
	private int port;
	private byte byteBuffer[] = new byte[4 * 1024];

	public SendThread(Image img, String serverIP, int port) {
		// TODO Auto-generated constructor stub
		this.image = img;
		this.serverIP = serverIP;
		this.port = port;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Send Thread：开始连接到服务端");
			Socket socket = new Socket(serverIP, port);
			System.out.println("Send Thread：连接到服务端成功");
			OutputStream outputStream = socket.getOutputStream();
			BufferedImage bImage = ImageUtil.toBufferedImage(image);
			ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", tempStream);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(tempStream.toByteArray());
			int amount;
			while ((amount = inputStream.read(byteBuffer)) != -1) {
				outputStream.write(byteBuffer, 0, amount); // 发送
			}
			tempStream.flush();
			tempStream.close();
			System.out.println("Send Thread：图片已发送完成");
			socket.close();
			System.out.println("Send Thread：套接字关闭");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
