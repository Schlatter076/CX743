package loyer.db;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 测试记录工具类
 * @author hw076
 *
 */
public class RecordTools {

  private RecordTools() {} //不允许其他类创建本类实例
  /**
   * 获取全部测试记录
   * @param tableName 表名
   * @return
   */
  public static List<RecordData> getAllByDB(String tableName) {
    List<RecordData> list = new ArrayList<>();
    String sql = "select * from " + tableName;
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        String name = rs.getString(1);
        String sum = rs.getString(2);
        String ok = rs.getString(3);
        String ng = rs.getString(4);
        String times = rs.getString(5);
        String date = rs.getString(6);
        
        list.add(new RecordData(name, sum, ok, ng, times, date));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试记录读取失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return list;
  }
  /**
   * 通过指定日期查询测试记录
   * @param tableName
   * @param date
   * @return
   */
  public static RecordData getByDate(String tableName, String date) {
    RecordData data = null;
    String sql = "select * from " + tableName + " where recordtime='"+date+"'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      if(rs.next()) {
        String name = rs.getString(1);
        String sum = rs.getString(2);
        String ok = rs.getString(3);
        String ng = rs.getString(4);
        String times = rs.getString(5);
        
        data = new RecordData(name, sum, ok, ng, times, date);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试记录读取失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return data;
  }
  /**
   * 测试记录写进数据库
   * @param tableName
   * @param datas
   * @return
   */
  public static int insertRecord(String tableName, String[] datas) {
    int back = 0;
    String sql = "insert into " + tableName + " values(?, ?, ?, ?, ?, ?)";
    if(datas == null || datas.length != 6) {
      JOptionPane.showMessageDialog(null, "数据格式不对，请检查后重新操作！");
      return -1;
    }
    back = DBHelper.AddU(sql, datas);
    DBHelper.close();
    return back;
  }
  /**
   * 更新测试记录
   * @param datas
   * @return
   */
  public static int updataRecord(String tableName, String[] datas) {
    int back = 0;
    if(datas == null || datas.length != 6) {
      JOptionPane.showMessageDialog(null, "数据格式不对，请检查后重新操作！");
      return -1;
    }
    String sql = "update "+tableName+" set recordname='"+datas[0]+"', recordsum='"+datas[1]+"', recordok='"+datas[2]+"',"
        + "recordng='"+datas[3]+"', recordts='"+datas[4]+"' where recordtime='"+datas[5]+"'";//*/
    back = DBHelper.AddU(sql, null);
    DBHelper.close();
    return back;
  }
  /**
   * 导出到excel
   * @param tableName
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
      String fileName = tableName + "不良记录.xls";
      File file = new File(pathFile, fileName);
      if(!file.exists()) {
        file.createNewFile();
      }
      //以fileName为文件名来创建一个Workbook
      wwb = Workbook.createWorkbook(file);
      
      //创建工作表
      WritableSheet ws = wwb.createSheet("不良记录表", 0);
      
      //查询数据库中所有的数据
      List<RecordData> list = getAllByDB(tableName);
      //要插入到的excl表格的行号，默认从0开始
      Label labelRecordname = new Label(0, 0, "机种名");
      Label labelRecortimes = new Label(1, 0, "测试总数");
      Label labelTestitem = new Label(2, 0, "良品数");
      Label labelMaxvalue = new Label(3, 0, "不良品数");
      Label labelMinvalue = new Label(4, 0, "测试时长");
      Label labelTestvalue = new Label(5, 0, "日期");
      ws.addCell(labelRecordname);
      ws.addCell(labelRecortimes);
      ws.addCell(labelTestitem);
      ws.addCell(labelMaxvalue);
      ws.addCell(labelMinvalue);
      ws.addCell(labelTestvalue);
      for(int i = 0; i < list.size(); i++) {
        Label labelRecordname_i = new Label(0, i+1, list.get(i).getName());
        Label labelRecordtimes_i = new Label(1, i+1, list.get(i).getSum());
        Label labelTestitem_i = new Label(2, i+1, list.get(i).getOk());
        Label labelMaxvalue_i = new Label(3, i+1, list.get(i).getNg());
        Label labelMinvalue_i = new Label(4, i+1, list.get(i).getTimes());
        Label labelTestvalue_i = new Label(5, i+1, list.get(i).getDate());
        ws.addCell(labelRecordname_i);
        ws.addCell(labelRecordtimes_i);
        ws.addCell(labelTestitem_i);
        ws.addCell(labelMaxvalue_i);
        ws.addCell(labelMinvalue_i);
        ws.addCell(labelTestvalue_i);
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
   * 创建JTable方法
   * 
   * @return
   */
  private static JTable getTestTable(String tableName) {
    Vector<Object> rowNum = null, colNum = null;
    // 创建列对象
    colNum = new Vector<>();
    colNum.add("");
    colNum.add("机种名");
    colNum.add("测试总数");
    colNum.add("良品数");
    colNum.add("不良数");
    colNum.add("测试时间");
    colNum.add("日期");

    // 创建行对象
    rowNum = new Vector<>();
    List<RecordData> tableList = getAllByDB(tableName);
    for (RecordData rd : tableList) {
      Vector<String> vt = new Vector<>();
      vt.add("");
      vt.add(rd.getName());
      vt.add(rd.getSum());
      vt.add(rd.getOk());
      vt.add(rd.getNg());
      vt.add(rd.getTimes());
      vt.add(rd.getDate());

      rowNum.add(vt);
    }

    DefaultTableModel model = new DefaultTableModel(rowNum, colNum) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    JTable table = new JTable(model);
    return table;
  }

  /**
   * 提供设置JTable方法
   * 
   * @param table
   * @return
   */
  public static JTable completedTable(String tableName) {

    JTable table = getTestTable(tableName);
    
    DefaultTableCellRenderer r = new DefaultTableCellRenderer(); // 设置
    r.setHorizontalAlignment(JLabel.CENTER); // 单元格内容居中
    // table.setOpaque(false); //设置表透明
    JTableHeader jTableHeader = table.getTableHeader(); // 获取表头
    // 设置表头名称字体样式
    jTableHeader.setFont(new Font("宋体", Font.PLAIN, 14));
    // 设置表头名称字体颜色
    jTableHeader.setForeground(Color.BLACK);
    jTableHeader.setDefaultRenderer(r);

    // 表头不可拖动
    jTableHeader.setReorderingAllowed(false);
    // 列大小不可改变
    jTableHeader.setResizingAllowed(false);

    // 设置列宽
    TableColumn colNull = table.getColumnModel().getColumn(0);
    TableColumn colTestitem = table.getColumnModel().getColumn(2);
    TableColumn colMaxvalue = table.getColumnModel().getColumn(3);
    TableColumn colMinvalue = table.getColumnModel().getColumn(4);
    TableColumn colTestvalue = table.getColumnModel().getColumn(5);
    colNull.setPreferredWidth(20);
    colTestitem.setPreferredWidth(150);
    colMaxvalue.setPreferredWidth(120);
    colMinvalue.setPreferredWidth(120);
    colTestvalue.setPreferredWidth(120);

    // table.setEnabled(false); // 内容不可编辑

    table.setDefaultRenderer(Object.class, r); // 居中显示

    table.setRowHeight(30); // 设置行高
    // 增加一行空白行
    // AbstractTableModel tableModel = (AbstractTableModel) table.getModel();
    DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
    tableModel.addRow(new Object[] { "*", "", "", "", "", "", "", "", "", "", "", "", "" });
    table.setGridColor(new Color(245, 245, 245)); // 设置网格颜色
    table.setForeground(Color.BLACK); // 设置文字颜色
    table.setBackground(new Color(245, 245, 245));
    table.setFont(new Font("宋体", Font.PLAIN, 13));
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);// 关闭表格列自动调整

    return table;
  }
  
  /**
   * recordtd表的实体
   * @author hw076
   *
   */
  public static class RecordData {
    
    private String name;
    private String sum;
    private String ok;
    private String ng;
    private String times;
    private String date;
    
    public RecordData() {
      super();
    }

    public RecordData(String name, String sum, String ok, String ng, String times, String date) {
      super();
      this.name = name;
      this.sum = sum;
      this.ok = ok;
      this.ng = ng;
      this.times = times;
      this.date = date;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSum() {
      return sum;
    }

    public void setSum(String sum) {
      this.sum = sum;
    }

    public String getOk() {
      return ok;
    }

    public void setOk(String ok) {
      this.ok = ok;
    }

    public String getNg() {
      return ng;
    }

    public void setNg(String ng) {
      this.ng = ng;
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
  }
}
