package endless.utils;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point2d;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.springframework.util.ResourceUtils;

import com.google.common.base.CharMatcher;
import com.nbsl.cv.utils.CoreFunc;
import com.nbsl.cv.utils.OCRUtil;
import com.nbsl.cv.utils.OpencvUtil;


public class IdCardCodeUtils {
	public static String saveStepFile = "F:/face/";

	public static void main(String[] args) throws Exception {

		String text = IdCardCodeUtils.idCard(ResourceUtils.getFile("classpath:test/5.jpg").getAbsolutePath());
		System.out.println(text);
	}


	/**
	 * 身份证正面识别
	 */
	public static String idCard(String imagePath) throws  Exception{
		Mat mat = opencv_imgcodecs.imread(imagePath); // 原图 
		mat = OpencvUtil.gray(mat);
		// 二值化 此处绝定图片的清晰度
		opencv_imgproc.adaptiveThreshold(mat, mat, 255, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
				opencv_imgproc.THRESH_BINARY_INV, 25, 10);
		// 腐蚀  去除背景图片
		mat = OpencvUtil.erode(mat, 1); 
		return toCard(mat);

	}

	public static float x1 = 0.45f;
	public static float x2 = 1.0f;
	public static float y1 = 0.77f;
	public static float y2 = 0.91f;
	
	public static String toCard(Mat mat){ 
		List<Point2d> list= Stream.of(new Point2d(mat.cols() * x1,mat.rows() * y1),
				new Point2d(mat.cols() * x1, mat.rows()* y1),
				new Point2d(mat.cols() * x2, mat.rows()* y2),
				new Point2d(mat.cols() * x2, mat.rows()* y2)).collect(Collectors.toList()); 
		Mat card= OpencvUtil.shear(mat,list); 
		opencv_imgproc.GaussianBlur(card, card, new Size(3, 3), 0, 0, opencv_core.BORDER_DEFAULT); 
		double thresholdValue = CoreFunc.otsu(card) - 25;// 减50是去掉文字周边燥点
		// 阴影覆盖; 
		opencv_imgproc.threshold(card, card, thresholdValue, 255, opencv_imgproc.THRESH_BINARY_INV);
		opencv_imgproc.medianBlur(card, card, 3);  
		opencv_imgcodecs.imwrite(saveStepFile +"charsMat.jpg", card); 
		//使用tess4j进行识别
		System.out.println("-------------");
		BufferedImage nameBuffer=OpencvUtil.Mat2BufImg(card,".png");
		String nameStr=OCRUtil.getImageMessage(nameBuffer,"chi_sim",false);
		String code = "";
		if(StringUtils.isNotBlank(nameStr)){
			nameStr=nameStr.replace("\n","");
			String codeX = CharMatcher.DIGIT.removeFrom(nameStr);
			code = CharMatcher.DIGIT.retainFrom(nameStr) + (StringUtils.isNotBlank(codeX)?("X".equalsIgnoreCase(codeX.substring(0,1))?"X":""):"");
		} 
		System.out.println(code);
		System.out.println("-------------"); 
		return code;
	} 

}
