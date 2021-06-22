package endless.conf;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpWordRotateTranslator;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslator;
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;

@Configuration
public class DjlConfig {

     
	/**
	 * Word detection model
	 * @return
	 * @throws Exception
	 */
    @Bean
    public ZooModel<Image, DetectedObjects> detectionModel() throws Exception {
    	Criteria<Image, DetectedObjects> criteria1 = Criteria.builder()
                .optEngine("PaddlePaddle")
                .setTypes(Image.class, DetectedObjects.class)
                //加载本地模型
               // .optModelPath(Paths.get("F:\\1\\model\\ch_ppocr_server_v2.0_det_infer.tar"))
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/det_db.zip")
                .optTranslator(new PpWordDetectionTranslator(new ConcurrentHashMap<String, String>()))
                .build(); 
		return ModelZoo.loadModel(criteria1); 
    }
    /**
     * Word Direction model
     * @return
     * @throws Exception
     */
    @Bean
    public ZooModel<Image, Classifications> directionModel() throws Exception {
    	Criteria<Image, Classifications> criteria1 = Criteria.builder()
                .optEngine("PaddlePaddle")
                .setTypes(Image.class, Classifications.class)
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/cls.zip")
                .optTranslator(new PpWordRotateTranslator())
                .build();
		return ModelZoo.loadModel(criteria1); 
    }
    /**
     * Word Recgonition model
     * @return
     * @throws Exception
     */
    @Bean
    public ZooModel<Image, String> recgonitionModel() throws Exception {
    	Criteria<Image, String> criteria1 = Criteria.builder()
                .optEngine("PaddlePaddle")
                .setTypes(Image.class, String.class)
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/rec_crnn.zip")
                .optTranslator(new PpWordRecognitionTranslator())
                .build();
    	return ModelZoo.loadModel(criteria1); 
    }
     
}