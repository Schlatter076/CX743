package loyer.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class RecoupTools {

  private RecoupTools() {} //不允许其他类创建本类实例
  
  /**
   * 获取拉力补偿值
   * @param tableName
   * @param name
   * @return
   */
  public static double getPullByName(String tableName, String name) {
    String val = "";
    String sql = "select pull from "+ tableName +"_recoup where name='" + name + "' ";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      if(rs.next()) {
        val += rs.getString(1);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "补偿值加载失败：" + e.getLocalizedMessage());
      return 0;
    }
    DBHelper.close();
    if(val.length() < 1) return 0;
    else return Double.parseDouble(val);
  }
  /**
   * 获取拉力补偿值
   * @param tableName
   * @return
   */
  public static List<String> getPull(String tableName) {
    List<String> list = new ArrayList<>();
    String sql = "select pull from "+ tableName +"_recoup";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        list.add(rs.getString(1));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "补偿值加载失败：" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return list;
  }
  /**
   * 获取行程补偿值
   * @param tableName
   * @return
   */
  public static List<String> getStroke(String tableName) {
    List<String> list = new ArrayList<>();
    String sql = "select stroke from "+ tableName +"_recoup";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        list.add(rs.getString(1));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "补偿值加载失败：" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return list;
  }
  /**
   * 获取行程补偿值
   * @param tableName
   * @param name
   * @return
   */
  public static double getStrokeByName(String tableName, String name) {
    String val = "";
    String sql = "select stroke from "+ tableName +"_recoup where name='" + name + "' ";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      if(rs.next()) {
        val += rs.getString(1);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "补偿值加载失败：" + e.getLocalizedMessage());
      return 0;
    }
    DBHelper.close();
    if(val.length() < 1) return 0;
    else return Double.parseDouble(val);
  }
  /**
   * 修改拉力补偿值
   * @param tableName
   * @param name
   * @param val
   * @return
   */
  public static int updatePull(String tableName, String name, double val) {
    
    String sql = "update "+ tableName +"_recoup set pull='" + val + "' where name='" + name + "'";
    int back = DBHelper.AddU(sql, null);
    DBHelper.close();
    return back;
  }
  /**
   * 修改拉力补偿值
   * @param tableName
   * @param vals
   * @return
   */
  public static int updatePull(String tableName, String[] vals) {
    
    String sql = "update "+ tableName +"_recoup set pull=case when name='Home' then '"+vals[0] + "' when name='Back' then '"+vals[1]+"' "
        + "when name='Audio' then '"+vals[2]+"' when name='Navi' then '"+vals[3]+"' when name='Power' then '"+vals[4]+"' "
            + "when name='PowerUp' then '"+vals[5]+"' when name='PowerDown' then '"+vals[6]+"' "
                + "when name='PowerLeft' then '"+vals[7]+"' when name='PowerRight' then '"+vals[8]+"' end";
    int back = DBHelper.AddU(sql, null);
    DBHelper.close();
    return back;
  }
  
  /**
   * 修改行程补偿值
   * @param tableName
   * @param vals
   * @return
   */
  public static int updateStroke(String tableName, String[] vals) {
    
    String sql = "update "+ tableName +"_recoup set stroke=case when name='Home' then '"+vals[0] + "' when name='Back' then '"+vals[1]+"' "
        + "when name='Audio' then '"+vals[2]+"' when name='Navi' then '"+vals[3]+"' when name='Power' then '"+vals[4]+"' "
            + "when name='PowerUp' then '"+vals[5]+"' when name='PowerDown' then '"+vals[6]+"' "
                + "when name='PowerLeft' then '"+vals[7]+"' when name='PowerRight' then '"+vals[8]+"' end";
    int back = DBHelper.AddU(sql, null);
    DBHelper.close();
    return back;
  }
  /**
   * 修改行程补偿值
   * @param tableName
   * @param name
   * @param val
   * @return
   */
  public static int updateStroke(String tableName, String name, double val) {
    
    String sql = "update "+ tableName +"_recoup set stroke='" + val + "' where name='" + name + "'";
    int back = DBHelper.AddU(sql, null);
    DBHelper.close();
    return back;
  }
}
