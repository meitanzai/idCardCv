package endless.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.ZooModel;
import endless.conf.ConfUtil;
import net.coobird.thumbnailator.Thumbnails;

@Service
public class OrcService {

    @Autowired
    private ZooModel<Image, DetectedObjects> detectionModel;
    
    @Autowired
    private ZooModel<Image, Classifications> directionModel;
    
    @Autowired
    private ZooModel<Image, String> recgonitionModel;
    
    

    public String orc(String imageUrl) {
    	
        try (Predictor<Image, DetectedObjects> detector = detectionModel.newPredictor();
        	 Predictor<Image, Classifications> direction= directionModel.newPredictor();
        		Predictor<Image, String> recgonition = recgonitionModel.newPredictor();	) {
        	Path imageFile = Paths.get(imageUrl);
            Image img = ImageFactory.getInstance().fromFile(imageFile);
            
            //获取文字框框
            DetectedObjects detectedObj = detector.predict(img);
            List<DetectedObjects.DetectedObject> boxes = detectedObj.items(); 
            
            //画框框
            if(ConfUtil.show){
            	 Image newImage = img.duplicate(Image.Type.TYPE_INT_ARGB);
                 newImage.drawBoundingBoxes(detectedObj); 
                 Thumbnails.of((BufferedImage)newImage.getWrappedImage()).scale(1).toFile(ConfUtil.stepLocal+File.separator+"size.png");
    		} 
            
            //识别框框中文字  是否旋转
            StringBuilder build = new StringBuilder();
            for(DetectedObjects.DetectedObject box : boxes){ 
            	 Image sample = getSubImage(img, box.getBoundingBox()); 
            	 //判断是否旋转
            	 //Classifications cl = direction.predict(sample);
            	 //识别的文字
            	 build.append("word:").append(recgonition.predict(sample)).append("\n");
            }
            return build.toString();
        } catch (Exception e) {
            throw new RuntimeException("fail", e);
        }
    }
  //扩展图片中文字的块，并裁剪
    static Image getSubImage(Image img, BoundingBox box) {
        Rectangle rect = box.getBounds();
        double[] extended = extendRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        int width = img.getWidth();
        int height = img.getHeight();
        int[] recovered = {
                (int) (extended[0] * width),
                (int) (extended[1] * height),
                (int) (extended[2] * width),
                (int) (extended[3] * height)
        };
        return img.getSubimage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }
    static double[] extendRect(double xmin, double ymin, double width, double height) {
        double centerx = xmin + width / 2;
        double centery = ymin + height / 2;
        if (width > height) {
            width += height * 2.0;
            height *= 3.0;
        } else {
            height += width * 2.0;
            width *= 3.0;
        }
        double newX = centerx - width / 2 < 0 ? 0 : centerx - width / 2;
        double newY = centery - height / 2 < 0 ? 0 : centery - height / 2;
        double newWidth = newX + width > 1 ? 1 - newX : width;
        double newHeight = newY + height > 1 ? 1 - newY : height;
        return new double[] {newX, newY, newWidth, newHeight};
    }
    private static int[] scale(int h, int w, int max) {
        int localMax = Math.max(h, w);
        float scale = 1.0f;
        if (max < localMax) {
            scale = max * 1.0f / localMax;
        }
        // paddle model only take 32-based size
        return resize32(h * scale, w * scale);
    }

    private static int[] resize32(double h, double w) {
        double min = Math.min(h, w);
        if (min < 32) {
            h = 32.0 / min * h;
            w = 32.0 / min * w;
        }
        int h32 = (int) h / 32;
        int w32 = (int) w / 32;
        return new int[]{h32 * 32, w32 * 32};
    }
    Image rotateImg(Image image) {
        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
            return ImageFactory.getInstance().fromNDArray(rotated);
        }
    }
}