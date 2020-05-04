package domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author K神带你飞
 * @since 2020-03-17
 */
@Data
@Accessors(chain = true)
public class TCzrk { 
	 
	private String guid; 
	private String rybh; //人员编号
	 
	private String gmsfhm; //身份证
	 
	private String xm; //姓名
	 
	private String xmpy; //姓名拼音
	 
	private String xbdm; //性别代码
 
	private String csrq;   //出生日期
	 
	private String mzdm; //mzdm
 
	private String whcddm; //文化程度代码
	 
	private String xxdm; //血型代码
	 
	private String zylb; //职业类别
	 
	private String hjqh; //户籍区划
	 
	private String hjxz; //户籍祥址
	 
	private String yshjxz; //原始户籍祥址
	 
	private String xzzqh; //现住址区划
 
	private String xzzxz; //现住址
	 
	private String ysxzzxz; //原住址
 
	private String hjssfjdm; //户籍所属分局代码
 
	private String hjsspcsdm; //户籍所属派出所代码
	 
	private String hjsszrqdm;//户籍所属责任区
	
	private String xzzssfjdm; //现住址所属分局
	private String xzzsspcsdm; //现住址所属派出所
	private String xzzsszrqdm; //现住址所属责任区
	private String hjhuid; //户籍huid
	private String xzzhuid; //现住址huid
	private String rylb; //人员类别
	private String cjry; //采集人员
	private String cjsj; //采集时间
	private String cym;  //曾用名
	private String yhzgxdm; //与户主关系代码
	private String sg; //身高
	private String byzkdm; //兵役状况
	private String zjxydm; //宗教信仰代码
	private String lxdh; //联系电话
	private String fwcs; // 房屋层数
	private String hyzkdm; //婚姻状态
	private String hh; // 户号
	private String gzrylx; //
	private String gddh; //固定电话
	private String yzrrgxdm; //
	private String bfxxdz; //备份信息地址
	private String xpxlh;
	private String zy; //职业
	private String mz; //民族
	private String xb; //性别
	private String qrxx;
	private String rxzp; //照片
	private Long sjly;  //数据来源
	private String zhgxsj; //最后更新时间
	private String whcd; //化程度代码
	private String zzmm; //政治面貌
	private String gzdw; //雇主单位
	private String sfhz;//是否户主
	private String hkxz;//户口详址
	private String hyzk; //婚姻状况
	private String hjgx;
	private String oldxzzhuid; //旧现住址户id
	private Long isld; //是否流动人口
	private Long partymember; //是否党员
	private Long dibao;  //是低保
	private Long disabled; //是否残疾
	private Long veteran;
	private Long advancedage;
	private Long yfdx; //优抚对象
	private Long onechild;//独生子
	private Long jls; //是否军烈属
	private String politicalstatus; //政治面貌
	private String laborandsocialsecuritynumber; //劳动保障证号
	private String workunit; //工作单位
	private Long newrylb; //人员类别  1, "户籍人口"  2, "流动人口"  3, "寄住人口"
	private String dwCode;
	private Long isdel;
	private String photo;
	public String mehcenterx;
	public String mehcentery;
	public String mehcroods;
	public String mehcroodsgeometry;


 
}
