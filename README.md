# 项目介绍
本项目是从https://gitee.com/nbsl/idCardCv fork过来的,修改原有的需要安装opencv的过程，全部使用javacv技术重构，不需要
安装openCV,通过javacv引入需要的jar进行开发。
新增的了前端控制识别区域的功能，新增了后端识别后验证 
![前端效果页面](https://gitee.com/endlesshh/idCardCv/blob/master/img/1.jpg)



# 身份证图像识别
idCard是一个开源的身份证识别系统，其目标是成为一个简单、高效、准确的非限制场景(unconstrained situation)下的身份证识别库。

相比于其他的身份证识别系统，idCard有如下特点：

它基于openCV这个开源库。这意味着你可以获取全部源代码，并且移植到opencv支持的所有平台。
它是基于java开发。
它的识别率较高。图片清晰情况下，号码检测与字符识别可以达到90%以上的精度。

# 待完成工作
------------
* 身份证头像识别
* 中文字符训练
* 姓名、民族、性别、出生日期等定位识别
 
 Required Software
------------
本版本在以下平台测试通过：
* windows7 64bit
* Eclipse (Luna)
* jdk1.8.0_45
* junit 4
* opencv3.4.3
 