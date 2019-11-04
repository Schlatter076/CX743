package loyer.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jfree.ui.RefineryUtilities;

import loyer.db.RecoupTools;
import loyer.properties.Tables;

public class RecoupView extends JDialog {

  private static final long serialVersionUID = 1L;
  private final JPanel contentPanel = new JPanel();
  private String tableName;
  private JTextField[] recField = new JTextField[18];
  private String[] pullStr = new String[9];
  private String[] strokeStr = new String[9];
  private String[] recStr = {"Home力", "Back力", "Audio力", "Navi力", "Power力", "PowerUp力", "PowerDown力", "PowerLeft力", "PowerRight力",
      "Home行程", "Back行程", "Audio行程", "Navi行程", "Power行程", "PowerUp行程", "PowerDown行程", "PowerLeft行程", "PowerRight行程" };
  private static final String SEPARATOR = System.getProperty("line.separator");

  /*
  public static void main(String[] args) {
    try {
      RecoupView dialog = new RecoupView(Tables.CX743);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//*/
  
  public static void getRecoupView(String tName) {
    try {
      RecoupView dialog = new RecoupView(tName);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public RecoupView(String tNames) {

    this.tableName = tNames;
    
    try {
      // 将界面风格设置成和系统一置
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
    } // */

    setTitle("补偿值设置");
    setModal(true);
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/pic/frame.jpg")));
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }

    });
    setBounds(100, 100, 450, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setLayout(new FlowLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout());
    
    JTextArea area = new JTextArea(4, 50);
    area.setBackground(Color.LIGHT_GRAY);
    area.setEditable(false);
    area.append("*修改参数时，请注意以下几点：" + SEPARATOR);
    area.append("1.先修改文本框内数值" + SEPARATOR);
    area.append("2.若想撤销，可点击"+"撤销更改"+"按键" + SEPARATOR);
    area.append("3.单击"+"参数设置"+"按键设置参数");
    JScrollPane pane = new JScrollPane(area);
    pane.setBorder(new TitledBorder(new EtchedBorder(), "Tips", TitledBorder.LEFT, TitledBorder.TOP,
          new Font("等线", Font.ITALIC, 13)));
    
    contentPanel.add(pane, BorderLayout.SOUTH);
    
    JPanel txtPanel = new JPanel(new GridLayout(3, 6, 5, 5));
    txtPanel.setBackground(Color.LIGHT_GRAY);
    txtPanel.setBorder(new TitledBorder(new EtchedBorder(), "补偿值列表", TitledBorder.CENTER, TitledBorder.BOTTOM,
          new Font("等线", Font.ITALIC, 13)));
    
    for(int i = 0; i < 18; i++) {
      recField[i] = new JTextField(10);
      txtPanel.add(new MyPanel(recStr[i], recField[i]));
    }
    
    JButton setButt = new JButton("参数设置");
    setButt.addActionListener(e -> {
      for(int i = 0; i < 9; i++) {
        pullStr[i] = recField[i].getText();
        strokeStr[i] = recField[i + 9].getText();
      }
      int p = RecoupTools.updatePull(tableName, pullStr);
      int s = RecoupTools.updateStroke(tableName, strokeStr);
      if(p > 0 && s > 0) {
        JOptionPane.showMessageDialog(null, "数据设置成功!");
      }
    });
    JButton exitButt = new JButton("退出参数设置");
    exitButt.addActionListener(e -> {
      close();
    });
    JButton resetButt = new JButton("撤销更改");
    resetButt.addActionListener(e -> {
      initLoad();
    });
    JPanel buttPanel = new JPanel(new GridLayout(1, 3, 20, 5));
    buttPanel.setBorder(new EtchedBorder());
    buttPanel.add(setButt);
    buttPanel.add(resetButt);
    buttPanel.add(exitButt);
    
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(txtPanel, BorderLayout.CENTER);
    panel.add(buttPanel, BorderLayout.SOUTH);
    contentPanel.add(panel, BorderLayout.CENTER);
    
    if(tableName.equals(Tables.CX743)) {
      List<String> pullList = RecoupTools.getPull(tableName);
      List<String> strokeList = RecoupTools.getStroke(tableName);
      for(int i = 0; i < 9; i++) {
        recField[i].setText(pullList.get(i));
        //System.out.println(pullList.get(i));
        recField[i + 9].setText(strokeList.get(i));
      }
    }
    
    this.pack();
    RefineryUtilities.centerFrameOnScreen(this);
  }
  
  /**
   * 载入数据库补偿值
   */
  private void initLoad() {
    if(tableName.equals(Tables.CX743)) {
      List<String> pullList = RecoupTools.getPull(tableName);
      List<String> strokeList = RecoupTools.getStroke(tableName);
      for(int i = 0; i < 9; i++) {
        recField[i].setText(pullList.get(i));
        recField[i + 9].setText(strokeList.get(i));
      }
    }
  }

  private void close() {
    this.dispose();
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  public static class MyPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public MyPanel(String title, JTextField field) {
      TitledBorder tb = new TitledBorder(new EtchedBorder(), title, TitledBorder.LEFT, TitledBorder.TOP,
          new Font("等线", Font.ITALIC, 13), Color.BLUE);
      setBorder(tb);
      setLayout(new BorderLayout());
      field.setHorizontalAlignment(SwingConstants.CENTER);
      field.setFont(new Font("宋体", Font.BOLD, 15));
      field.setBackground(new Color(245, 245, 245));
      add(field, BorderLayout.CENTER);
    }
  }

}
