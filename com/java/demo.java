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

	/**ʱ���״̬ 1.���£�2.�����ȣ�3.����*/
	private String dateState;
	
	/**ѡ��Ƭ����״̬ 1.�ල������2.�ල�����ʣ�3.����������4.�Ƿ���ҽ����5.Э��Ѳ����*/
	private String typeState;
	
	/**ѡ��ͼ������״̬ 1.��״ͼ��2.����ͼ*/
	private String echartsState;
	/**��״ͼ��ɫ*/
	private String color;
	
	/**ͷ��list*/
	private List<Map<String,Object>> mapList = new ArrayList<>();
	
	/**��Ƭlist*/
	private List<Map<String,Object>> cardList = new ArrayList<>();
	
	/**ͼ��xml������һ��*/
	private String countXml;
	
	/**ʱ������ڲ�ѯ����*/
	private String[] dateArr;
	
	/**����List���ں�����չʾ*/
	private List<String> addrList; 
	
	/**��ȡ��ǰ��λ*/
	private String unitId;
	
	/**ͼ�����*/
	private String title;
	
	private TdJdCheckrecService tdJdCheckrecService = SpringContextHolder.getBean(TdJdCheckrecService.class);
	private TsZoneService tsZoneService = SpringContextHolder.getBean(TsZoneService.class);
	private BusinessCountService businessCountService = SpringContextHolder.getBean(BusinessCountService.class);
	
	/**��ʼ������*/
	public BusinessCountViewBean(){
		/**Ĭ�ϱ�������*/
		if(StringUtils.isBlank(dateState)){
			dateState="1";
		}	
		/**Ĭ�ϼල����*/
		if(StringUtils.isBlank(typeState)){
			typeState="1";
		}
		/**Ĭ����״ͼ*/
		if(StringUtils.isBlank(echartsState)){
			echartsState="1";
		}
		/**��ȡ��ǰ��λ*/
		unitId = Global.getTsUnit().getRid().toString();
		
		Map<String,Object> quarterMap = new HashMap<String,Object>();
		/**��ѯ����������*/
		quarterMap.put("typeName","����");
		quarterMap.put("dateState", "1");
		mapList.add(quarterMap);
	
		Map<String,Object> monthMap = new HashMap<String,Object>();
		/**��ѯ��������*/
		monthMap.put("typeName","����");
		monthMap.put("dateState", "2");
		mapList.add(monthMap);
		
		Map<String,Object> yearMap = new HashMap<String,Object>();
		/**��ѯ��������*/
		yearMap.put("typeName","����");
		yearMap.put("dateState", "3");
		mapList.add(yearMap);
		
		/**Ĭ�ϵ�ǰ�²�ѯ����*/
		this.getMonths();
		
		/**��ȡ����list��Ϊ������*/
		addrList = tsZoneService.selectZoneName();
		
		/**Ĭ����ʾ��һ��ͼ��*/
		this.changeCardAction();
		
		/**Ĭ����ɫ*/
		color = "#26A3FF";
	}
	
	/**�����Ƭ��������*/
	public void changeCardAction(){
		/**��ȡ��Ƭ����*/
		this.changeDateAction();
		
	}

	/**���echartsͼ�������л�ͼ��*/
	public void changeEchartsAction(){
		/**��ȡ��Ƭ����*/
		this.changeDateAction();
	}
	
	/**��ʼ����״ͼ*/
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
			
			//������
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
			bar.name("����");
			/**��ѯ����*/
			objArr = this.getArrData();
			bar.data(objArr);
			
			bar.barWidth(20);
			bar.itemStyle().normal().color(color);
			op.series().add(bar);
		
			//������
			ValueAxis valueAxis = new ValueAxis();
			valueAxis.setType(AxisType.value);
			op.yAxis().add(valueAxis);
		
			this.countXml = op.toString();
			
	}
	
	/**��ʼ������ͼ*/
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
			
			//������
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
			
			
			//������
			ValueAxis valueAxis = new ValueAxis();
			valueAxis.setAxisLine(axisLine);
			valueAxis.setType(AxisType.value);
			op.yAxis().add(valueAxis);
				
			Line line = new Line();
			line.name("����");
			
			/**��ѯ����*/
			objArr = this.getArrData();
			line.data(objArr);
			line.itemStyle().normal().color(color);
			op.series().add(line);
			
			this.countXml = op.toString();
		
	}
	
	/**���ݿ�Ƭ���Ͳ�ѯ����*/
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
			 /**��ѯ�ල��*/
			 List<Integer> countList = businessCountService.selectSuperviseCount(entity);
			if(countList!=null&&countList.size()>0){
				objArr = countList.toArray();
			}
			color = "#8583EB";
		 }else if("2".equals(typeState)){
			 /**��ѯ������*/
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
			 /**��ѯ��������*/
			 List<Integer> countList = businessCountService.selectCaseCount(entity);
				if(countList!=null&&countList.size()>0){
					objArr = countList.toArray();
				}
			color = "#FF8226";
		 }else if("4".equals(typeState)){
			 /**��ѯ�Ƿ���ҽ��*/
			 entity.setSpePri("15");
			 List<Integer> countList = businessCountService.selectCaseCount(entity);
				if(countList!=null&&countList.size()>0){
					objArr = countList.toArray();
				}
			color = "#FF8226";
		 }else{
			 /**Э��Ѳ����*/
			 List<Integer> countList = businessCountService.selectXgSuperviseCount(entity);
				if(countList!=null&&countList.size()>0){
					objArr = countList.toArray();
				}
				color = "#26A3FF";
		 }
		 return objArr;
	}
	
	
	/**����л�ʱ�䰴ťʱ����*/
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

	/**��ʼ����ƬList*/
	public void initCard(){
		cardList = new ArrayList<>();
		String zoneCode = null;
		if (StringUtils.isNotBlank(Global.getTsUnit().getFkByZoneId().getZoneCode())) {
			zoneCode = ZoneUtil.zoneSelect(Global.getTsUnit().getFkByZoneId().getZoneCode());
		}
		
		
		
		SearchMsgEntity entity = new SearchMsgEntity();
		/**��ѯ�õ�λ�ල����*/
		Map<String,Object> superviseMap = new HashMap<>(); 
		entity.setDateState(dateState);
		entity.setDateArr(dateArr);
		entity.setSrchZoneCode(zoneCode);
		Integer superviseSum = tdJdCheckrecService.selectByEmpIdAndDate(entity);
		superviseMap.put("sum", superviseSum);
	 	superviseMap.put("typeName", "�ල����");
	 	superviseMap.put("typeState", "1");
	 	superviseMap.put("color", "#8583EB");
		cardList.add(superviseMap);
		
	 	/**��ѯ�õ�λ�����еĵ�λ�������㸲����*/
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
	 	rateMap.put("typeName", "������");
	 	rateMap.put("typeState", "2");
	 	rateMap.put("color", "#26A3FF");
	 	cardList.add(rateMap);
	 	
	 	/**��ѯ��������*/
	 	Map<String,Object> caseMap = new HashMap<>(); 
	 	entity = new SearchMsgEntity();
	 	entity.setDateState(dateState);
	 	entity.setDateArr(dateArr);
	 	entity.setSrchZoneCode(zoneCode);
		Integer caseSum = businessCountService.selectByEmpIdAndDate(entity);
		caseMap.put("sum", caseSum);
		caseMap.put("typeName", "��������");
		caseMap.put("typeState", "3");
		caseMap.put("color", "#FF8226");
		cardList.add(caseMap);
	 	
	 	/**��ѯ�Ƿ���ҽ��*/
	 	Map<String,Object> unlawMap = new HashMap<>(); 
	 	entity = new SearchMsgEntity();
	 	entity.setDateState(dateState);
	 	entity.setDateArr(dateArr);
	 	entity.setSpePri("15");
	 	entity.setSrchZoneCode(zoneCode);
	 	Integer unlawSum = businessCountService.selectByEmpIdAndDate(entity);
	 	unlawMap.put("sum", unlawSum);
	 	unlawMap.put("typeName", "�Ƿ���ҽ��");
	 	unlawMap.put("typeState", "4");
	 	unlawMap.put("color", "#FF8226");
	 	cardList.add(unlawMap);
	 	
	 	/**��ѯЭ��Ѳ����*/
	 	Map<String,Object> xgMap = new HashMap<>(); 
		entity = new SearchMsgEntity();
	 	entity.setDateState(dateState);
	 	entity.setDateArr(dateArr);
	 	entity.setSrchZoneCode(zoneCode);
	 	Integer xgSum = businessCountService.selectXgCount(entity);
	 	
	 	xgMap.put("sum", xgSum);
	 	xgMap.put("typeName", "Э��Ѳ����");
	 	xgMap.put("typeState", "5");
	 	xgMap.put("color", "#26A3FF");
	 	cardList.add(xgMap);
	}
	
	

	/**��ȡ��ǰ��*/
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
	
	
	/**���ݵ�ǰ�·ݣ���ȡ��ǰ���ȣ����ڲ�ѯ��ǰ���ȵ�����*/
	private void getQuarter(){
		Calendar calendar=Calendar.getInstance();
		//��õ�ǰʱ����·ݣ��·ݴ�0��ʼ���Խ��Ҫ��1
		int month=calendar.get(Calendar.MONTH)+1;
		int year = calendar.get(Calendar.YEAR);
		dateArr = new String[3];
		if(1==month||2==month||3==month){
			/**��һ����*/
			dateArr[0] = year+"-01";
			dateArr[1] = year+"-02";
			dateArr[2] = year+"-03";
		}else if(4==month||5==month||6==month){
			/**�ڶ�����*/
			dateArr[0] = year+"-04";
			dateArr[1] = year+"-05";
			dateArr[2] = year+"-06";
		}else if(7==month||8==month||9==month){
			/**��������*/
			dateArr[0] = year+"-07";
			dateArr[1] = year+"-08";
			dateArr[2] = year+"-09";
		}else{
			/**���ļ���*/
			dateArr[0] = year+"-10";
			dateArr[1] = year+"-11";
			dateArr[2] = year+"-12";
		}
	}
	
	/**���ݵ�ǰ��ݲ�ѯ*/
	private void getYears(){
		Calendar calendar=Calendar.getInstance();
		//��õ�ǰʱ����·ݣ��·ݴ�0��ʼ���Խ��Ҫ��1
		int year = calendar.get(Calendar.YEAR);
		dateArr = new String[1];
		dateArr[0] = year+"";
	}
	
	/**��ʼ��ͼ�����*/
	private void getEchartsTitle(){
		String msgOne = "";
		String msgTwo = "";
		if("1".equals(dateState)){
			msgOne = "���¶�";
		}else if("2".equals(dateState)){
			msgOne = "������";
		}else if("3".equals(dateState)){
			msgOne = "�����";
		}
		
		if("1".equals(typeState)){
			msgTwo = "�ල����";
		}else if("2".equals(typeState)){
			msgTwo = "�ල������";
		}else if("3".equals(typeState)){
			msgTwo = "��������";
		}else if("4".equals(typeState)){
			msgTwo = "�Ƿ���ҽ��";
		}else{
			msgTwo = "Э��Ѳ����";
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
