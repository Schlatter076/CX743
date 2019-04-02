package loyer.client;

import java.awt.EventQueue;
import java.util.List;

import org.jfree.ui.RefineryUtilities;

import loyer.db.ProductTypeTools;
import loyer.db.ProductTypeTools.ProductType;
import loyer.gui.LogInFrame;
import loyer.properties.Tables;

public class LogIn extends LogInFrame {

  private static List<ProductType> list = null;
  
  static {
    list = ProductTypeTools.getAllByDB();
  }//*/
  
  public LogIn() {
    textField.setText(list.get(0).getName());  //设置默认机种
    frame.pack();
    RefineryUtilities.centerFrameOnScreen(frame); 
  }
  @Override
  public void logInEvent() {
    if(!isDataView) {
      if(textField.getText().equals(list.get(0).getName())) {
        isDataView = true;
        frame.dispose();
        DataView.getDataView(Tables.CX743, list.get(0).getName());
      } 
    }
  }
  @Override
  public void chooseEvent() {
    textField.setText(list.get(typeCount % list.size()).getName());
    typeCount++;
  }
  
  public static void logIn() {
    EventQueue.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        LogIn win = new LogIn();
        win.frame.setVisible(true);
      }
    });
  }//*/
  
}
