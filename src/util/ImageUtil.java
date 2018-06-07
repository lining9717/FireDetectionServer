package util;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageUtil {
	private static int redThre = 130; // 115~135 红色分量阈值
	private static int saturationTh = 60; // 55~65 //饱和度阈值
	private static int Rth = 230;
	private static int Gth = 230;
	private static int Th1 = 20;
	private static int Th2 = 20;
	private static int Th3 = 15;
	
	public static Image matToImage(Mat matrix) {
		int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
		byte[] buffer = new byte[bufferSize];
		matrix.get(0, 0, buffer); // 获取所有的像素点
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (matrix.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
			for (int i = 0; i < buffer.length; i = i + 3) {
				buffer[i] ^= buffer[i + 2];
				buffer[i + 2] ^= buffer[i];
				buffer[i] ^= buffer[i + 2];
			}
		}
		BufferedImage image1 = new BufferedImage(matrix.cols(), matrix.rows(), type);
		image1.getRaster().setDataElements(0, 0, matrix.cols(), matrix.rows(), buffer);
		return image1;
	}
	
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent Pixels
		// boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			/*
			 * if (hasAlpha) { transparency = Transparency.BITMASK; }
			 */

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			// int type = BufferedImage.TYPE_3BYTE_BGR;//by wang
			/*
			 * if (hasAlpha) { type = BufferedImage.TYPE_INT_ARGB; }
			 */
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}
	
	public static void CheckColor(Mat inImg,Mat temp) {
		// inImg数据类型是CV_8UC4
		Imgproc.cvtColor(inImg, inImg, Imgproc.COLOR_BGRA2BGR);
		inImg.convertTo(inImg, CvType.CV_64FC3);
		temp.convertTo(temp, CvType.CV_64FC3);
		Mat out = new Mat(inImg.rows(), inImg.cols(), CvType.CV_8UC1);
		double[] buff = new double[(int) inImg.total() * inImg.channels()]; // 获取原图中的RGB信息
		double[] data = new double[(int) (out.channels() * out.total())]; // 写入输出图像的信息
		double[] tempdata = new double[(int) (temp.channels() * temp.total())]; // temp mat data
		inImg.get(0, 0, buff);// 获取整张图片的像素信息
		temp.get(0, 0,tempdata);
		int colcount = out.cols();
		int imgCols = inImg.cols();
		int imgRows = inImg.rows();
		int imgChannel = inImg.channels();
        double B, G, R;
		for (int i = 0; i < imgRows; i++) {
			for (int j = 0, k = 0; j < imgCols * imgChannel; j += imgChannel, k++) {
				B = buff[i * imgCols * imgChannel + j];
				G = buff[i * imgCols * imgChannel + j + 1];
				R = buff[i * imgCols * imgChannel + j + 2];
				if (isFire(R, G, B,tempdata,colcount,i,k)) {
					data[i * colcount + k] = 255;
					continue;
				}
				data[i * colcount + k] = 0;
			}
		}
		out.put(0, 0, data); // 写入输出图像
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
		Imgproc.dilate(out, out, kernel);// 高亮图像
		inImg.convertTo(inImg, CvType.CV_8UC1);
		drawfire(inImg, out);
	}
	
	public static Mat preProcessing(Mat webcam_image,Mat backImg) {
		//temp store new Img
		Mat tempImg = new Mat();
		tempImg = webcam_image.clone();
		//Gaussian
		Imgproc.GaussianBlur(tempImg, tempImg, new Size(3, 3), 0, 0);
		Imgproc.cvtColor(tempImg, tempImg, 6);	//rgb2gary
		Core.absdiff(tempImg, backImg, tempImg);	//background clip
		Imgproc.threshold(tempImg, tempImg, 50, 255, 0);	//set threshold to Binary img
		Imgproc.dilate(tempImg, tempImg, new Mat());	//膨脹
		Imgproc.erode(tempImg, tempImg, new Mat());		//腐蝕
		Core.addWeighted(backImg, 0.95, tempImg, 0.05,1, backImg); //update background
		return tempImg;
	}
	
	private static boolean isFire(double R,double G,double B,double[] tempdata,int colCount,int i,int k) {
		int minValue = (int) min(min(B, G), R);
		double S = (1 - 3.0 * minValue / (R + G + B));
		double T1 = Math.abs(R - G);
		double T2 = Math.abs(B - G);
		double T3 = Math.abs(R - B);
		return R >= redThre 
			&& R >= G 
			&& G >= B 
			&& S >= ((255 - R) * saturationTh / redThre)
			&& ((R >= Rth && G >= Gth) || (T1 >= Th1 && T2 >= Th2)) 
			&& T2 + T3 > Th3 
			&& tempdata[i * colCount + k] == 255;
	}
	
	private static void drawfire(Mat inputImg, Mat foreImg) {
		List<MatOfPoint> contours_set = new ArrayList<>(); // 保存轮廓提取后的点集及拓扑关系
		Imgproc.findContours(foreImg, contours_set, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		MatOfPoint matOfPoint;
		for (int i = 0; i < contours_set.size();) {
			matOfPoint = contours_set.get(i);
			Rect rect = Imgproc.boundingRect(matOfPoint);
			float[] radius = new float[1];
			Point center = new Point();
			Imgproc.minEnclosingCircle(new MatOfPoint2f(contours_set.get(i).toArray()), center, radius);
			if (rect.area() > 0) {
				Imgproc.rectangle(inputImg, rect.tl(), rect.br(), new Scalar(0, 255, 0));
				++i;
			}
		}
	}
	
	private static double min(double a, double b) {
		if (a < b)
			return a;
		else
			return b;
	}

}
