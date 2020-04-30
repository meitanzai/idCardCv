package endless.controller;

import java.io.File;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;

import org.bytedeco.javacpp.opencv_imgcodecs;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdcardUtil;
import endless.utils.ExtResult;
import endless.utils.IdCardCodeUtils;
import endless.utils.WorkId;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import net.coobird.thumbnailator.Thumbnails;

/**
 * <pre>
 *   身份证扫描服务
 * </pre>
 * 
 * <small>2020年4月28日14:59:28 | endLess</small>
 */
@RestController
@RequestMapping("/api/idCard/")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApiIdCardController{
	
	 
	@PostMapping(value = "scanCard")
    @ApiOperation( value = "扫描身份证号码", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
    public ExtResult<String> scanCard(HttpServletRequest request,
			@ApiParam(name = "svg_xml", required = true, value = "svg_xml") @RequestParam(value = "svg_xml",required = true) String svg_xml){
    	try {
    		File temp = Files.createTempFile(WorkId.sortUID()+"", ".png").toFile(); 
    		Base64.decodeToFile(svg_xml.substring(22,svg_xml.length()), temp);
    		Thumbnails.of(temp).size(290, 384).toFile("F:/face/size.png");
    		Thumbnails.of(temp).size(290, 384).toFile(temp); 
    		
    		String code = IdCardCodeUtils.idCard(temp.getAbsolutePath());
    		System.out.println(code);
    		if(IdcardUtil.isValidCard(code)){
    			return ExtResult.ok(code);
    		}else{
    			return ExtResult.fail_msg(code+"不是正确的身份证号码，请重新识别");
    		} 
		} catch (Exception e) {
			e.printStackTrace();
			return ExtResult.fail_msg("服务出现异常");
		}
    }
}
