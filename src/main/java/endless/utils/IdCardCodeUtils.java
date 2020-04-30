package endless.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point2d;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;

import com.google.common.base.CharMatcher;
import com.nbsl.cv.utils.CoreFunc;
import com.nbsl.cv.utils.OCRUtil;
import com.nbsl.cv.utils.OpencvUtil;

public class IdCardCodeUtils {
	public static String saveStepFile = "F:/face/";

	public static void main(String[] args) throws Exception {

	}

	/**
	 * 身份证号码识别
	 */
	public static String idCard(String imagePath) throws Exception {
		Mat mat = opencv_imgcodecs.imread(imagePath); // 原图
		return toCard(mat);

	}

	//页面中号码所在位置
	public static float x1 = 0.35f;
	public static float x2 = 1.0f;
	public static float y1 = 0.75f;
	public static float y2 = 0.91f;

	public static String toCard(Mat mat) throws IOException {
		// 1获取指定身份证号码区域
		List<Point2d> list = Stream
				.of(new Point2d(mat.cols() * x1, mat.rows() * y1), new Point2d(mat.cols() * x1, mat.rows() * y1),
						new Point2d(mat.cols() * x2, mat.rows() * y2), new Point2d(mat.cols() * x2, mat.rows() * y2))
				.collect(Collectors.toList());
		//2裁剪身份证号码区域图片
		Mat card = OpencvUtil.shear(mat, list);
		//3裁剪数字区域
		Rect rect = detectTextArea(card);
		if(rect == null){
			return "";
		}
		card = new Mat(card, rect);
		//4转为bufferImge
		opencv_imgcodecs.imwrite(saveStepFile + "card.png", card);
		BufferedImage nameBuffer = OpencvUtil.Mat2BufImg(card, ".png");
		//5使用tess4j识别
		String nameStr = OCRUtil.getImageMessage(nameBuffer, "chi_sim", false);
		String code = "";
		if (StringUtils.isNotBlank(nameStr)) {
			nameStr = nameStr.replace("\n", "");
			String codeX = CharMatcher.DIGIT.removeFrom(nameStr);
			code = CharMatcher.DIGIT.retainFrom(nameStr)
					+ (StringUtils.isNotBlank(codeX) ? ("X".equalsIgnoreCase(codeX.substring(0, 1)) ? "X" : "") : "");
		}
		System.out.println(code);
		return code;
	}

	private static Rect detectTextArea(Mat srcMat) {

		Mat grayMat = new Mat(); // 灰度图 
		opencv_imgproc.cvtColor(srcMat, grayMat, opencv_imgproc.COLOR_RGB2GRAY);// 灰度化
		// 高斯模糊 的原理(周边像素的平均值+正态分布的权重
		opencv_imgproc.GaussianBlur(grayMat, grayMat, new Size(7, 7), 0, 0, opencv_core.BORDER_DEFAULT);
		// 因为边缘部分的像素值是与旁边像素明显有区别的，所以对图片局部求极值，就可以得到整幅图片的边缘信息了
		grayMat = CoreFunc.Sobel(grayMat);
		// opencv_imgcodecs.imwrite("temp/Sobel.jpg", grayMat);
		opencv_imgcodecs.imwrite(saveStepFile + "Sobel.jpg", grayMat);

		opencv_imgproc.threshold(grayMat, grayMat, 0, 255, opencv_imgproc.THRESH_OTSU + opencv_imgproc.THRESH_BINARY);
		opencv_imgproc.medianBlur(grayMat, grayMat, 13);

		// opencv_imgcodecs.imwrite("temp/grayMat.jpg", grayMat);
		opencv_imgcodecs.imwrite(saveStepFile + "grayMat.jpg", grayMat);

		// 使用闭操作。对图像进行闭操作以后，可以看到车牌区域被连接成一个矩形装的区域。
		Mat element = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(5, 3));
		opencv_imgproc.morphologyEx(grayMat, grayMat, opencv_imgproc.MORPH_CLOSE, element);

		// opencv_imgcodecs.imwrite("temp/MORPH_CLOSE.jpg", grayMat);
		opencv_imgcodecs.imwrite(saveStepFile + "MORPH_CLOSE.jpg", grayMat);

		/**
		 * 轮廓提取()
		 */
		MatVector contoursList = new MatVector();
		Mat hierarchy = new Mat();
		opencv_imgproc.findContours(grayMat, contoursList, hierarchy, opencv_imgproc.RETR_EXTERNAL,
				opencv_imgproc.CHAIN_APPROX_SIMPLE);
		
		Rect rect = null;
		int minWidth = grayMat.cols() / 2;
		for(long i=0,total = contoursList.size();i<total;i++){
			rect = opencv_imgproc.boundingRect(contoursList.get(i)); 
			if(rect.width() < minWidth){
				rect = null;
				continue;
			}
			break;
		}
				
				
		if (rect != null) { 
			int x = rect.x();
			int y = rect.y();
			int w = rect.width();
			int h = rect.height();
			rect.x(tryXy(x,2));
			rect.y(tryXy(y,2));
			rect.width(w + tryValue(grayMat.cols(),x+w,3));
			rect.height(h + tryValue(grayMat.rows(),y+h,3));
			opencv_imgcodecs.imwrite(saveStepFile + "rectMat.jpg", new Mat(grayMat, rect));
			return rect;
		}
		return rect;
	} 
	
	private static int tryXy(int x,int down){
		int endV = x - down;
		while(endV < 0){
			endV++;
		} 
		return endV;
	}
	
	private static int tryValue(int maxL,int oldL,int addL){
		int endV = 0;
		do{
			endV = addL;
			addL--;
		}while(maxL < oldL + addL);
		
		return endV;
	}
	
}
