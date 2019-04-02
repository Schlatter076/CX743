package loyer.db;

import java.awt.Color;
import java.awt.Font;
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

public class DataTools {

  /**
   * 获取全部测试数据
   * @param tableName
   * @return
   */
  public static List<Data> getAllByDB(String tableName) {
    
    List<Data> list = new ArrayList<>();
    String sql = "select * from " + tableName;
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        String s1 = rs.getString(1);
        String s2 = rs.getString(2);
        String s3 = rs.getString(3);
        String s4 = rs.getString(4);
        String s5 = rs.getString(5);
        String s6 = rs.getString(6);
        String s7 = rs.getString(7);
        
        list.add(new Data(s1, s2, s3, s4, s5, s6, s7));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "测试数据加载失败" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return list;
  }
  /**
   * 创建JTable方法
   * 
   * @return
   */
  public static JTable getTestTable(String tableName) {
    Vector<Object> rowNum = null, colNum = null;
    // 创建列对象
    colNum = new Vector<>();
    colNum.add("");
    colNum.add("序号");
    colNum.add("测试项目");
    colNum.add("上限");
    colNum.add("下限");
    colNum.add("测试值");
    colNum.add("单位");
    colNum.add("测试结果");
    colNum.add("备注");

    // 创建行对象
    rowNum = new Vector<>();
    List<Data> tableList = getAllByDB(tableName);
    for (Data rd : tableList) {
      Vector<String> vt = new Vector<>();
      vt.add("");
      vt.add(rd.getPdxuhao());
      vt.add(rd.getTestitem());
      vt.add(rd.getMaxvalue());
      vt.add(rd.getMinvalue());
      vt.add(rd.getTestvalue());
      vt.add(rd.getDanwei());
      vt.add(rd.getTestresult());

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
   * @param tableName
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
    TableColumn colTestResult = table.getColumnModel().getColumn(7);
    colNull.setPreferredWidth(20);
    colTestitem.setPreferredWidth(150);
    colMaxvalue.setPreferredWidth(120);
    colMinvalue.setPreferredWidth(120);
    colTestvalue.setPreferredWidth(120);
    colTestResult.setPreferredWidth(120);

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
    //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);// 关闭表格列自动调整

    return table;
  }
  /**
   * f517表的实例
   * @author Loyer
   *
   */
  public static class Data{
    
    private String pdxuhao;
    private String testitem;
    private String maxvalue;
    private String minvalue;
    private String testvalue;
    private String danwei;
    private String testresult;
    
    
    public Data() {
      super();
    }
    
    public Data(String pdxuhao, String testitem, String maxvalue, String minvalue, String testvalue, String danwei,
        String testresult) {
      super();
      this.pdxuhao = pdxuhao;
      this.testitem = testitem;
      this.maxvalue = maxvalue;
      this.minvalue = minvalue;
      this.testvalue = testvalue;
      this.danwei = danwei;
      this.testresult = testresult;
    }

    public String getPdxuhao() {
      return pdxuhao;
    }
    public void setPdxuhao(String pdxuhao) {
      this.pdxuhao = pdxuhao;
    }
    public String getTestitem() {
      return testitem;
    }
    public void setTestitem(String testitem) {
      this.testitem = testitem;
    }
    public String getMaxvalue() {
      return maxvalue;
    }
    public void setMaxvalue(String maxvalue) {
      this.maxvalue = maxvalue;
    }
    public String getMinvalue() {
      return minvalue;
    }
    public void setMinvalue(String minvalue) {
      this.minvalue = minvalue;
    }
    public String getTestvalue() {
      return testvalue;
    }
    public void setTestvalue(String testvalue) {
      this.testvalue = testvalue;
    }
    public String getDanwei() {
      return danwei;
    }
    public void setDanwei(String danwei) {
      this.danwei = danwei;
    }
    public String getTestresult() {
      return testresult;
    }
    public void setTestresult(String testresult) {
      this.testresult = testresult;
    }
  }
}
