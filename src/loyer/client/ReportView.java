package loyer.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.ui.RefineryUtilities;

import loyer.db.RecordTools;

public class ReportView extends JDialog {

  private static final long serialVersionUID = 1L;
  private final JPanel contentPanel = new JPanel();
  private JScrollPane botPane;
  private MyLineChart myLine;
  private String tableName;

  /*
  public static void main(String[] args) {
    try {
      ReportViewer dialog = new ReportViewer(Tables.CX743 + Tables.RECORD);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//*/
  
  public static void getReportView(String tName) {
    try {
      ReportView dialog = new ReportView(tName);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public ReportView(String tName) {
    
    this.tableName = tName;
    
    try {
      // 将界面风格设置成和系统一置
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
    } // */
    
    setTitle("查看不良报告");
    setModal(true); //保持对话框最前
    setBounds(100, 100, 450, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout());
    setIconImage(Toolkit.getDefaultToolkit().getImage(JFrame.class.getResource("/pic/frame.jpg")));
    //窗口添加关闭事件
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    
    myLine = new MyLineChart(RecordTools.getAllByDB(tableName));
    ChartPanel chartPanel = myLine.getChartPanel();
    
    botPane = new JScrollPane(chartPanel);
    botPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    botPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    botPane.setBorder(new TitledBorder(new EtchedBorder(), "不良分析", TitledBorder.LEFT, 
        TitledBorder.TOP, new Font("宋体", Font.PLAIN, 15), Color.BLUE));
    
    contentPanel.add(botPane, BorderLayout.CENTER);
    
    this.pack();
    RefineryUtilities.centerFrameOnScreen(this);
  }
  /**
   * 退出事件
   */
  private void close() {
    this.dispose();
  }

}
