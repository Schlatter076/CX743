package loyer.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ProductNumTools {

  private ProductNumTools() {}
  
  /**
   * 校验该编号是否已测试通过
   * @param tableName
   * @param productNum
   * @return
   */
  public static boolean isTested(String tableName, String productNum) {
    String sql = "select num from " + tableName + "_product_num where num='" + productNum + "'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      if(rs.next()) {
        return true;
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
    }
    return false;
  }
  /**
   * 向表中插入一条数据
   * @param tableName
   * @param productNum
   * @return
   */
  public static int insert(String tableName, String productNum) {
    
    String sql = "insert into " + tableName + "_product_num values('" + productNum + "')";
    int back =  DBHelper.AddU(sql, null);
    DBHelper.close();
    return back;
  }
  public static List<String> getAllByDB(String tableName) {
    List<String> list = new ArrayList<>();
    String sql = "select num from " + tableName + "_product_num";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        list.add(rs.getString(1));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "产品编号加载失败：" + e.getLocalizedMessage());
    }
    return list;
  }
  /**
   * 导出产品编号到本地TXT
   * @param tableName
   */
  public static void outTxt(String tableName) {
    try {
      String path = "excl/";
      File pathFile = new File(path);
      if(!pathFile.isDirectory()) {
        pathFile.mkdirs();
      }
      //创建可写入的Excel工作簿
      String fileName = tableName + "产品编号" +".txt";
      File file = new File(pathFile, fileName);
      if(!file.exists()) {
        file.createNewFile();
      }
      //查询数据库中所有的数据
      List<String> list = getAllByDB(tableName);
      PrintWriter out = new PrintWriter(new FileOutputStream(file), true);
      for(String str : list) {
        out.println(str);
      }
      out.close();
    } catch(Exception e) {
      JOptionPane.showMessageDialog(null, "TXT写入失败:" + e.getLocalizedMessage());
    }
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
      String fileName = tableName + "产品编号" +".xls";
      File file = new File(pathFile, fileName);
      if(!file.exists()) {
        file.createNewFile();
      }
      //以fileName为文件名来创建一个Workbook
      wwb = Workbook.createWorkbook(file);
      
      //创建工作表
      WritableSheet ws = wwb.createSheet("测试数据表", 0);
      
      //查询数据库中所有的数据
      List<String> list = getAllByDB(tableName);
      //要插入到的excl表格的行号，默认从0开始
      Label label1 = new Label(0, 0, "产品编号");
      
      ws.addCell(label1);
      
      for(int i = 0; i < list.size(); i++) {
        Label label1_ = new Label(0, i+1, list.get(i));
        ws.addCell(label1_);
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
}
