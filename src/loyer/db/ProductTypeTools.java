package loyer.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * 产品类型工具类
 * @author hw076
 *
 */
public class ProductTypeTools {

  private ProductTypeTools() {} //不允许其他类创建本来实例
  
  /**
   * 获取全部机种
   * @return
   */
  public static List<ProductType> getAllByDB() {
    List<ProductType> list = new ArrayList<>();
    String sql = "select * from product_type";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      while(rs.next()) {
        int num = rs.getInt(1);
        String name = rs.getString(2);
        list.add(new ProductType(num, name));
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "产品型号加载失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return list;
  }
  /**
   * 根据数字标号来获取产品型号
   * @param num
   * @return
   */
  public static ProductType getTypeByNum(int num) {
    ProductType data = null;
    String sql = "select * from product_type where num='"+num+"'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      if(rs.next()) {
        String name = rs.getString(2);
        data = new ProductType(num, name);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "产品型号加载失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return data;
  }
  
  /**
   * product_type表的实体
   * @author hw076
   *
   */
  public static class ProductType {
    
    private int num;
    private String name;
    
    public ProductType() {
      super();
    }
    public ProductType(int num, String name) {
      super();
      this.num = num;
      this.name = name;
    }
    public int getNum() {
      return num;
    }
    public void setNum(int num) {
      this.num = num;
    }
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
  }
}
