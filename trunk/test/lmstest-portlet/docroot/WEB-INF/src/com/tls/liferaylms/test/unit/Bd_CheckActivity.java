package com.tls.liferaylms.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.tls.liferaylms.test.SeleniumTestCase;
import com.tls.liferaylms.test.util.CheckPage;
import com.tls.liferaylms.test.util.Context;
import com.tls.liferaylms.test.util.CourseActivityMenu;
import com.tls.liferaylms.test.util.GetPage;
import com.tls.liferaylms.test.util.InstancePortlet;
import com.tls.liferaylms.test.util.Login;
import com.tls.liferaylms.test.util.Sleep;
import com.tls.liferaylms.test.util.TestProperties;


public class Bd_CheckActivity extends SeleniumTestCase {
	  String test = TestProperties.get("act.test");
	  String ext = TestProperties.get("act.ext");
	  String p2p = TestProperties.get("act.p2p");
	  String enc = TestProperties.get("act.enc");
	  String pres = TestProperties.get("act.pres");
	  String scorm = TestProperties.get("act.scorm");
	  String desa = TestProperties.get("act.desa");

	  @Test
	  public void checkActivities() throws Exception {
		  if(getLog().isInfoEnabled())getLog().info("init");
		  GetPage.getPage(driver, "", Context.getTestPage());

		  Login login = new Login(driver, Context.getStudentUser2(), Context.getStudentPass2(), Context.getBaseUrl());

		  if(login.isLogin())
			  login.logout();

		  boolean student2Login = login.login();
		  assertTrue("Error not logued Student2",student2Login);

		  if(student2Login){
			  fillBadActivities();
		  }
		  
		  
		  login = new Login(driver, Context.getStudentUser(), Context.getStudentPass(), Context.getBaseUrl());

		  if(login.isLogin())
			  login.logout();
			
		  boolean studentLogin = login.login();
		  assertTrue("Error not logued",studentLogin);

		  if(studentLogin){
			  fillActivities();
		  }
		  
		  login = new Login(driver, Context.getTeacherUser(), Context.getTeacherPass(), Context.getBaseUrl());

		  if(login.isLogin())
			  login.logout();

		  boolean teacherLogin = login.login();
		  
		  if(teacherLogin){
			  teacherActivities();
		  }

		  login = new Login(driver, Context.getUser(), Context.getPass(), Context.getBaseUrl());

		  if(login.isLogin())
			  login.logout();

		  boolean testLogin = login.login();

		  if(testLogin){
			  testActivities();
		  }

		  login = new Login(driver, Context.getStudentUser(), Context.getStudentPass(), Context.getBaseUrl());

		  if(login.isLogin())
			  login.logout();
		  
		  //boolean studentLogin = login.login();
		  studentLogin = login.login();

		  if(testLogin){
			  fillP2PActivity();
		  }

		  if(getLog().isInfoEnabled())getLog().info("end!");
	  }

	  private void fillP2PActivity(){
		  try{
			  GetPage.getPage(driver, Context.getCoursePage(),"/reto");
			  
			  for(String id : Context.getActivities().keySet()){
				  if(getLog().isInfoEnabled())getLog().info("Check activity::"+id);
				  
				  WebElement a = CourseActivityMenu.findElementActivityMenuTotal(driver,id);
				  assertNotNull("Not activity found:"+id, a);

				  if(getLog().isInfoEnabled())getLog().info("Click!");
				  //double click
				  a.click();
				  try{
					  a.click();
				  }catch(Exception e){}

				  assertEquals("Errors in activity "+id, 0, CheckPage.check(driver));

				  if(id.length()>p2p.length()&&id.substring(0, p2p.length()).equals(p2p)){
					  Sleep.sleep(2000000);
				  }
			  }
			  
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	  }

	  private void testActivities(){
		  try{
			  GetPage.getPage(driver, Context.getTestPage(),"");
			  			  
			  InstancePortlet.createInstance(driver, "Administraci\u00f3n de portal", "portlet_portaladmin_WAR_liferaylmsportlet");
			  
			  WebElement portaladmin = getElement(By.id("portlet_portaladmin_WAR_liferaylmsportlet"));
			  assertNotNull("Not portaladmin portlet found:", portaladmin);
			  WebElement actions = getElement(portaladmin,By.className("actions"));
			  assertNotNull("Not actions portaladmin portlet found:", actions);
			  WebElement p2pAct = getElement(actions,By.className("taglib-icon"));
			  p2pAct.click();
			  
			  Sleep.sleep(2000);
			  
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	  }
	  
	  private void teacherActivities(){

		  try{
			  GetPage.getPage(driver, Context.getCoursePage(),"/reto");

			  for(String id : Context.getActivities().keySet()){
				  if(getLog().isInfoEnabled())getLog().info("Check activity::"+id);
				  WebElement a = CourseActivityMenu.findElementActivityMenuTotal(driver,id);
				  assertNotNull("Not activity found:"+id, a);

				  if(getLog().isInfoEnabled())getLog().info("Click!");
				  //double click
				  a.click();
				  try{
					  a.click();
				  }catch(Exception e){}

				  assertEquals("Errors in activity "+id, 0, CheckPage.check(driver));
				  
				  if((id.length()>pres.length()&&id.substring(0, pres.length()).equals(pres))||(id.length()>desa.length()&&id.substring(0, desa.length()).equals(desa))){
					  WebElement container = getElement(By.id("_offlinetaskactivity_WAR_liferaylmsportlet_usersSearchContainerSearchContainer"));
					  if(container==null)
						  container = getElement(By.id("_onlinetaskactivity_WAR_liferaylmsportlet_usersSearchContainerSearchContainer"));
					  
					  assertNotNull("Not container for table of Desa activities", container);
					  List<WebElement> cols2 = getElements(container,By.className("col-2"));
					  assertNotNull("Not col in table found", cols2);
					  
					  for(int i =0;i<cols2.size();i++){
						  if(i==0)
							  continue;
						  
						  if(getLog().isInfoEnabled())getLog().info("col2:"+cols2.get(i).getText());
						  WebElement al = getElement(cols2.get(i),By.tagName("a"));
						  assertNotNull("Not link for check activity", al);
						  al.click();
						  
						  Sleep.sleep(2000);
						  
						  //WebElement showppg = getElement(By.id("_offlinetaskactivity_WAR_liferaylmsportlet_showPopupGrades"));
						  WebElement showppg = getElement(By.id("_offlinetaskactivity_WAR_liferaylmsportlet_result"));
						  if(showppg==null)
							  showppg = getElement(By.id("_onlinetaskactivity_WAR_liferaylmsportlet_result"));
						  
						  assertNotNull("Not found note textarea", showppg);
						  showppg.clear();
						  showppg.sendKeys("100");
						  
						  showppg = getElement(By.id("_offlinetaskactivity_WAR_liferaylmsportlet_comments"));
						  if(showppg==null)
							  showppg = getElement(By.id("_onlinetaskactivity_WAR_liferaylmsportlet_comments"));
						  
						  assertNotNull("Not found comments for note textarea", showppg);
						  showppg.clear();
						  showppg.sendKeys(TestProperties.get("act.test.3.answer"));
						  
						  try{
							  ((JavascriptExecutor)driver).executeScript("_offlinetaskactivity_WAR_liferaylmsportlet_doSaveGrades()");
						  }catch(Exception e){
							  ((JavascriptExecutor)driver).executeScript("_onlinetaskactivity_WAR_liferaylmsportlet_doSaveGrades()");
						  }

						  Sleep.sleep(2000);
						  
						  WebElement close = getElement(By.id("closethick"));
						  close.click();

						  Sleep.sleep(2000);
						  
						  //reload DOM
						  container = getElement(By.id("_offlinetaskactivity_WAR_liferaylmsportlet_usersSearchContainerSearchContainer"));
						  if(container==null)
							  container = getElement(By.id("_onlinetaskactivity_WAR_liferaylmsportlet_usersSearchContainerSearchContainer"));
						  
						  assertNotNull("Not container for table of Desa activities", a);
						  cols2 = getElements(container,By.className("col-2"));
						  
					  }				  					  
					  
					  //SCORM
				  }else if(id.length()>scorm.length()&&id.substring(0, scorm.length()).equals(scorm)){
					  checkAndCloseScorm();
				  }
			  }
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	  }
	  

	  private boolean fillBadActivities(){
		  try{
			  GetPage.getPage(driver, Context.getCoursePage(),"/reto");

			  for(String id : Context.getActivities().keySet()){
				  if(getLog().isInfoEnabled())getLog().info("Check activity::"+id);
				  WebElement a = CourseActivityMenu.findElementActivityMenuTotal(driver,id);
				  assertNotNull("Not activity found:"+id, a);

				  if(getLog().isInfoEnabled())getLog().info("Click!");
				  //double click
				  a.click();
				  try{
					  a.click();
				  }catch(Exception e){}

				  assertEquals("Errors in activity "+id, 0, CheckPage.check(driver));

				  	  //Test
				  if(id.length()>test.length()&&id.substring(0, test.length()).equals(test)){
					  WebElement submit = driver.findElement(By.className("aui-button-input-submit"));
					  submit.click();
					  
					  Sleep.sleep(1000);
					  
					  WebElement popup = getElement(By.className("aui-dialog-content"));
					  assertNotNull("Not found popup container", popup);
					  WebElement submitIframe = getElement(popup,By.className("aui-buttonitem-label-only"));
					  assertNotNull("Not submit found in popup", submitIframe);
					  submitIframe.click();
					  
					  Sleep.sleep(2000);
					  
					  WebElement portletContainer = getElement(By.id("p_p_id_execactivity_WAR_liferaylmsportlet_"));
					  assertNotNull("Not found portlet container", portletContainer);
					  
					  List<WebElement> pes = getElements(portletContainer,By.tagName("p"));
					  
					  String percent = pes.get(1).getText();
					  
					  assertNotEquals("Check test note fail", "100%", percent.substring(percent.length()-4,percent.length()));
					  assertEquals("Check test note fail", "0%", percent.substring(percent.length()-2,percent.length()));

					  //Scorm
				  }else if(id.length()>scorm.length()&&id.substring(0, scorm.length()).equals(scorm)){
					  checkAndCloseScorm();
					  //Enc
				  }else if(id.length()>enc.length()&&id.substring(0, enc.length()).equals(enc)){
					  checkEnc();
					  
					  //P2P
				  }else if(id.length()>p2p.length()&&id.substring(0, p2p.length()).equals(p2p)){
					  WebElement capa1 = getElement(By.id("capa1"));
					  WebElement ta = getElement(capa1,By.tagName("textarea"));
					  ta.clear();
					  ta.sendKeys(TestProperties.get("act.p2p.text"));

					  WebElement fileName = getElement(By.id("_p2ptaskactivity_WAR_liferaylmsportlet_fileName"));
					  File f = new File("docroot"+File.separator+"WEB-INF"+File.separator+"classes"+File.separator+"resources"+File.separator+"encuesta.csv");
					  fileName.sendKeys(f.getAbsolutePath());
					  
					  WebElement cb = getElement(capa1,By.className("container-buttons"));
					  WebElement input = getElement(cb,By.tagName("input"));
					  
					  input.click();
					  try{
						  input.click();
					  }catch(Exception e){}
					  
					  Sleep.sleep(1000);
					  
					  WebElement but = getElement(By.id("buttonSendP2P"));
					  assertNotNull("Not found Button for submit p2p", but);
					  but.click();
					  try{
						  input.click();
					  }catch(Exception e){}
					  
					  //Desa
				  }else if(id.length()>desa.length()&&id.substring(0, desa.length()).equals(desa)){
					  fillDesa();
				  }
				  
			  }
		  }catch(Exception e){
			  e.printStackTrace();
			  return false;
		  }
		  
		  return true;
	  }
	  
	  private boolean fillActivities(){
		  try{
			  GetPage.getPage(driver, Context.getCoursePage(),"/reto");
			  
			  for(String id : Context.getActivities().keySet()){
				  if(getLog().isInfoEnabled())getLog().info("Check activity::"+id);

				  WebElement a = CourseActivityMenu.findElementActivityMenuTotal(driver,id);
				  assertNotNull("Not activity found:"+id, a);

				  if(getLog().isInfoEnabled())getLog().info("Click!");
				  //double click
				  a.click();
				  try{
					  a.click();
				  }catch(Exception e){}
				  
				  assertEquals("Errors in activity "+id, 0, CheckPage.check(driver));
				  
				  //Test
				  if(id.length()>test.length()&&id.substring(0, test.length()).equals(test)){
					  if(getLog().isInfoEnabled())getLog().info("Test activity");
					  WebElement we = getElement(By.className("questiontype_1"));
					  assertNotNull("Not found Multiple Question container", we);
					  List<WebElement> options = getElements(we,By.className("answer"));
					  assertNotNull("Not found options", options);
					  WebElement input = getElement(options.get(0),By.tagName("input"));
					  assertNotNull("Not correct input", input);
					  input.click();

					  we = getElement(By.className("questiontype_0"));
					  assertNotNull("Not found Option Question container", we);
					  options = getElements(we,By.className("answer"));
					  assertNotNull("Not found options", options);
					  input = getElement(options.get(0),By.tagName("input"));
					  assertNotNull("Not correct input", input);
					  input.click();

					  we = getElement(By.className("questiontype_3"));
					  assertNotNull("Not found Option Question container", we);
					  WebElement ge = getElement(we,By.className("answer-fillblank"));
					  assertNotNull("Not found Father for input fillblank", ge);
					  input = getElement(ge,By.tagName("input"));
					  input.clear();
					  input.sendKeys(TestProperties.get("act.test.3.answer"));

					  we = getElement(By.className("questiontype_2"));
					  assertNotNull("Not found Option Question container", we);
					  WebElement textarea = getElement(we,By.tagName("textarea"));
					  textarea.sendKeys(TestProperties.get("act.test.3.answer"));

					  we = getElement(By.className("ui-droppable"));
					  assertNotNull("Not found Dropable container", we);
					  //first element
					  WebElement divDropable = null;
					  List<WebElement> divsDropable = getElements(we,By.tagName("div"));
					  
					  for(WebElement div: divsDropable){
						  
						  String[] etext = div.getText().split(" ");
						  if(etext.length>2&&etext[1].equals("0")){
							 divDropable = div;
							 break;
						  }
					  }
					  
					  assertNotNull("Not found Dropable option", divDropable);
					  
					  ((JavascriptExecutor)driver).executeScript("javascript:document.getElementsByClassName(\"drop\")[0].getElementsByTagName(\"input\")[0].value = '"+divDropable.getAttribute("id")+"';");

					  we = getElement(By.className("questiontype_sortable"));
					  assertNotNull("Not found Question sortable container", we);
					  
					  List<WebElement> inputSort = getElements(we, By.tagName("input"));
					  assertNotNull("Not found input for sort destination", inputSort);
					  assertEquals("Not found correct inputs number for sort",2, inputSort.size());
					  
					  String value = inputSort.get(1).getAttribute("value");
					  String[] values = value.split("&");
					  
					  StringBuilder result = new StringBuilder();
					  int sum = 0;
					  for(String valuet:values){
						  String[] ident =valuet.split("=");
						  assertEquals("Not found correct parts in sort input",2, ident.length);
						  result.append(ident[0]);
						  result.append("=");
						  
						  WebElement sorts = getElement(By.className("question_sortable"));
						  List<WebElement> lis = getElements(sorts, By.tagName("li"));
						  boolean find = false;
						  while(!find){
							  for(WebElement li : lis){
								  String text = li.getText();
								  String[] te = text.split(" ");
								  if(getLog().isInfoEnabled())getLog().info("text!"+li.getText());
								  if(te.length==3){
									  String num = ident[0].substring(ident[0].length()-2, ident[0].length()-1);
									  Integer onum = Integer.valueOf(num)+sum;
									  if(te[1].equals(String.valueOf(onum))){
										  result.append(li.getAttribute("id"));
										  find = true;
										  break;
									  }
								  }
							  }
							  if(!find){
								  sum++;
							  }
						  }
						  result.append("&");
					  }
					  
					  ((JavascriptExecutor)driver).executeScript("javascript:document.getElementsByClassName(\"questiontype_sortable\")[0].getElementsByTagName(\"input\")[1].value = '"+result.toString()+"';");

					  
					  WebElement submit = driver.findElement(By.className("aui-button-input-submit"));
					  submit.click();
					  
					  Sleep.sleep(1000);
					  
					  WebElement portletContainer = getElement(By.id("p_p_id_execactivity_WAR_liferaylmsportlet_"));
					  assertNotNull("Not found portlet container", portletContainer);
					  
					  List<WebElement> pes = getElements(portletContainer,By.tagName("p"));
					  
					  String percent = pes.get(1).getText();
					  
					  assertEquals("Check test note fail", "100%", percent.substring(percent.length()-4,percent.length()));
					  
					  //Recurso externo
				  }else if(id.length()>ext.length()&&id.substring(0, ext.length()).equals(ext)){
					  WebElement video = getElement(By.className("video"));
					  WebElement iframe = getElement(video,By.tagName("iframe"));
					  assertNotNull("No iframe for video", iframe);
					  
					  //P2P
				  }else if(id.length()>p2p.length()&&id.substring(0, p2p.length()).equals(p2p)){
					  WebElement capa1 = getElement(By.id("capa1"));
					  WebElement ta = getElement(capa1,By.tagName("textarea"));
					  ta.clear();
					  ta.sendKeys(TestProperties.get("act.p2p.text"));

					  WebElement fileName = getElement(By.id("_p2ptaskactivity_WAR_liferaylmsportlet_fileName"));
					  File f = new File("docroot"+File.separator+"WEB-INF"+File.separator+"classes"+File.separator+"resources"+File.separator+"encuesta.csv");
					  fileName.sendKeys(f.getAbsolutePath());
					  
					  WebElement cb = getElement(capa1,By.className("container-buttons"));
					  WebElement input = getElement(cb,By.tagName("input"));
					  
					  input.click();
					  try{
						  input.click();
					  }catch(Exception e){}
					  
					  Sleep.sleep(1000);

					  //driver.switchTo().frame(0);
					  
					  WebElement but = getElement(By.id("buttonSendP2P"));
					  assertNotNull("Not found Button for submit p2p", but);
					  but.click();
					  try{
						  input.click();
					  }catch(Exception e){}

					  //driver.switchTo().activeElement();
					  
					  //SCORM scorm
				  }else if(id.length()>scorm.length()&&id.substring(0, scorm.length()).equals(scorm)){
					  checkAndCloseScorm();
					  //Encuesta
				  }else if(id.length()>enc.length()&&id.substring(0, enc.length()).equals(enc)){
					  checkEnc();
					  //Desa
				  }else if(id.length()>desa.length()&&id.substring(0, desa.length()).equals(desa)){
					  fillDesa();
				  }
			  }
		  }catch(Exception e){
			  e.printStackTrace();
			  return false;
		  }
		  
		  return true;
	  }
	  
	  
	  private String readFile(File file){
		  BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			if(br==null)
				return null;
		    try {
		        StringBuilder sb = new StringBuilder();
		        String line = null;
				try {
					line = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}

		        while (line != null) {
		            sb.append(line);
		            sb.append('\n');
		            try {
						line = br.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		        return sb.toString();
		    } finally {
		        try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
	  }
	  
	  private boolean checkAndCloseScorm(){
		  Set<String> windows = driver.getWindowHandles();
		  assertEquals("SCORM Activity not found", 2, windows.size());
		  if(getLog().isInfoEnabled())getLog().info("do!"+windows.size());
		  
		  boolean present = false;
		  for(String window:windows){
			  driver.switchTo().window(window);
			  if (driver.getTitle().contains("SCORM Activity")) {
				  try{
					  Alert confirm =driver.switchTo().alert();
					  confirm.accept();
				  }catch(Exception e){}
				  present = true;
				  driver.close();
			  }
		  }
		  assertEquals("SCORM Activity not found", true, present);
		  
		  windows = driver.getWindowHandles();
		  assertEquals("SCORM Activity not close", 1, windows.size());

		  for(String window:windows){
			  driver.switchTo().window(window);
		  }
		  
		  WebElement webElement = getElement(By.className("description-title"));
		  String title = webElement.getText();
		  
		  assertEquals("Not correct Window", true,title.length()>scorm.length()&&title.substring(0, scorm.length()).equals(scorm));
		  
		  return true;
	  }
	  
	  private void checkEnc(){
		  List<WebElement> questions = getElements(By.className("question"));
		  
		  File f = new File("docroot"+File.separator+"WEB-INF"+File.separator+"classes"+File.separator+"resources"+File.separator+"encuesta.csv");
		  String text = readFile(f);
		  StringBuilder sb = new StringBuilder();
		  for(WebElement question : questions){
			  List<WebElement> answers = getElements(question,By.className("answer"));
			  WebElement questiontext = getElement(question,By.className("questiontext"));
			  sb.append(questiontext.getText());
			  sb.append(";");
			  for(int i=0;i<answers.size();i++){
				  sb.append(answers.get(i).getText());
				  if(i+1!=answers.size())
					  sb.append(",");
			  }
			  sb.append("\n");
		  }
		  if(getLog().isInfoEnabled())getLog().info("Una!"+text);
		  if(getLog().isInfoEnabled())getLog().info("Otra!"+sb.toString());
		  
		  assertEquals("CSV not match whit questions",text, sb.toString());
		  

		  for(WebElement question : questions){
			  WebElement answer = getElement(question,By.className("answer"));
			  WebElement input = getElement(answer,By.tagName("input"));
			  input.click();
		  }
		  
		  WebElement submit = driver.findElement(By.className("aui-button-input-submit"));
		  
		  submit.click();
		  try{
			  submit.click();
		  }catch(Exception e){}
	  }
	  
	  private void fillDesa(){
		  WebElement text = getElement(By.id("_onlinetaskactivity_WAR_liferaylmsportlet_text"));
		  text.clear();
		  text.sendKeys(TestProperties.get("act.test.3.answer"));

		  WebElement submit = driver.findElement(By.className("aui-button-input-submit"));
		  submit.click();
	  }
}
