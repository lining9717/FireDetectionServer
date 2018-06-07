package ui;

import server.*;
import client.*;
import util.ImageUtil;
import javax.swing.JFrame;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

public class MainUI extends Thread{
	private static JFrame frame;
	private static JLabel showIP;
	private static JLabel videoLabel;
	private static Image img;
	private static final int listenerPort = 8888;
	private static final int sendPort = 8889;
	public static String clientIP;
	private static JLabel labeltemp ;

	/**
	 * Create the application.
	 */
	public MainUI() {}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		clientIP = null;
		ListenerThread listenerThread = new ListenerThread(listenerPort);
		listenerThread.start();
		videoSocket();
	}
	
	/**
	 * Initialize the contents of the frame.
	 * 
	 * @wbp.parser.entryPoint
	 * 
	 */
	private static void initialize(int w, int h) {
		frame = new JFrame();
		frame.setBounds(100, 100, w, h);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("É­ÁÖ»ðÑæ¼à¿Ø");

		JLabel ipLabel = new JLabel("IP:");
		ipLabel.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 16));
		ipLabel.setForeground(Color.red);
		ipLabel.setBounds(5, 5, 20, 15);
		frame.getContentPane().add(ipLabel);
		
		showIP = new JLabel("192.168.43.99");
		showIP.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 16));
		showIP.setForeground(Color.red);
		showIP.setBounds(25, 5, 200, 15);
		frame.getContentPane().add(showIP);

		videoLabel = new JLabel();
		videoLabel.setBounds(0, 0, w, h);
		frame.getContentPane().add(videoLabel);
		frame.setVisible(true);
		
		
		JFrame frame2 = new JFrame("temp");
		frame2.setBounds(50, 50, w, h);
		frame2.getContentPane().setLayout(null);
		
		labeltemp = new JLabel();
		labeltemp.setBounds(0, 0, w, h);
		frame2.getContentPane().add(labeltemp); 
		frame2.setVisible(true);
	}
	
	public static void videoSocket() {
		VideoCapture cap = new VideoCapture(0);
		if (!cap.isOpened()) { 
			System.out.println("No Camera");
			return;
		} 
		Mat webcam_image = new Mat();
		Mat backImg = new Mat();
		cap.read(webcam_image);
		cap.read(backImg);
		//set background
		Imgproc.GaussianBlur(backImg, backImg, new Size(3, 3), 0, 0);		//GaussianBlur
		Imgproc.cvtColor(backImg, backImg, 6);	//rgb2gary
		initialize(webcam_image.width(), webcam_image.height());
		
		while (true) {
			cap.read(webcam_image);
			if (webcam_image.empty()) {
				cap.release();
				break;
			}
			Mat temp = ImageUtil.preProcessing(webcam_image, backImg);
			Image img2 = ImageUtil.matToImage(temp);
			labeltemp.setIcon(new ImageIcon(img2));
		
			ImageUtil.CheckColor(webcam_image,ImageUtil.preProcessing(webcam_image, backImg));
			img = ImageUtil.matToImage(webcam_image);
			if(clientIP != null) {
				System.out.println("ÊÓÆµ´«ËÍ·þÎñ¶ËIPÎª "+clientIP);
				Thread send = new SendThread(img, clientIP, sendPort);
				send.start();
			}
			videoLabel.setIcon( new ImageIcon(img));	
		}
	}
	
}