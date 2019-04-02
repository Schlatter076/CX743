package loyer.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class Initialize extends JFrame {

  private static final long serialVersionUID = 1L;
  private JPanel contentPane;
  private int count = 0;
  /** 获取当前屏幕宽度 */
  protected static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
  /** 获取当前屏幕高度 */
  protected static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
  private Timer timer;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    
    Initialize frame = new Initialize();
    frame.setVisible(true);
    LogIn.logIn();
    frame.dispose();
  }

  /**
   * Create the frame.
   */
  public Initialize() {
    
    try {
      // 将界面风格设置成和系统一置
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
    } // */
    
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setUndecorated(true);
    setBounds(WIDTH / 3, HEIGHT / 3, WIDTH / 3, HEIGHT / 4);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);
    
    JLabel label = new JLabel(new ImageIcon(ImageIcon.class.getResource("/pic/img7.jpg")));
    contentPane.add(label, BorderLayout.CENTER);
    
    JProgressBar progressBar = new JProgressBar();
    progressBar.setMaximum(100);
    progressBar.setMinimum(0);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    progressBar.setFont(new Font("宋体", Font.PLAIN, 19));
    progressBar.setForeground(new Color(0, 204, 51));
    contentPane.add(progressBar, BorderLayout.SOUTH);
    
    timer = new Timer(200, e -> {
      count++;
      if(count >= 100) count = 0;
      progressBar.setValue(count);
    });
    timer.start();
  }
}
