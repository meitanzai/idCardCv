package com.nbsl.cv.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatExpr;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Point2d;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.PointVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.RotatedRect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.indexer.ByteArrayIndexer;
import org.bytedeco.javacpp.indexer.ByteIndexer;
import org.bytedeco.javacpp.indexer.DoubleArrayIndexer;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.javacpp.indexer.IntRawIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;

public class OpencvUtil {
	private static final int BLACK = 0;
	private static final int WHITE = 255;

	/**
	 * 灰化处理
	 *
	 * @return
	 */
	public static Mat gray(Mat mat) {
		Mat gray = new Mat();
		opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY, 1);
		return gray;
	}

	/**
	 * 二值化处理
	 *
	 * @return
	 */
	public static Mat binary(Mat mat) {
		Mat binary = new Mat();
		opencv_imgproc.adaptiveThreshold(mat, binary, 255, opencv_imgproc.ADAPTIVE_THRESH_MEAN_C,
				opencv_imgproc.THRESH_BINARY_INV, 25, 10);
		return binary;
	}

	/**
	 * 模糊处理
	 *
	 * @param mat
	 * @return
	 */
	public static Mat blur(Mat mat) {
		Mat blur = new Mat();
		opencv_imgproc.blur(mat, blur, new Size(5, 5));
		return blur;
	}

	/**
	 * 膨胀
	 *
	 * @param mat
	 * @return
	 */
	public static Mat dilate(Mat mat, int size) {
		Mat dilate = new Mat();
		Mat element = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(size, size));
		// 膨胀
		opencv_imgproc.dilate(mat, dilate, element); // , new Point(-1, -1), 1
		return dilate;
	}

	/**
	 * 腐蚀
	 *
	 * @param mat
	 * @return
	 */
	public static Mat erode(Mat mat, int size) {
		Mat erode = new Mat();
		Mat element = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(size, size));
		// 腐蚀
		opencv_imgproc.erode(mat, erode, element);// , new Point(-1, -1), 1
		return erode;
	}

	/**
	 * 边缘检测
	 *
	 * @param mat
	 * @return
	 */
	public static Mat carry(Mat mat) {
		Mat dst = new Mat();
		// 高斯平滑滤波器卷积降噪
		opencv_imgproc.GaussianBlur(mat, dst, new Size(3, 3), 0);
		// 边缘检测
		opencv_imgproc.Canny(mat, dst, 50, 150);
		return dst;
	}

	/**
	 * 轮廓检测
	 *
	 * @param mat
	 * @return
	 */
	public static MatVector findContours(Mat mat) {
		MatVector contours = new MatVector();
		Mat hierarchy = new Mat();
		opencv_imgproc.findContours(mat, contours, hierarchy, opencv_imgproc.RETR_LIST,
				opencv_imgproc.CHAIN_APPROX_SIMPLE);
		return contours;
	}

	/**
	 * 清除小面积轮廓
	 *
	 * @param mat
	 * @param size
	 * @return
	 */
	public static Mat drawContours(Mat mat, int size) {
		MatVector cardContours = OpencvUtil.findContours(mat);
		for (int i = 0; i < cardContours.size(); i++) {
			double area = OpencvUtil.area(cardContours.get(i));
			if (area < size) {
				opencv_imgproc.drawContours(mat, cardContours, i, new opencv_core.Scalar(0, 0));
			}
		}
		return mat;
	}

	/**
	 * 人脸识别
	 *
	 * @param mat
	 * @return
	 */
	public static RectVector face(Mat mat) throws Exception {
		File file = org.springframework.util.ResourceUtils
				.getFile("classpath:idcard\\haarcascades\\haarcascade_frontalface_alt.xml");
		CascadeClassifier faceDetector = new CascadeClassifier(file.getAbsolutePath());
		// 在图片中检测人脸
		RectVector faceDetections = new RectVector();
		// 指定人脸识别的最大和最小像素范围
		Size minSize = new Size(100, 100);
		Size maxSize = new Size(500, 500);
		// 参数设置为scaleFactor=1.1f, minNeighbors=4, flags=0 以此来增加识别人脸的正确率
		faceDetector.detectMultiScale(mat, faceDetections, 1.1f, 4, 0, minSize, maxSize);
		if (faceDetections != null && faceDetections.size() > 0L) {
			// 在每一个识别出来的人脸周围画出一个方框
			//Rect rect = faceDetections.get(0);
			return faceDetections;
		} else {
			return null;
		}
	}

	/**
	 * 循环进行人脸识别
	 */
	public static Mat faceLoop(Mat src) throws Exception {
		RectVector face = new RectVector();
		// 默认人脸识别失败时图像旋转90度
		int k = 90;
		while (k > 0) {
			for (int i = 0; i < 360 / k; i++) {
				// 人脸识别
				face = OpencvUtil.face(src);
				if (face == null) {
					src = rotate3(src, k);
				} else { 
					break;
				}
			}
			if (face != null) { 
				break;
			} else {
				k = k - 30;
			}
		}
		Mat testImage = new Mat(src, face.get()[0]);  
		//opencv_imgproc.resize(testImage, testImage,new Size(Common.faceWidth, Common.faceHeight));
		return testImage; 
	}

	/**
	 * 剪切身份证区域
	 *
	 * @param mat
	 */
	public static Mat houghLinesP(Mat begin, Mat mat) {
		// 灰度 
		mat = OpencvUtil.gray(mat); 
		// 二值化
		mat = OpencvUtil.binary(mat); 
		// 腐蚀  去除背景图片
		mat = OpencvUtil.erode(mat, 2);   
		// 边缘检测
		mat = OpencvUtil.carry(mat);
		opencv_imgcodecs.imwrite("F:/face/22.jpg", mat);
		// 降噪
		mat = OpencvUtil.navieRemoveNoise(mat, 1);
		opencv_imgcodecs.imwrite("F:/face/11.jpg", mat);
		// 膨胀
		mat = OpencvUtil.dilate(mat, 3); 
		
		// 轮廓检测,清除小的轮廓部分
		MatVector contours = OpencvUtil.findContours(mat);
		for (int i = 0; i < contours.size(); i++) {
			double area = OpencvUtil.area(contours.get(i));
			if (area < 5000) {
				opencv_imgproc.drawContours(mat, contours, i, new Scalar(0, 0));
			}
		} 
		Mat storage = new Mat();
		opencv_imgproc.HoughLinesP(mat, storage, 1, Math.PI / 180, 10, 0, 10);  
		int[] maxLine = new int[] { 0, 0, 0, 0 };
		IntRawIndexer storeIndex = storage.createIndexer();
		// 获取最长的直线 
		
		for (int x = 0; x < storage.rows(); x++) {
			int[] vec = new int[4]; 
			storeIndex.get(x, vec); 
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			double newLength = Math.sqrt(Math.abs((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
			double oldLength = Math.sqrt(Math.abs((maxLine[0] - maxLine[2]) * (maxLine[0] - maxLine[2])
					+ (maxLine[1] - maxLine[3]) * (maxLine[1] - maxLine[3])));
			System.out.println(newLength + "="+oldLength);
			if (newLength > oldLength) {
				maxLine = vec;
			}
		}
		for(int max : maxLine){
			System.out.println(max);
		}
		// 计算最长线的角度
		double angle = getAngle(maxLine[0], maxLine[1], maxLine[2], maxLine[3]);
		// 旋转角度
		mat = rotate3(mat, angle);
		begin = rotate3(begin, angle);

		opencv_imgproc.HoughLinesP(mat, storage, 1, Math.PI / 180, 10, 10, 10);
		List<int[]> lines = new ArrayList<>();
		IntRawIndexer storeIndex1 = storage.createIndexer();
		// 在mat上划线
		for (int x = 0; x < storage.rows(); x++) { 
			int[] vec = new int[4];
			storeIndex1.get(x, vec);
			int x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			Point start = new Point(x1, y1);
			Point end = new Point(x2,y2);
			// 获取与图像x边缘近似平行的直线
			if (Math.abs(start.y() - end.y()) < 5) {
				if (Math.abs(x2 - x1) > 20) {
					lines.add(vec);
				}
			}
			// 获取与图像y边缘近似平行的直线
			if (Math.abs(start.x() - end.x()) < 5) {
				if (Math.abs(y2 - y1) > 20) {
					lines.add(vec);
				}
			}
		}
		// 获取最大的和最小的X,Y坐标
		double maxX = 0.0, minX = 10000, minY = 10000, maxY = 0.0;
		for (int i = 0; i < lines.size(); i++) {
			int[] vec = lines.get(i);
			int x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			maxX = maxX > x1 ? maxX : x1;
			maxX = maxX > x2 ? maxX : x2;
			minX = minX > x1 ? x1 : minX;
			minX = minX > x2 ? x2 : minX;
			maxY = maxY > y1 ? maxY : y1;
			maxY = maxY > y2 ? maxY : y2;
			minY = minY > y1 ? y1 : minY;
			minY = minY > y2 ? y2 : minY;
		} 
		if (maxX < mat.cols() && minX > 0 && maxY < mat.rows() && minY > 0) {
			List<Point2d> list = new ArrayList<>();
			Point2d point1 = new Point2d(minX + 10, minY + 10);
			Point2d point2 = new Point2d(minX + 10, maxY - 10);
			Point2d point3 = new Point2d(maxX - 10, minY + 10);
			Point2d point4 = new Point2d(maxX - 10, maxY - 10);
			list.add(point1);
			list.add(point2);
			list.add(point3);
			list.add(point4);
			mat = shear(begin, list);
		} else {
			mat = begin;
		}  
		opencv_imgcodecs.imwrite("F:/face/houghLinesP1.jpg", mat);
		return mat;
	}

	/**
	 * 计算角度
	 *
	 * @param px1
	 * @param py1
	 * @param px2
	 * @param py2
	 * @return
	 */
	public static double getAngle(double px1, double py1, double px2, double py2) {
		// 两点的x、y值
		double x = px2 - px1;
		double y = py2 - py1;
		double hypotenuse = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		// 斜边长度
		double cos = x / hypotenuse;
		double radian = Math.acos(cos);
		// 求出弧度
		double angle = 180 / (Math.PI / radian);
		// 用弧度算出角度
		if (y < 0) {
			angle = -angle;
		} else if ((y == 0) && (x < 0)) {
			angle = 180;
		}
		while (angle < 0) {
			angle = angle + 90;
		}
		return angle;
	}

	/**
	 * 累计概率hough变换直线检测
	 *
	 * @param mat
	 */
	public static Mat houghLines(Mat mat) {
		Mat storage = new Mat();
		opencv_imgproc.HoughLines(mat, storage, 1, Math.PI / 180, 50, 0, 0, 0, 1);
		for (int x = 0; x < storage.rows(); x++) {
			DoubleIndexer dst1Idx = storage.row(x).createIndexer();

			double[] vec = new double[4];
			dst1Idx.get(0, vec);
			double rho = vec[0];
			double theta = vec[1];

			Point pt1 = new Point();
			Point pt2 = new Point();

			double a = Math.cos(theta);
			double b = Math.sin(theta);

			double x0 = a * rho;
			double y0 = b * rho;

			pt1.x((int) Math.round(x0 + 1000 * (-b)));
			pt1.y((int) Math.round(y0 + 1000 * (a)));
			pt2.x((int) Math.round(x0 - 1000 * (-b)));
			pt2.y((int) Math.round(y0 - 1000 * (a)));

			if (theta >= 0) {
				opencv_imgproc.line(mat, pt1, pt2, new Scalar(255));
			}
		}
		return mat;
	}

	/**
	 * 根据四点坐标截取模板图片
	 *
	 * @param mat
	 * @param pointList
	 * @return
	 */
	public static Mat shear(Mat mat, List<Point2d> pointList) {
		int x = minX(pointList);
		int y = minY(pointList);
		int xl = xLength(pointList) > mat.cols() - x ? mat.cols() - x : xLength(pointList);
		int yl = yLength(pointList) > mat.rows() - y ? mat.rows() - y : yLength(pointList);
		Rect re = new Rect(x, y, xl, yl); 
		return new Mat(mat, re);
	}

	/**
	 * 图片旋转
	 *
	 * @param splitImage
	 * @param angle
	 * @return
	 */
	public static Mat rotate3(Mat splitImage, double angle) {
		double thera = angle * Math.PI / 180;
		double a = Math.sin(thera);
		double b = Math.cos(thera);

		int wsrc = splitImage.arrayWidth();
		int hsrc = splitImage.arrayHeight();

		int wdst = (int) (hsrc * Math.abs(a) + wsrc * Math.abs(b));
		int hdst = (int) (wsrc * Math.abs(a) + hsrc * Math.abs(b));
		Mat imgDst = new Mat(hdst, wdst, splitImage.type());

		Point2f pt = new Point2f(splitImage.cols() / 2, splitImage.rows() / 2);
		// 获取仿射变换矩阵
		Mat affineTrans = opencv_imgproc.getRotationMatrix2D(pt, angle, 1.0);
		final DoubleIndexer bgrIdx = affineTrans.createIndexer();

		// System.out.println(affineTrans.dump());
		// 改变变换矩阵第三列的值
		bgrIdx.put(0, 2, bgrIdx.get(0, 2) + (wdst - wsrc) / 2);
		bgrIdx.put(1, 2, bgrIdx.get(1, 2) + (hdst - hsrc) / 2);

		opencv_imgproc.warpAffine(splitImage, imgDst, affineTrans, imgDst.size());
		return imgDst;
	}

	/**
	 * 图像直方图处理
	 *
	 * @param mat
	 * @return
	 */
	public static Mat equalizeHist(Mat mat) {
		Mat dst = new Mat();
		MatVector mv = new MatVector();
		opencv_core.split(mat, mv);
		for (int i = 0; i < mat.channels(); i++) {
			opencv_imgproc.equalizeHist(mv.get(i), mv.get(i));
		}
		opencv_core.merge(mv, dst);
		return dst;
	}

	/**
	 * 和下面的方法是反着来的
	 *
	 * @param pNum
	 *            默认值为1
	 */
	public static Mat navieRemoveNoiseBack(Mat mat, int pNum) {
		int i, j, m, n, nValue, nCount;
		int nWidth = mat.cols();
		int nHeight = mat.rows();
		UByteRawIndexer matIndex = mat.createIndexer();
		// 如果一个点的周围都是白色的，而它确是黑色的，删除它
		for (j = 1; j < nHeight - 1; ++j) {
			for (i = 1; i < nWidth - 1; ++i) {
				nValue = matIndex.get(j, i);
				if (nValue == 0) {
					nCount = 0;
					// 比较以(j ,i)为中心的9宫格，如果周围都是白色的，同化
					for (m = j - 1; m <= j + 1; ++m) {
						for (n = i - 1; n <= i + 1; ++n) {
							if (matIndex.get(m, n) == 0) {
								nCount++;
							}
						}
					}
					if (nCount <= pNum) {
						// 周围黑色点的个数小于阀值pNum,把该点设置白色
						matIndex.put(j, i, BLACK);
					}
				} else {
					nCount = 0;
					// 比较以(j ,i)为中心的9宫格，如果周围都是黑色的，同化
					for (m = j - 1; m <= j + 1; ++m) {
						for (n = i - 1; n <= i + 1; ++n) {
							if (matIndex.get(m, n) == 0) {
								nCount++;
							}
						}
					}
					if (nCount >= 7) {
						// 周围黑色点的个数大于等于7,把该点设置黑色;即周围都是黑色
						matIndex.put(j, i, WHITE);
					}
				}
			}
		}
		return mat;
	}
	/**
	 * 8邻域降噪,又有点像9宫格降噪;即如果9宫格中心被异色包围，则同化
	 *
	 * @param pNum
	 *            默认值为1
	 */
	public static Mat navieRemoveNoise(Mat mat, int pNum) {
		int i, j, m, n, nValue, nCount;
		int nWidth = mat.cols();
		int nHeight = mat.rows();
		UByteRawIndexer matIndex = mat.createIndexer();
		// 如果一个点的周围都是白色的，而它确是黑色的，删除它
		for (j = 1; j < nHeight - 1; ++j) {
			for (i = 1; i < nWidth - 1; ++i) {
				nValue = matIndex.get(j, i);
				if (nValue == 0) {
					nCount = 0;
					// 比较以(j ,i)为中心的9宫格，如果周围都是白色的，同化
					for (m = j - 1; m <= j + 1; ++m) {
						for (n = i - 1; n <= i + 1; ++n) {
							if (matIndex.get(m, n) == 0) {
								nCount++;
							}
						}
					}
					if (nCount <= pNum) {
						// 周围黑色点的个数小于阀值pNum,把该点设置白色
						matIndex.put(j, i, WHITE);
					}
				} else {
					nCount = 0;
					// 比较以(j ,i)为中心的9宫格，如果周围都是黑色的，同化
					for (m = j - 1; m <= j + 1; ++m) {
						for (n = i - 1; n <= i + 1; ++n) {
							if (matIndex.get(m, n) == 0) {
								nCount++;
							}
						}
					}
					if (nCount >= 7) {
						// 周围黑色点的个数大于等于7,把该点设置黑色;即周围都是黑色
						matIndex.put(j, i, BLACK);
					}
				}
			}
		}
		return mat;
	}

	/**
	 * 连通域降噪
	 *
	 * @param pArea
	 *            默认值为1
	 */
	public static Mat contoursRemoveNoise(Mat mat, double pArea) {
		// mat=floodFill(mat,mat.new Point(mat.cols()/2,mat.rows()/2),new
		// Color(225,0,0));
		int i, j, color = 1;
		int nWidth = mat.cols(), nHeight = mat.rows();
		IntIndexer matIndex = mat.createIndexer();
		for (i = 0; i < nWidth; ++i) {
			for (j = 0; j < nHeight; ++j) {
				if (matIndex.get(j, i) == BLACK) {
					// 用不同颜色填充连接区域中的每个黑色点
					// floodFill就是把一个点x的所有相邻的点都涂上x点的颜色，一直填充下去，直到这个区域内所有的点都被填充完为止
					opencv_imgproc.floodFill(mat, new Mat(), new Point(i, j), new Scalar(color));
					color++;
				}
			}
		}

		// 统计不同颜色点的个数
		int[] ColorCount = new int[255];

		for (i = 0; i < nWidth; ++i) {
			for (j = 0; j < nHeight; ++j) {
				if (matIndex.get(j, i) != 255) {
					ColorCount[matIndex.get(j, i) - 1]++;
				}
			}
		}

		// 去除噪点
		for (i = 0; i < nWidth; ++i) {
			for (j = 0; j < nHeight; ++j) {
				if (ColorCount[matIndex.get(j, i) - 1] <= pArea) {
					matIndex.put(j, i, WHITE);
				}
			}
		}

		for (i = 0; i < nWidth; ++i) {
			for (j = 0; j < nHeight; ++j) {
				if (matIndex.get(j, i) < WHITE) {
					matIndex.put(j, i, BLACK);
				}
			}
		}
		return mat;
	}

	/**
	 * Mat转换成BufferedImage
	 *
	 * @param matrix
	 *            要转换的Mat
	 * @param fileExtension
	 *            格式为 ".jpg", ".png", etc
	 * @return
	 */
	public static BufferedImage Mat2BufImg(Mat matrix, String fileExtension) {
		BytePointer mob = new BytePointer();
		opencv_imgcodecs.imencode(fileExtension, matrix, mob);
		byte[] byteArray = mob.getStringBytes();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return replaceWithWhiteColor(bufImage);
	}

	public static BufferedImage replaceWithWhiteColor(BufferedImage bi) {

		int[] rgb = new int[3];

		int width = bi.getWidth();

		int height = bi.getHeight();

		int minx = bi.getMinX();

		int miny = bi.getMinY();

		/**
		 * 
		 * 遍历图片的像素，为处理图片上的杂色，所以要把指定像素上的颜色换成目标白色 用二层循环遍历长和宽上的每个像素
		 * 
		 */

		int hitCount = 0;

		for (int i = minx; i < width - 1; i++) {

			for (int j = miny; j < height; j++) {

				/**
				 * 
				 * 得到指定像素（i,j)上的RGB值，
				 * 
				 */

				int pixel = bi.getRGB(i, j);

				int pixelNext = bi.getRGB(i + 1, j);

				/**
				 * 
				 * 分别进行位操作得到 r g b上的值
				 * 
				 */

				rgb[0] = (pixel & 0xff0000) >> 16;

				rgb[1] = (pixel & 0xff00) >> 8;

				rgb[2] = (pixel & 0xff);

				/**
				 * 
				 * 进行换色操作，我这里是要换成白底，那么就判断图片中rgb值是否在范围内的像素
				 * 
				 */

				// 经过不断尝试，RGB数值相互间相差15以内的都基本上是灰色，

				// 对以身份证来说特别是介于73到78之间，还有大于100的部分RGB值都是干扰色，将它们一次性转变成白色

				if ((Math.abs(rgb[0] - rgb[1]) < 15)

						&& (Math.abs(rgb[0] - rgb[2]) < 15)

						&& (Math.abs(rgb[1] - rgb[2]) < 15) &&

						(((rgb[0] > 73) && (rgb[0] < 78)) || (rgb[0] > 100))) {

					// 进行换色操作,0xffffff是白色

					bi.setRGB(i, j, 0xffffff);

				}

			}

		}

		return bi;

	}

	/**
	 * BufferedImage转换成Mat
	 *
	 * @param original
	 *            要转换的BufferedImage
	 * @param imgType
	 *            bufferedImage的类型 如 BufferedImage.TYPE_3BYTE_BGR
	 * @param matType
	 *            转换成mat的type 如 CvType.CV_8UC3
	 */
	public static Mat BufImg2Mat(BufferedImage original, int imgType, int matType) {
		if (original == null) {
			throw new IllegalArgumentException("original == null");
		}
		if (original.getType() != imgType) {
			BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);
			Graphics2D g = image.createGraphics();
			try {
				g.setComposite(AlphaComposite.Src);
				g.drawImage(original, 0, 0, null);
			} finally {
				g.dispose();
			}
		}
		DataBufferByte dbi = (DataBufferByte) original.getRaster().getDataBuffer();
		byte[] pixels = dbi.getData();
		MatExpr mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
		Mat nmat = mat.asMat();
		ByteIndexer nIndex = nmat.createIndexer();
		nIndex.put(0, 0, pixels);
		return nmat;
	}

	/**
	 * 人眼识别
	 *
	 * @param mat
	 * @return
	 */
	public static List<Point> eye(Mat mat) {
		List<Point> eyeList = new ArrayList<>();
		CascadeClassifier eyeDetector = new CascadeClassifier(
				System.getProperty("user.dir") + "\\opencv\\haarcascades\\haarcascade_eye.xml");
		// 在图片中检测人眼
		RectVector eyeDetections = new RectVector();
		// 指定人脸识别的最大和最小像素范围
		Size minSize = new Size(20, 20);
		Size maxSize = new Size(30, 30);

		eyeDetector.detectMultiScale(mat, eyeDetections, 1.1f, 3, 0, minSize, maxSize);

		if (eyeDetections != null && eyeDetections.size() == 2) {
			Point point1 = new Point(eyeDetections.get(0).x(), eyeDetections.get(0).y());
			eyeList.add(point1);
			Point point2 = new Point(eyeDetections.get(1).x(), eyeDetections.get(1).y());
			eyeList.add(point2);
		} else {
			return null;
		}
		return eyeList;
	}

	/**
	 * 获取轮廓的顶点坐标
	 *
	 * @param contour
	 * @return
	 */
	public static List<Point> getPointList(PointVector contour) {
		Mat mat2f = new Mat(contour);
		RotatedRect rect = opencv_imgproc.minAreaRect(mat2f);
		Mat points = new Mat();
		opencv_imgproc.boxPoints(rect, points);
		return getPoints(points.toString());
	}

	/**
	 * 获取轮廓的面积
	 *
	 * @param contour
	 * @return
	 */
	public static double area(Mat contour) {
		RotatedRect rect = opencv_imgproc.minAreaRect(contour);
		return rect.boundingRect().area();
	}

	/**
	 * 获取点坐标集合
	 *
	 * @param str
	 * @return
	 */
	public static List<Point> getPoints(String str) {
		List<Point> points = new ArrayList<>();
		str = str.replace("[", "").replace("]", "");
		String[] pointStr = str.split(";");
		for (int i = 0; i < pointStr.length; i++) {
			double x = Double.parseDouble(pointStr[i].split(",")[0]);
			double y = Double.parseDouble(pointStr[i].split(",")[1]);
			Point po = new Point((int) x, (int) y);
			points.add(po);
		}
		return points;
	}

	/**
	 * 获取最小的X坐标
	 *
	 * @param points
	 * @return
	 */
	public static int minX(List<Point2d> points) {
		Collections.sort(points, new XComparator(false));
		return (int) (points.get(0).x() > 0 ? points.get(0).x() : -points.get(0).x());
	}

	/**
	 * 获取最小的Y坐标
	 *
	 * @param points
	 * @return
	 */
	public static int minY(List<Point2d> points) {
		Collections.sort(points, new YComparator(false));
		return (int) (points.get(0).y() > 0 ? points.get(0).y() : -points.get(0).y());
	}

	/**
	 * 获取最长的X坐标距离
	 *
	 * @param points
	 * @return
	 */
	public static int xLength(List<Point2d> points) {
		Collections.sort(points, new XComparator(false));
		return (int) (points.get(3).x() - points.get(0).x());
	}

	/**
	 * 获取最长的Y坐标距离
	 *
	 * @param points
	 * @return
	 */
	public static int yLength(List<Point2d> points) {
		Collections.sort(points, new YComparator(false));
		return (int) (points.get(3).y() - points.get(0).y());
	}

	// 集合排序规则（根据X坐标排序）
	public static class XComparator implements Comparator<Point2d> {
		private boolean reverseOrder; // 是否倒序

		public XComparator(boolean reverseOrder) {
			this.reverseOrder = reverseOrder;
		}

		public int compare(Point2d arg0, Point2d arg1) {
			if (reverseOrder)
				return (int) arg1.x() - (int) arg0.x();
			else
				return (int) arg0.x() - (int) arg1.x();
		}
	}

	// 集合排序规则（根据Y坐标排序）
	public static class YComparator implements Comparator<Point2d> {
		private boolean reverseOrder; // 是否倒序

		public YComparator(boolean reverseOrder) {
			this.reverseOrder = reverseOrder;
		}

		public int compare(Point2d arg0, Point2d arg1) {
			if (reverseOrder)
				return (int) arg1.y() - (int) arg0.y();
			else
				return (int) arg0.y() - (int) arg1.y();
		}
	}

}