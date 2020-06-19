package com.chis.modules.portal.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;

import com.chis.common.utils.SpringContextHolder;
import com.chis.common.utils.ZoneUtil;
import com.chis.modules.pwsjd.service.BusinessCountService;
import com.chis.modules.pwsjd.service.TdJdCheckrecService;
import com.chis.modules.system.entity.SearchMsgEntity;
import com.chis.modules.system.service.TsZoneService;
import com.chis.modules.system.utils.Global;
import com.github.abel533.echarts.axis.AxisLabel;
import com.github.abel533.echarts.axis.AxisLine;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.AxisType;
import com.github.abel533.echarts.code.PointerType;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.code.X;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Bar;
import com.github.abel533.echarts.series.Line;
import com.github.abel533.echarts.style.LineStyle;
import com.github.abel533.echarts.style.TextStyle;

@ManagedBean(name = "businessCountViewBean")
@ViewScoped
public class BusinessCountViewBean {

	/**时间段状态 1.本月，2.本季度，3.本年*/
	private String dateState;
	
	/**选择卡片类型状态 1.监督次数，2.监督覆盖率，3.案件次数，4.非法行医数，5.协管巡查数*/
	private String typeState;
	
	/**选择图标类型状态 1.柱状图，2.折线图*/
	private String echartsState;
	/**柱状图颜色*/
	private String color;
	
	/**头部list*/
	private List<Map<String,Object>> mapList = new ArrayList<>();
	
	/**卡片list*/
	private List<Map<String,Object>> cardList = new ArrayList<>();
	
	/**图标xml，公用一个*/
	private String countXml;
	
	/**时间段用于查询条件*/
	private String[] dateArr;
	
	/**日期List用于横坐标展示*/
	private List<String> addrList; 
	
	/**获取当前单位*/
	private String unitId;
	
	/**图表标题*/
	private String title;
	
	private TdJdCheckrecService tdJdCheckrecService = SpringContextHolder.getBean(TdJdCheckrecService.class);
	private TsZoneService tsZoneService = SpringContextHolder.getBean(TsZoneService.class);
	private BusinessCountService businessCountService = SpringContextHolder.getBean(BusinessCountService.class);
	
	/**初始化方法*/
	public BusinessCountViewBean(){
		/**默认本月内容*/
		if(StringUtils.isBlank(dateState)){
			dateState="1";
		}	
		/**默认监督次数*/
		if(StringUtils.isBlank(typeState)){
			typeState="1";
		}
		/**默认柱状图*/
		if(StringUtils.isBlank(echartsState)){
			echartsState="1";
		}
		/**获取当前单位*/
		unitId = Global.getTsUnit().getRid().toString();
		
		Map<String,Object> quarterMap = new HashMap<String,Object>();
		/**查询条件本季度*/
		quarterMap.put("typeName","本月");
		quarterMap.put("dateState", "1");
		mapList.add(quarterMap);
	
		Map<String,Object> monthMap = new HashMap<String,Object>();
		/**查询条件本月*/
		monthMap.put("typeName","本季");
		monthMap.put("dateState", "2");
		mapList.add(monthMap);
		
		Map<String,Object> yearMap = new HashMap<String,Object>();
		/**查询条件本年*/
		yearMap.put("typeName","本年");
		yearMap.put("dateState", "3");
		mapList.add(yearMap);
		
		/**默认当前月查询条件*/
		this.getMonths();
		
		/**获取地区list作为横坐标*/
		addrList = tsZoneService.selectZoneName();
		
		/**默认显示第一个图表*/
		this.changeCardAction();
		
		/**默认颜色*/
		color = "#26A3FF";
	}
	
	/**点击卡片触发方法*/
	public void changeCardAction(){
		/**获取卡片类型*/
		this.changeDateAction();
		
	}

	/**点击echarts图表类型切换图表*/
	public void changeEchartsAction(){
		/**获取卡片类型*/
		this.changeDateAction();
	}
	
	/**初始化柱状图*/
	public void initEchartsBar(){
		 Object[] objArr = null;
		 
			GsonOption op = new GsonOption();
			op.tooltip().trigger(Trigger.axis);
			op.tooltip().axisPointer().type(PointerType.shadow);
			op.grid().x(50).x2(50).y(50).y2(40);
			this.getEchartsTitle();
			op.title(title);
			 op.title().x(X.center);
			 op.title().y(10);
		
			 TextStyle style = new TextStyle();
			style.setColor("#292929");
			style.setFontSize(20);
			op.title().textStyle(style);
			op.backgroundColor("#ffffff");
			
			//横坐标
			CategoryAxis 	xAxis = new CategoryAxis();
			xAxis.setData(addrList);
			xAxis.setType(AxisType.category);
			AxisLabel axisLabel = new AxisLabel();
			axisLabel.setInterval(0);
			xAxis.setAxisLabel(axisLabel);
			xAxis.setShow(true);
			op.xAxis().add(xAxis);
			op.calculable(false);
			
			Bar bar = new Bar();
			bar.name("地区");
			/**查询数据*/
			objArr = this.getArrData();
			bar.data(objArr);
			
			bar.barWidth(20);
			bar.itemStyle().normal().color(color);
			op.series().add(bar);
		
			//纵坐标
			ValueAxis valueAxis = new ValueAxis();
			valueAxis.setType(AxisType.value);
			op.yAxis().add(valueAxis);
		
			this.countXml = op.toString();
			
	}
	
	/**初始化折线图*/
	public void initEchartsLine(){
			
		Object[] objArr = null;
			GsonOption op = new GsonOption();
			op.tooltip().trigger(Trigger.item);
			op.grid().x(50).x2(50).y(50).y2(40);
			this.getEchartsTitle();
			op.title(title);
			op.title().x(X.center);
			 op.title().y(10);
		
			 TextStyle style = new TextStyle();
			style.setColor("#292929");
			style.setFontSize(20);
			op.title().textStyle(style);
			op.backgroundColor("#ffffff");
			
			//横坐标
			CategoryAxis superviseXAxis = new CategoryAxis();
			superviseXAxis.setData(addrList);
			
			AxisLine axisLine = new AxisLine();
			LineStyle superviseLineStyle = new LineStyle();
			superviseLineStyle.setColor("#DEDEDE");
			superviseLineStyle.setWidth(0);
			axisLine.setLineStyle(superviseLineStyle);
			superviseXAxis.setAxisLine(axisLine);
			superviseXAxis.setType(AxisType.category);
			superviseXAxis.setShow(true);
			op.xAxis().add(superviseXAxis);
			AxisLabel axisLabel = new AxisLabel();
			axisLabel.setInterval(0);
			superviseXAxis.setAxisLabel(axisLabel);
			op.calculable(false);
			
			
			//纵坐标
			ValueAxis valueAxis = new ValueAxis();
			valueAxis.setAxisLine(axisLine);
			valueAxis.setType(AxisType.value);
			op.yAxis().add(valueAxis);
				
			Line line = new Line();
			line.name("次数");
			
			/**查询数据*/
			objArr = this.getArrData();
			line.data(objArr);
			line.itemStyle().normal().color(color);
			op.series().add(line);
			
			this.countXml = op.toString();
		
	}
	
	/**根据卡片类型查询数据*/
	private Object[] getArrData(){
		 SearchMsgEntity entity = new SearchMsgEntity();
		 entity.setDateArr(dateArr);
		 entity.setDateState(dateState);
		 String zoneCode = null;
		if (StringUtils.isNotBlank(Global.getTsUnit().getFkByZoneId().getZoneCode())) {
			zoneCode = ZoneUtil.zoneSelect(Global.getTsUnit().getFkByZoneId().getZoneCode());
		}
		entity.setSrchZoneCode(zoneCode);
		 Object[] objArr = null;
		 if("1".equals(typeState)){
			 /**查询监督数*/
			 List<Integer> countList = businessCountService.selectSuperviseCount(entity);
			if(countList!=null&&countList.size()>0){
				objArr = countList.toArray();
			}
			color = "#8583EB";
		 }else if("2".equals(typeState)){
			 /**查询覆盖率*/
			 List<String> newList = new ArrayList<>();
			 List<Integer> countList = businessCountService.selectSuperviseCount(entity);
			 List<Integer> allCountList = businessCountService.selectComptypeCount(null);
			 DecimalFormat df = new DecimalFormat("0.0%");
			 for(int i=0;i<allCountList.size();i++){
				 Integer allCount = allCountList.get(i);
				 if(allCount!=0){
					 BigDecimal rate=new BigDecimal(countList.get(i)).divide(new BigDecimal(allCount),5,RoundingMode.HALF_DOWN).multiply(new BigDecimal(100)).setScale(2,RoundingMode.HALF_DOWN);
				 	newList.add(rate.toString());
				 }else{
					 newList.add("0");
				 }
			 }
			 if(newList!=null&&newList.size()>0){
				 objArr = newList.toArray();
			 }
			 color = "#26A3FF";
		 }else if("3".equals(typeState)){
			 entity.setSpePri(null);
			 /**查询案件次数*/
			 List<Integer> countList = businessCountService.selectCaseCount(entity);
				if(countList!=null&&countList.size()>0){
					objArr = countList.toArray();
				}
			color = "#FF8226";
		 }else if("4".equals(typeState)){
			 /**查询非法行医数*/
			 entity.setSpePri("15");
			 List<Integer> countList = businessCountService.selectCaseCount(entity);
				if(countList!=null&&countList.size()>0){
					objArr = countList.toArray();
				}
			color = "#FF8226";
		 }else{
			 /**协管巡查数*/
			 List<Integer> countList = businessCountService.selectXgSuperviseCount(entity);
				if(countList!=null&&countList.size()>0){
					objArr = countList.toArray();
				}
				color = "#26A3FF";
		 }
		 return objArr;
	}
	
	
	/**点击切换时间按钮时触发*/
	public void changeDateAction(){
		if("1".equals(dateState)){
			this.getMonths();
		}else if("2".equals(dateState)){
			this.getQuarter();
		}else{
			this.getYears();
		}
		
		this.initCard();
		
		if("1".equals(echartsState)){
			this.initEchartsBar();
		}else{
			this.initEchartsLine();
		}
		
	}

	/**初始化卡片List*/
	public void initCard(){
		cardList = new ArrayList<>();
		String zoneCode = null;
		if (StringUtils.isNotBlank(Global.getTsUnit().getFkByZoneId().getZoneCode())) {
			zoneCode = ZoneUtil.zoneSelect(Global.getTsUnit().getFkByZoneId().getZoneCode());
		}
		
		
		
		SearchMsgEntity entity = new SearchMsgEntity();
		/**查询该单位监督次数*/
		Map<String,Object> superviseMap = new HashMap<>(); 
		entity.setDateState(dateState);
		entity.setDateArr(dateArr);
		entity.setSrchZoneCode(zoneCode);
		Integer superviseSum = tdJdCheckrecService.selectByEmpIdAndDate(entity);
		superviseMap.put("sum", superviseSum);
	 	superviseMap.put("typeName", "监督次数");
	 	superviseMap.put("typeState", "1");
	 	superviseMap.put("color", "#8583EB");
		cardList.add(superviseMap);
		
	 	/**查询该单位下所有的单位数，计算覆盖率*/
	 	Map<String,Object> rateMap = new HashMap<>(); 
	 	entity = new SearchMsgEntity();
		entity.setSrchZoneCode(zoneCode);
	 	List<Integer> allCountList = businessCountService.selectComptypeCount(entity);
	 	Integer allSum=allCountList.get(0);
	 	if(allSum!=0){	
	 		BigDecimal rate=new BigDecimal(superviseSum).divide(new BigDecimal(allSum),5,RoundingMode.HALF_DOWN).multiply(new BigDecimal(100)).setScale(2,RoundingMode.HALF_DOWN);
	 		rateMap.put("sum", rate.toString()+"%");
	 	}else{
	 		rateMap.put("sum", 0);
	 	}
	 	rateMap.put("typeName", "覆盖率");
	 	rateMap.put("typeState", "2");
	 	rateMap.put("color", "#26A3FF");
	 	cardList.add(rateMap);
	 	
	 	/**查询案件次数*/
	 	Map<String,Object> caseMap = new HashMap<>(); 
	 	entity = new SearchMsgEntity();
	 	entity.setDateState(dateState);
	 	entity.setDateArr(dateArr);
	 	entity.setSrchZoneCode(zoneCode);
		Integer caseSum = businessCountService.selectByEmpIdAndDate(entity);
		caseMap.put("sum", caseSum);
		caseMap.put("typeName", "案件次数");
		caseMap.put("typeState", "3");
		caseMap.put("color", "#FF8226");
		cardList.add(caseMap);
	 	
	 	/**查询非法行医数*/
	 	Map<String,Object> unlawMap = new HashMap<>(); 
	 	entity = new SearchMsgEntity();
	 	entity.setDateState(dateState);
	 	entity.setDateArr(dateArr);
	 	entity.setSpePri("15");
	 	entity.setSrchZoneCode(zoneCode);
	 	Integer unlawSum = businessCountService.selectByEmpIdAndDate(entity);
	 	unlawMap.put("sum", unlawSum);
	 	unlawMap.put("typeName", "非法行医数");
	 	unlawMap.put("typeState", "4");
	 	unlawMap.put("color", "#FF8226");
	 	cardList.add(unlawMap);
	 	
	 	/**查询协管巡查数*/
	 	Map<String,Object> xgMap = new HashMap<>(); 
		entity = new SearchMsgEntity();
	 	entity.setDateState(dateState);
	 	entity.setDateArr(dateArr);
	 	entity.setSrchZoneCode(zoneCode);
	 	Integer xgSum = businessCountService.selectXgCount(entity);
	 	
	 	xgMap.put("sum", xgSum);
	 	xgMap.put("typeName", "协管巡查数");
	 	xgMap.put("typeState", "5");
	 	xgMap.put("color", "#26A3FF");
	 	cardList.add(xgMap);
	}
	
	

	/**获取当前月*/
	private void getMonths(){
		dateArr = new String[1];
		Calendar calendar=Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;
		if((month+"").length()>1){
			dateArr[0] = year+"-"+month;
		}else{
			dateArr[0] = year+"-0"+month;
		}
	}
	
	
	/**根据当前月份，获取当前季度，用于查询当前季度的数据*/
	private void getQuarter(){
		Calendar calendar=Calendar.getInstance();
		//获得当前时间的月份，月份从0开始所以结果要加1
		int month=calendar.get(Calendar.MONTH)+1;
		int year = calendar.get(Calendar.YEAR);
		dateArr = new String[3];
		if(1==month||2==month||3==month){
			/**第一季度*/
			dateArr[0] = year+"-01";
			dateArr[1] = year+"-02";
			dateArr[2] = year+"-03";
		}else if(4==month||5==month||6==month){
			/**第二季度*/
			dateArr[0] = year+"-04";
			dateArr[1] = year+"-05";
			dateArr[2] = year+"-06";
		}else if(7==month||8==month||9==month){
			/**第三季度*/
			dateArr[0] = year+"-07";
			dateArr[1] = year+"-08";
			dateArr[2] = year+"-09";
		}else{
			/**第四季度*/
			dateArr[0] = year+"-10";
			dateArr[1] = year+"-11";
			dateArr[2] = year+"-12";
		}
	}
	
	/**根据当前年份查询*/
	private void getYears(){
		Calendar calendar=Calendar.getInstance();
		//获得当前时间的月份，月份从0开始所以结果要加1
		int year = calendar.get(Calendar.YEAR);
		dateArr = new String[1];
		dateArr[0] = year+"";
	}
	
	/**初始化图表标题*/
	private void getEchartsTitle(){
		String msgOne = "";
		String msgTwo = "";
		if("1".equals(dateState)){
			msgOne = "本月度";
		}else if("2".equals(dateState)){
			msgOne = "本季度";
		}else if("3".equals(dateState)){
			msgOne = "本年度";
		}
		
		if("1".equals(typeState)){
			msgTwo = "监督次数";
		}else if("2".equals(typeState)){
			msgTwo = "监督覆盖率";
		}else if("3".equals(typeState)){
			msgTwo = "案件次数";
		}else if("4".equals(typeState)){
			msgTwo = "非法行医数";
		}else{
			msgTwo = "协管巡查数";
		}
			
		title = msgOne+msgTwo;
	}
	

	public List<Map<String, Object>> getMapList() {
		return mapList;
	}

	public void setMapList(List<Map<String, Object>> mapList) {
		this.mapList = mapList;
	}

	public List<Map<String, Object>> getCardList() {
		return cardList;
	}

	public void setCardList(List<Map<String, Object>> cardList) {
		this.cardList = cardList;
	}

	public String getCountXml() {
		return countXml;
	}

	public void setCountXml(String countXml) {
		this.countXml = countXml;
	}

	public String[] getDateArr() {
		return dateArr;
	}

	public void setDateArr(String[] dateArr) {
		this.dateArr = dateArr;
	}


	public List<String> getAddrList() {
		return addrList;
	}



	public void setAddrList(List<String> addrList) {
		this.addrList = addrList;
	}



	public String getDateState() {
		return dateState;
	}

	public void setDateState(String dateState) {
		this.dateState = dateState;
	}

	public String getTypeState() {
		return typeState;
	}

	public void setTypeState(String typeState) {
		this.typeState = typeState;
	}

	public String getEchartsState() {
		return echartsState;
	}

	public void setEchartsState(String echartsState) {
		this.echartsState = echartsState;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
