package loyer.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

/**
 * 用户工具类
 * @author hw076
 *
 */
public class UserTools {

  private UserTools() {} //不允许其他类创建本类实例
  
  /**
   * 根据ID名获取用户
   * @param id 1-->common 2-->admin 3-->nayin
   * @return
   */
  public static UserData getUserByID(int id) {
    
    UserData user = null;
    String sql = "select * from user where id='"+id+"'";
    ResultSet rs = DBHelper.search(sql, null);
    try {
      if(rs.next()) {
        int i = rs.getInt(1);
        String name = rs.getString(2);
        String pwd = rs.getString(3);
        int limit = rs.getInt(4);
        
        user = new UserData(i, name, pwd, limit);
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "用户数据加载失败:" + e.getLocalizedMessage());
    }
    DBHelper.close();
    return user;
  }
  
  /**
   * user表的实体
   * @author hw076
   *
   */
  public static class UserData {
    
    private int id;
    private String name;
    private String pwd;
    private int limit;
    
    public UserData(int id, String name, String pwd, int limit) {
      super();
      this.id = id;
      this.name = name;
      this.pwd = pwd;
      this.limit = limit;
    }

    public UserData() {
      super();
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPwd() {
      return pwd;
    }

    public void setPwd(String pwd) {
      this.pwd = pwd;
    }

    public int getLimit() {
      return limit;
    }

    public void setLimit(int limit) {
      this.limit = limit;
    }
  }
}
