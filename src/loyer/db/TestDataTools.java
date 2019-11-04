package loyer.db;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 测试数据记录表
 * @author hw076
 *
 */
public class TestDataTools {

  private TestDataTools() {} //不允许其他类创建本类实例
  /**
   * 获取全部测试数据记录
   * @param tableName
   * @return
   */
  public static List<TestData> getAllByDB(String tableName) {
    List<TestData> list = new ArrayList<>();
    String sql = "select * from " + tableName + " where date='"+LocalDate.now().toString()+"'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        String id = rs.getString(1);
        String number = rs.getString(2);
        String xuhao = rs.getString(3);
        String items = rs.getString(4);
        String upper = rs.getString(5);
        String lower = rs.getString(6);
        String value = rs.getString(7);
        String unit = rs.getString(8);
        String result = rs.getString(9);
        String times = rs.getString(10);
        String date = rs.getString(11);
        String remark = rs.getString(12);
        
        list.add(new TestData(id, number, xuhao, items, upper, lower, value, unit, result, times, date, remark));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试数据记录加载失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return list;
  }
  /**
   * 获取对应id的步骤名
   * @param tableName
   * @param id
   * @return
   */
  public static String getItemNumById(String tableName, String id) {
    String sql = "select num from "+tableName+" where id='"+id+"'";
    ResultSet rs = DBHelper.search(sql, null);
    String str = "";
    try {
      if(rs.next()) {
        str = rs.getString(1);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试数据记录加载失败:" + e.getLocalizedMessage());
    } 
    DBHelper.close();
    return str;
  }
  /**
   * 获取对应id的产品编号
   * @param tableName
   * @param id
   * @return
   */
  public static String getProductNumById(String tableName, String id) {
    String sql = "select product_num from "+tableName+" where id='"+id+"'";
    ResultSet rs = DBHelper.search(sql, null);
    String str = "";
    try {
      if(rs.next()) {
        
        str = rs.getString(1);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试数据记录加载失败:" + e.getLocalizedMessage());
    } 
    DBHelper.close();
    return str;
  }
  /**
   * 获取测试记录id
   * @param tableName
   * @return
   */
  public static List<String> getIds(String tableName) {
    List<String> set = new ArrayList<>();
    String sql = "select id,product_num from " + tableName + " where date='"+LocalDate.now().toString()+"'";
    //String sql = "select id,product_num from " + tableName + " where date='2019-04-24'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        if(!rs.getString(2).equals("--")) {
          set.add(rs.getString(1));
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试数据记录加载失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return set;
  }
  /**
   * 根据id获取测试数据
   * @param id
   * @param tableName
   * @return
   */
  public static TestData getById(String id, String tableName) {
    TestData td = null;
    String sql = "select * from " + tableName + " where id='"+id+"'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        
        String number = rs.getString(2);
        String xuhao = rs.getString(3);
        String items = rs.getString(4);
        String upper = rs.getString(5);
        String lower = rs.getString(6);
        String value = rs.getString(7);
        String unit = rs.getString(8);
        String result = rs.getString(9);
        String times = rs.getString(10);
        String date = rs.getString(11);
        String remark = rs.getString(12);
        
        td = new TestData(id, number, xuhao, items, upper, lower, value, unit, result, times, date, remark);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试数据记录加载失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return td;
  }
  
  /**
   * 向表中插入一条数据
   * @param tableName
   * @param datas 一行数据
   * @return
   */
  public static int insert(String tableName, String[] datas) {
    if(datas == null || datas.length != 11) {
      JOptionPane.showMessageDialog(null, "测试数据格式不正确，请核对后重试！");
      return -1;
    }
    String sql = "insert into "+tableName+" (product_num,num,items,upper,lower,value,unit,result,times,date,remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    int back = DBHelper.AddU(sql, datas);
    DBHelper.close();
    return back;
  }
  /**
   * 将测试数据记录导出到本地
   */
  public static void outExcl(String tableName) {
    WritableWorkbook wwb = null;
    try {
      String path = "excl/";
      File pathFile = new File(path);
      if(!pathFile.isDirectory()) {
        pathFile.mkdirs();
      }
      //创建可写入的Excel工作簿
      String fileName = tableName + "测试数据" + LocalDate.now()+".xls";
      File file = new File(pathFile, fileName);
      if(!file.exists()) {
        file.createNewFile();
      }
      //以fileName为文件名来创建一个Workbook
      wwb = Workbook.createWorkbook(file);
      
      //创建工作表
      WritableSheet ws = wwb.createSheet("测试数据表", 0);
      
      //查询数据库中所有的数据
      List<TestData> list = getAllByDB(tableName);
      //要插入到的excl表格的行号，默认从0开始
      Label label1 = new Label(0, 0, "产品编号");
      Label label2 = new Label(1, 0, "测试步骤");
      Label label3 = new Label(2, 0, "测试内容");
      Label label4 = new Label(3, 0, "上限值");
      Label label5 = new Label(4, 0, "下限值");
      Label label6 = new Label(5, 0, "实测值");
      Label label7 = new Label(6, 0, "单位");
      Label label8 = new Label(7, 0, "结果判定");
      Label label9 = new Label(8, 0, "修改时间");
      Label label10 = new Label(9, 0, "日期");
      Label label11 = new Label(10, 0, "备注");
      ws.addCell(label1);
      ws.addCell(label2);
      ws.addCell(label3);
      ws.addCell(label4);
      ws.addCell(label5);
      ws.addCell(label6);
      ws.addCell(label7);
      ws.addCell(label8);
      ws.addCell(label9);
      ws.addCell(label10);
      ws.addCell(label11);
      for(int i = 0; i < list.size(); i++) {
        Label label1_ = new Label(0, i+1, list.get(i).getNumber());
        Label label2_ = new Label(1, i+1, list.get(i).getXuhao());
        Label label3_ = new Label(2, i+1, list.get(i).getItems());
        Label label4_ = new Label(3, i+1, list.get(i).getUpper());
        Label label5_ = new Label(4, i+1, list.get(i).getLower());
        Label label6_ = new Label(5, i+1, list.get(i).getTestValue());
        Label label7_ = new Label(6, i+1, list.get(i).getUnit());
        Label label8_ = new Label(7, i+1, list.get(i).getResult());
        Label label9_ = new Label(8, i+1, list.get(i).getTimes());
        Label label10_ = new Label(9, i+1, list.get(i).getDate());
        Label label11_ = new Label(10, i+1, list.get(i).getRemark());
        
        ws.addCell(label1_);
        ws.addCell(label2_);
        ws.addCell(label3_);
        ws.addCell(label4_);
        ws.addCell(label5_);
        ws.addCell(label6_);
        ws.addCell(label7_);
        ws.addCell(label8_); 
        ws.addCell(label9_);
        ws.addCell(label10_);
        ws.addCell(label11_); 
        
      }
      //写进文档
      wwb.write();
      
    } catch(Exception e) {
      JOptionPane.showMessageDialog(null, "excl写入失败:" + e.getLocalizedMessage());
    } finally {
      //关闭Excel工作簿对象
      try {
        wwb.close();
      } catch (WriteException | IOException e) {
        JOptionPane.showMessageDialog(null, "excl导出失败:" + e.getLocalizedMessage());
      }
    }
  }
  /**
   * 将测试数据对比导出到本地
   */
  public static void outCompExcl(String tableName) {
    WritableWorkbook wwb = null;
    try {
      String path = "excl/";
      File pathFile = new File(path);
      if (!pathFile.isDirectory()) {
        pathFile.mkdirs();
      }
      // 创建可写入的Excel工作簿
      String fileName = tableName + "数据比对" + LocalDate.now() + ".xls";
      File file = new File(pathFile, fileName);
      if (!file.exists()) {
        file.createNewFile();
      }
      // 以fileName为文件名来创建一个Workbook
      wwb = Workbook.createWorkbook(file);

      // 创建工作表
      WritableSheet ws = wwb.createSheet("测试数据表", 0);

      List<String> ids = getIds(tableName);

      // 要插入到的excl表格的行号，默认从0开始
      Label label1 = new Label(0, 0, "产品编号");
      Label label2 = new Label(1, 0, "Home按压力");
      Label label3 = new Label(2, 0, "Home行程");
      Label label4 = new Label(3, 0, "Back按压力");
      Label label5 = new Label(4, 0, "Back行程");
      Label label6 = new Label(5, 0, "Audio按压力");
      Label label7 = new Label(6, 0, "Audio行程");
      Label label8 = new Label(7, 0, "Navi按压力");
      Label label9 = new Label(8, 0, "Navi行程");
      Label label10 = new Label(9, 0, "Power按压力");
      Label label11 = new Label(10, 0, "Power行程");
      Label label12 = new Label(11, 0, "PowerUp按压力");
      Label label13 = new Label(12, 0, "PowerUp行程");
      Label label14 = new Label(13, 0, "PowerDown按压力");
      Label label15 = new Label(14, 0, "PowerDown行程");
      Label label16 = new Label(15, 0, "PowerLeft按压力");
      Label label17 = new Label(16, 0, "PowerLeft行程");
      Label label18 = new Label(17, 0, "PowerRight按压力");
      Label label19 = new Label(18, 0, "PowerRight行程");

      Label label21 = new Label(19, 0, "修改时间");
      Label label22 = new Label(20, 0, "日期");
      ws.addCell(label1);
      ws.addCell(label2);
      ws.addCell(label3);
      ws.addCell(label4);
      ws.addCell(label5);
      ws.addCell(label6);
      ws.addCell(label7);
      ws.addCell(label8);
      ws.addCell(label9);
      ws.addCell(label10);
      ws.addCell(label11);
      ws.addCell(label12);
      ws.addCell(label13);
      ws.addCell(label14);
      ws.addCell(label15);
      ws.addCell(label16);
      ws.addCell(label17);
      ws.addCell(label18);
      ws.addCell(label19);
      ws.addCell(label21);
      ws.addCell(label22);

      int count = 0;
        
      for (String id : ids) {
        
        String item = getItemNumById(tableName, id);
        TestData td = getById(id, tableName);
        
        int key = Integer.parseInt(item);
        switch (key) {
        case 1:
          ws.addCell(new Label(0, ++count, getProductNumById(tableName, id)));
          ws.addCell(new Label(19, count, td.getTimes()));
          ws.addCell(new Label(20, count, td.getDate()));
          break;
          
        case 2:
          ws.addCell(new Label(1, count, td.getTestValue()));
          break;
        case 3:
          ws.addCell(new Label(2, count, td.getTestValue()));
          break;
        case 5:
          ws.addCell(new Label(3, count, td.getTestValue()));
          break;
        case 6:
          ws.addCell(new Label(4, count, td.getTestValue()));
          break;
        case 8:
          ws.addCell(new Label(5, count, td.getTestValue()));
          break;
        case 9:
          ws.addCell(new Label(6, count, td.getTestValue()));
          break;
        case 11:
          ws.addCell(new Label(7, count, td.getTestValue()));
          break;
        case 12:
          ws.addCell(new Label(8, count, td.getTestValue()));
          break;
        case 14:
          ws.addCell(new Label(9, count, td.getTestValue()));
          break;
        case 15:
          ws.addCell(new Label(10, count, td.getTestValue()));
          break;
        case 17:
          ws.addCell(new Label(11, count, td.getTestValue()));
          break;
        case 18:
          ws.addCell(new Label(12, count, td.getTestValue()));
          break;
        case 20:
          ws.addCell(new Label(13, count, td.getTestValue()));
          break;
        case 21:
          ws.addCell(new Label(14, count, td.getTestValue()));
          break;
        case 23:
          ws.addCell(new Label(15, count, td.getTestValue()));
          break;
        case 24:
          ws.addCell(new Label(16, count, td.getTestValue()));
          break;
        case 26:
          ws.addCell(new Label(17, count, td.getTestValue()));
          break;
        case 27:
          ws.addCell(new Label(18, count, td.getTestValue()));
          break;
        default:
          break;
        }
      }//*/

      // 写进文档
      wwb.write();

    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "excl写入失败:" + e.getLocalizedMessage());
    } finally {
      // 关闭Excel工作簿对象
      try {
        wwb.close();
      } catch (WriteException | IOException e) {
        JOptionPane.showMessageDialog(null, "excl导出失败:" + e.getLocalizedMessage());
      }
    }
  }
  /**
   * 表的实体
   * @author hw076
   *
   */
  public static class TestData {
    
    private String id;
    private String number;
    private String xuhao;
    private String items;
    private String upper;
    private String lower;
    private String testValue;
    private String unit;
    private String result;
    private String times;
    private String date;
    private String remark;
    
    public TestData() {
      super();
    }
    
    public TestData(String id, String number, String xuhao, String items, String upper, String lower, String testValue,
        String unit, String result, String times, String date, String remark) {
      super();
      this.id = id;
      this.number = number;
      this.xuhao = xuhao;
      this.items = items;
      this.upper = upper;
      this.lower = lower;
      this.testValue = testValue;
      this.unit = unit;
      this.result = result;
      this.times = times;
      this.date = date;
      this.remark = remark;
    }

    public String getNumber() {
      return number;
    }
    public void setNumber(String number) {
      this.number = number;
    }
    public String getXuhao() {
      return xuhao;
    }
    public void setXuhao(String xuhao) {
      this.xuhao = xuhao;
    }
    
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getItems() {
      return items;
    }
    public void setItems(String items) {
      this.items = items;
    }
    public String getUpper() {
      return upper;
    }
    public void setUpper(String upper) {
      this.upper = upper;
    }
    public String getLower() {
      return lower;
    }
    public void setLower(String lower) {
      this.lower = lower;
    }
    public String getTestValue() {
      return testValue;
    }
    public void setTestValue(String testValue) {
      this.testValue = testValue;
    }
    public String getUnit() {
      return unit;
    }
    public void setUnit(String unit) {
      this.unit = unit;
    }
    public String getResult() {
      return result;
    }
    public void setResult(String result) {
      this.result = result;
    }

    public String getTimes() {
      return times;
    }

    public void setTimes(String times) {
      this.times = times;
    }

    public String getDate() {
      return date;
    }

    public void setDate(String date) {
      this.date = date;
    }

    public String getRemark() {
      return remark;
    }

    public void setRemark(String remark) {
      this.remark = remark;
    }   
    
  }
}
