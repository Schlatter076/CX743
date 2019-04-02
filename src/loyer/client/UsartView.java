package loyer.client;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.jfree.ui.RefineryUtilities;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import loyer.db.PositionTools;
import loyer.db.PositionTools.PositionData;
import loyer.exception.NoSuchPort;
import loyer.exception.NotASerialPort;
import loyer.exception.PortInUse;
import loyer.exception.SerialPortParamFail;
import loyer.exception.TooManyListeners;
import loyer.properties.Commands;
import loyer.properties.Tables;
import loyer.serial.SerialPortTools;

public class UsartView extends JDialog {

  private static final long serialVersionUID = 1L;
  private JTabbedPane tabbedPane;
  private JPanel leftPane;
  private JPanel rightPane;
  private JMenuBar menuBar;
  private JMenu helpMenu;
  private JMenuItem tipItem;
  private JMenuItem aboutItem;

  /** 参数面板 */
  private JPanel[] panels = new JPanel[9];
  /** 电机参数值 */
  private JTextField[][] fields = new JTextField[9][7];
  private JButton[][] butts = new JButton[9][4];
  /** 电机参数名 */
  private JTextField[][] args = new JTextField[9][7];
  private JButton[] modButts = new JButton[8];
  private String tableName;
  /** 接收缓冲区 */
  private JTextArea rxArea;
  /** 发送缓冲区 */
  private JTextArea txArea;
  private JRadioButton rxStrButt;
  private JRadioButton rxHexButt;
  private JButton clearRxButt;
  private JButton saveRxButt;
  private JRadioButton txStrButt;
  private JRadioButton txHexButt;
  private JButton clearTxButt;
  private JButton saveTxButt;
  private JButton transFileButt;
  private JButton transDataButt;
  private JButton autoTransButt;
  private JTextField cycleField;
  private JComboBox<String> portListBox;
  private JComboBox<String> baudBox;
  private JComboBox<String> parityBox;
  private JComboBox<String> stopBitBox;
  private JComboBox<String> dataBitBox;
  private JButton openPort;
  /** 串口对象 */
  private SerialPort COM1;
  private ArrayList<String> portList = SerialPortTools.findPort();
  /** 换行符 */
  private static final String SEPARATOR = System.getProperty("line.separator");
  private Timer timer1;
  private JTextField rxCountField;
  private JTextField txCountField;
  private JButton clearCountButt;
  private String[] switchStr = { "Home", "Back", "Audio", "Navi", "Power", "PowerUp", "PowerDown", "PowerLeft",
      "PowerRight" };
  private JButton[] trButts = new JButton[24];
  private JCheckBox[] trCButts = new JCheckBox[24];
  private JTextField[] trFields = new JTextField[24];
  private String[] values = new String[9];
  private Robot r = null;

  
  /*
  public static void main(String[] args) {
    try {
      UsartView dialog = new UsartView(Tables.CX743);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//*/
  
  public static void getUsartView(String tName) {
    try {
      UsartView dialog = new UsartView(tName);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public UsartView(String tName) {

    this.tableName = tName;

    try {
      // 将界面风格设置成和系统一置
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
    } // */

    try {
      r = new Robot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }

    setTitle("调试助手");
    setModal(true);
    setIconImage(Toolkit.getDefaultToolkit().getImage(JFrame.class.getResource("/pic/frame.jpg")));
    getContentPane().setLayout(new BorderLayout());

    // 窗口"X"关闭事件
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });

    menuBar = new JMenuBar();
    helpMenu = new JMenu("帮助(H)");
    tipItem = new JMenuItem("提示与技巧(T)...");
    aboutItem = new JMenuItem("关于(A)");
    helpMenu.add(tipItem);
    helpMenu.addSeparator();
    helpMenu.add(aboutItem);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);
    tipItem.addActionListener(e -> tips());
    aboutItem.addActionListener(e -> about());

    tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    leftPane = new JPanel(new BorderLayout(5, 10));
    leftPane.setBorder(new TitledBorder(new EtchedBorder(), "串口调试助手", TitledBorder.CENTER, TitledBorder.TOP,
        new Font("等线", Font.PLAIN, 13), Color.BLACK));

    rxArea = new JTextArea();
    rxArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
    JScrollPane rxPane = new JScrollPane(rxArea);
    rxPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    rxStrButt = new JRadioButton("文本模式");
    rxStrButt.addActionListener(e -> {
      if (rxStrButt.isSelected()) {
        rxHexButt.setSelected(false);
      } else if (!rxHexButt.isSelected()) {
        rxStrButt.setSelected(true);
      }
    });
    rxHexButt = new JRadioButton("HEX模式", true);
    rxHexButt.addActionListener(e -> {
      if (rxHexButt.isSelected()) {
        rxStrButt.setSelected(false);
      } else if (!rxStrButt.isSelected()) {
        rxHexButt.setSelected(true);
      }
    });
    clearRxButt = new JButton("清空接收区");
    clearRxButt.addActionListener(e -> {
      rxArea.setText("");
    });
    saveRxButt = new JButton("保存接收数据");
    JPanel rxButtPanel = new JPanel(new GridLayout(4, 1, 5, 5));
    rxButtPanel.add(rxStrButt);
    rxButtPanel.add(rxHexButt);
    rxButtPanel.add(clearRxButt);
    rxButtPanel.add(saveRxButt);
    JPanel rxPanel = new JPanel(new BorderLayout(5, 5));
    rxPanel.setBorder(new TitledBorder(new EtchedBorder(), "接收缓冲区", TitledBorder.LEFT, TitledBorder.TOP));
    rxPanel.add(rxPane, BorderLayout.CENTER);
    rxPanel.add(rxButtPanel, BorderLayout.WEST);

    txArea = new JTextArea();
    txArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
    JScrollPane txPane = new JScrollPane(txArea);
    txPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    txStrButt = new JRadioButton("文本模式");
    txStrButt.addActionListener(e -> {
      if (txStrButt.isSelected()) {
        txHexButt.setSelected(false);
      } else if (!txHexButt.isSelected()) {
        txStrButt.setSelected(true);
      }
    });
    txHexButt = new JRadioButton("HEX模式", true);
    txHexButt.addActionListener(e -> {
      if (txHexButt.isSelected()) {
        txStrButt.setSelected(false);
      } else if (!txStrButt.isSelected()) {
        txHexButt.setSelected(true);
      }
    });
    clearTxButt = new JButton("清空发送区");
    clearTxButt.addActionListener(e -> {
      txArea.setText("");
    });
    saveTxButt = new JButton("保存发送数据");
    JPanel txButtPanel = new JPanel(new GridLayout(4, 1, 5, 5));
    txButtPanel.add(txStrButt);
    txButtPanel.add(txHexButt);
    txButtPanel.add(clearTxButt);
    txButtPanel.add(saveTxButt);

    JPanel botPanel = new JPanel(new GridLayout(1, 4, 5, 5));
    transFileButt = new JButton("发送文件");
    transFileButt.setEnabled(false);
    transDataButt = new JButton("发送数据");
    transDataButt.setEnabled(false);
    transDataButt.addActionListener(e -> {
      transData();
    });
    autoTransButt = new JButton("自动发送");
    autoTransButt.setEnabled(false);
    autoTransButt.addActionListener(e -> {
      autoTrans();
    });
    cycleField = new JTextField("500");
    cycleField.setHorizontalAlignment(JTextField.CENTER);
    cycleField.setFont(new Font("宋体", Font.PLAIN, 12));
    JPanel cyclePanel = new JPanel(new GridLayout(1, 2));
    cyclePanel.add(new JLabel("周期(ms)") {
      private static final long serialVersionUID = 1L;

      @Override
      public void setHorizontalAlignment(int alignment) {
        super.setHorizontalAlignment(JLabel.RIGHT);
      }
    });
    cyclePanel.add(cycleField);

    botPanel.add(transFileButt);
    botPanel.add(transDataButt);
    botPanel.add(autoTransButt);
    botPanel.add(cyclePanel);

    JPanel txPanel = new JPanel(new BorderLayout(5, 5));
    txPanel.setBorder(new TitledBorder(new EtchedBorder(), "发送缓冲区", TitledBorder.LEFT, TitledBorder.TOP));
    txPanel.add(txPane, BorderLayout.CENTER);
    txPanel.add(txButtPanel, BorderLayout.WEST);
    txPanel.add(botPanel, BorderLayout.SOUTH);

    JPanel jp = new JPanel(new GridLayout(2, 1, 5, 5));
    jp.add(rxPanel);
    jp.add(txPanel);

    portListBox = new JComboBox<>();
    baudBox = new JComboBox<>();
    parityBox = new JComboBox<>();
    stopBitBox = new JComboBox<>();
    dataBitBox = new JComboBox<>();
    SerialParam param1 = new SerialParam("串口", portListBox);
    SerialParam param2 = new SerialParam("波特率", baudBox);
    SerialParam param5 = new SerialParam("数据位", dataBitBox);
    SerialParam param3 = new SerialParam("校验位", parityBox);
    SerialParam param4 = new SerialParam("停止位", stopBitBox);
    JPanel paramPanel = new JPanel(new GridLayout(1, 5, 20, 5));
    for (String s : portList) {
      portListBox.addItem(s);
    }
    baudBox.addItem("9600");
    baudBox.addItem("2400");
    baudBox.addItem("19200");
    parityBox.addItem("0");
    parityBox.addItem("1");
    parityBox.addItem("2");
    stopBitBox.addItem("1");
    dataBitBox.addItem("8");
    dataBitBox.addItem("5");
    dataBitBox.addItem("6");
    dataBitBox.addItem("7");
    dataBitBox.addItem("9");

    paramPanel.add(param1);
    paramPanel.add(param2);
    paramPanel.add(param5);
    paramPanel.add(param3);
    paramPanel.add(param4);

    openPort = new JButton("打开串口");
    openPort.setFont(new Font("微软雅黑", Font.PLAIN, 20));
    openPort.addActionListener(e -> {
      openPortListener();
    });
    JPanel openPortPanel = new JPanel(new BorderLayout(10, 5));
    rxCountField = new JTextField("0");
    txCountField = new JTextField("0");
    SerialParam rxCount = new SerialParam("接收", rxCountField);
    SerialParam txCount = new SerialParam("发送", txCountField);
    JPanel countPanel = new JPanel(new GridLayout(2, 1));
    countPanel.add(rxCount);
    countPanel.add(txCount);

    openPortPanel.add(openPort, BorderLayout.WEST);
    openPortPanel.add(new JLabel("注：先查看参数，然后打开串口") {

      private static final long serialVersionUID = 1L;

      @Override
      public void setHorizontalAlignment(int alignment) {
        super.setHorizontalAlignment(JLabel.LEFT);
      }

      @Override
      public void setForeground(Color fg) {
        super.setForeground(Color.BLUE);
      }
    }, BorderLayout.CENTER);
    openPortPanel.add(countPanel, BorderLayout.EAST);

    JPanel portPanel = new JPanel(new BorderLayout(5, 10));
    portPanel.add(paramPanel, BorderLayout.NORTH);
    portPanel.add(openPortPanel, BorderLayout.CENTER);
    clearCountButt = new JButton("清零");
    clearCountButt.addActionListener(e -> {
      rxCountField.setText("0");
      txCountField.setText("0");
    });
    portPanel.add(clearCountButt, BorderLayout.EAST);

    JPanel rightP = new JPanel(new GridLayout(24, 1));
    rightP.setBorder(new TitledBorder(new EtchedBorder(), "多字符串发送", TitledBorder.CENTER, TitledBorder.TOP,
        new Font("等线", Font.PLAIN, 13), Color.BLACK));
    JPanel[] ps = new JPanel[24];
    for (int i = 0; i < 24; i++) {
      ps[i] = new JPanel(new BorderLayout());
      if (i < 9)
        trButts[i] = new JButton("0" + (i + 1));
      else
        trButts[i] = new JButton(i + 1 + "");
      trFields[i] = new JTextField();
      trCButts[i] = new JCheckBox();
      trCButts[i].setSelected(true);
      ps[i].add(trButts[i], BorderLayout.WEST);
      ps[i].add(trFields[i], BorderLayout.CENTER);
      ps[i].add(trCButts[i], BorderLayout.EAST);
      ps[i].setBorder(new LineBorder(Color.LIGHT_GRAY));
      rightP.add(ps[i]);

      trButts[i].addActionListener(e -> {
        String ccd = trFields[Integer.parseInt(e.getActionCommand()) - 1].getText().trim();
        if (ccd.length() > 5) {
          SerialPortTools.writeBytes(COM1, SerialPortTools.toByteArray(ccd));
        }
      });
    }

    JSplitPane leftSP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jp, rightP);
    leftSP.setResizeWeight(0.6D);

    leftPane.add(leftSP, BorderLayout.CENTER);
    leftPane.add(portPanel, BorderLayout.SOUTH);
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    rightPane = new JPanel(new BorderLayout(5, 5));
    rightPane.setBorder(new TitledBorder(new EtchedBorder(), "电机参数设置", TitledBorder.CENTER, TitledBorder.TOP,
        new Font("等线", Font.PLAIN, 13), Color.BLACK));
    tabbedPane.addTab("串口工具", leftPane);

    tabbedPane.addTab("位置参数", rightPane);

    JPanel panel = new JPanel(new GridLayout(9, 1, 1, 2));

    for (int i = 0; i < 9; i++) {

      panels[i] = new JPanel(new GridLayout(2, 9, 2, 2));
      panels[i].setBorder(new TitledBorder(new EtchedBorder(), switchStr[i] + "参数:", TitledBorder.LEFT,
          TitledBorder.TOP, new Font("宋体", Font.PLAIN, 12), Color.BLACK));

      args[i][0] = new JTextField("X设定位置");
      args[i][0].setEditable(false);
      args[i][0].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][0]);
      args[i][1] = new JTextField("X当前位置");
      args[i][1].setEditable(false);
      args[i][1].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][1]);
      args[i][2] = new JTextField("Y设定位置");
      args[i][2].setEditable(false);
      args[i][2].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][2]);
      args[i][3] = new JTextField("Y当前位置");
      args[i][3].setEditable(false);
      args[i][3].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][3]);
      args[i][4] = new JTextField("Z设定位置");
      args[i][4].setEditable(false);
      args[i][4].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][4]);
      args[i][5] = new JTextField("Z当前位置");
      args[i][5].setEditable(false);
      args[i][5].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][5]);
      args[i][6] = new JTextField("行程");
      args[i][6].setEditable(false);
      args[i][6].setHorizontalAlignment(JTextField.CENTER);
      panels[i].add(args[i][6]);

      butts[i][0] = new JButton("更新" + switchStr[i] + "参数");
      butts[i][1] = new JButton("去" + switchStr[i] + "位置");
      panels[i].add(butts[i][0]);
      panels[i].add(butts[i][1]);

      PositionData pd = PositionTools.getByName(tableName, switchStr[i]);

      fields[i][0] = new JTextField(pd.getXposition() + "");
      fields[i][1] = new JTextField(pd.getXtemp() + "");
      fields[i][2] = new JTextField(pd.getYposition() + "");
      fields[i][3] = new JTextField(pd.getYtemp() + "");
      fields[i][4] = new JTextField(pd.getZposition() + "");
      fields[i][5] = new JTextField(pd.getZtemp() + "");
      fields[i][6] = new JTextField("0");

      for (int j = 0; j < 7; j++) {
        fields[i][j].setHorizontalAlignment(JTextField.CENTER);
        fields[i][j].setForeground(Color.BLUE);
        fields[i][j].setBackground(new Color(243, 235, 197));
      }

      panels[i].add(fields[i][0]);
      panels[i].add(fields[i][1]);
      panels[i].add(fields[i][2]);
      panels[i].add(fields[i][3]);
      panels[i].add(fields[i][4]);
      panels[i].add(fields[i][5]);
      panels[i].add(fields[i][6]);

      butts[i][2] = new JButton("回读" + switchStr[i] + "行程");
      butts[i][3] = new JButton("回原点");
      panels[i].add(butts[i][2]);
      panels[i].add(butts[i][3]);

      panel.add(panels[i]);

      for (int t = 0; t < 4; t++) {
        butts[i][t].addActionListener(e -> {
          buttProcess(e.getActionCommand());
        });
      }
    }
    rightPane.add(panel, BorderLayout.CENTER);
    JPanel bp = new JPanel(new GridLayout(1, 8, 10, 5));
    modButts[0] = new JButton("X前进");
    modButts[1] = new JButton("X后退");
    modButts[2] = new JButton("Y前进");
    modButts[3] = new JButton("Y后退");
    modButts[4] = new JButton("Z前进");
    modButts[5] = new JButton("Z后退");
    modButts[6] = new JButton("电机停止");
    modButts[7] = new JButton("回原点");
    bp.setBorder(new TitledBorder("电机控制"));
    for (int i = 0; i < 8; i++) {
      modButts[i].addActionListener(e -> {
        buttProcess(e.getActionCommand());
      });
      bp.add(modButts[i]);
    }
    rightPane.add(bp, BorderLayout.SOUTH);

    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    // 自动发送定时任务
    timer1 = new Timer(Integer.parseInt(cycleField.getText()), e -> {
      String str = txArea.getText();
      if (txHexButt.isSelected()) {
        byte[] data = SerialPortTools.toByteArray(str);
        SerialPortTools.writeBytes(COM1, SerialPortTools.toByteArray(txArea.getText()));
        int count = Integer.parseInt(txCountField.getText()) + data.length;
        txCountField.setText(count + "");
      } else if (txStrButt.isSelected()) {
        SerialPortTools.writeString(COM1, "UTF-8", str);
        int count = Integer.parseInt(txCountField.getText()) + str.length();
        txCountField.setText(count + "");
      }
    });// */

    trFields[0].setText(SerialPortTools.bytesToHex(Commands.X_FORWARD));
    trFields[1].setText(SerialPortTools.bytesToHex(Commands.X_BACKWARD));
    trFields[2].setText(SerialPortTools.bytesToHex(Commands.Y_FORWARD));
    trFields[3].setText(SerialPortTools.bytesToHex(Commands.Y_BACKWARD));
    trFields[4].setText(SerialPortTools.bytesToHex(Commands.Z_FORWARD));
    trFields[5].setText(SerialPortTools.bytesToHex(Commands.Z_BACKWARD));
    trFields[6].setText(SerialPortTools.bytesToHex(Commands.MOTOR_STOP));
    trFields[7].setText(SerialPortTools.bytesToHex(Commands.RESET));

    this.pack();
    RefineryUtilities.centerFrameOnScreen(this);

  }

  /**
   * 打开串口事件
   */
  public void openPortListener() {
    if (COM1 == null && openPort.getText().equals("打开串口")) {
      if (portList.contains(portListBox.getSelectedItem().toString())) {
        try {
          COM1 = SerialPortTools.getPort(portListBox.getSelectedItem().toString(),
              Integer.parseInt(baudBox.getSelectedItem().toString()),
              Integer.parseInt(dataBitBox.getSelectedItem().toString()),
              Integer.parseInt(stopBitBox.getSelectedItem().toString()),
              Integer.parseInt(parityBox.getSelectedItem().toString()));
          openPort.setText("关闭串口");
          transFileButt.setEnabled(true);
          transDataButt.setEnabled(true);
          autoTransButt.setEnabled(true);
          SerialPortTools.add(COM1, arg0 -> {
            switch (arg0.getEventType()) {
            case SerialPortEvent.BI: // 10 通讯中断
            case SerialPortEvent.OE: // 7 溢位（溢出）错误
            case SerialPortEvent.FE: // 9 帧错误
            case SerialPortEvent.PE: // 8 奇偶校验错误
            case SerialPortEvent.CD: // 6 载波检测
            case SerialPortEvent.CTS: // 3 清除待发送数据
            case SerialPortEvent.DSR: // 4 待发送数据准备好了
            case SerialPortEvent.RI: // 5 振铃指示
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
              JOptionPane.showMessageDialog(null, COM1.getName() + "::" + arg0.getEventType());
              break;
            case SerialPortEvent.DATA_AVAILABLE:
              // 有数据到达
              r.delay(50);
              if (rxHexButt.isSelected()) {
                byte[] data = SerialPortTools.readBytes(COM1);
                rxArea.append(SerialPortTools.bytesToHex(data) + SEPARATOR);
                int count = Integer.parseInt(rxCountField.getText()) + data.length;
                rxCountField.setText(count + "");
                debugDataArrivals(data);
              } else if (rxStrButt.isSelected()) {
                String str = SerialPortTools.readString(COM1, "UTF-8");
                rxArea.append(str + SEPARATOR);
                int count = Integer.parseInt(rxCountField.getText()) + str.length();
                rxCountField.setText(count + "");
              }
              break;
            }
          });
        } catch (NumberFormatException | SerialPortParamFail | NotASerialPort | NoSuchPort | PortInUse
            | TooManyListeners e) {
          JOptionPane.showMessageDialog(null, e.toString());
          openPort.setText("打开串口");
          transFileButt.setEnabled(false);
          transDataButt.setEnabled(false);
          autoTransButt.setEnabled(false);
        }
      }
    } else if (openPort.getText().equals("关闭串口")) {
      COM1.close();
      transFileButt.setEnabled(false);
      transDataButt.setEnabled(false);
      autoTransButt.setEnabled(false);
      COM1 = null;
      openPort.setText("打开串口");
    }
  }

  /**
   * 调试数据传回
   * 
   * @param data
   */
  public void debugDataArrivals(byte[] data) {
    for (int i = 0; i < 8; i++) {
      if (isEquals(data[i], "f3") && isEquals(data[i + 1], "f4") && isEquals(data[i + 15], "0a")) {
        if (isEquals(data[i + 14], "20")) {
          StringBuilder xsb = new StringBuilder();
          StringBuilder ysb = new StringBuilder();
          StringBuilder zsb = new StringBuilder();
          for (int t = 2; t < 6; t++) {
            xsb.append(String.format("%02x", data[i + t]));
            ysb.append(String.format("%02x", data[i + t + 4]));
            zsb.append(String.format("%02x", data[i + t + 8]));
          }
          for (int j = 0; j < 9; j++) {
            fields[j][1].setText(Integer.parseInt(xsb.toString().trim(), 16) + "");
            fields[j][3].setText(Integer.parseInt(ysb.toString().trim(), 16) + "");
            fields[j][5].setText(Integer.parseInt(zsb.toString().trim(), 16) + "");
          }
          xsb.delete(0, xsb.length());
          ysb.delete(0, xsb.length());
          zsb.delete(0, xsb.length());
          break;
        }
      }
    }
  }

  public boolean isEquals(byte hex, String str) {
    return String.format("%02x", hex).equals(str);
  }

  /**
   * 发送数据事件
   */
  public void transData() {
    if (COM1 != null) {
      String str = txArea.getText();
      if (str.length() > 0 && txHexButt.isSelected()) {
        byte[] data = SerialPortTools.toByteArray(str);
        SerialPortTools.writeBytes(COM1, data);
        int count = Integer.parseInt(txCountField.getText()) + data.length;
        txCountField.setText(count + "");
      } else if (str.length() > 0 && txStrButt.isSelected()) {
        SerialPortTools.writeString(COM1, "UTF-8", str);
        int count = Integer.parseInt(txCountField.getText()) + str.length();
        txCountField.setText(count + "");
      } else
        JOptionPane.showMessageDialog(null, COM1.getName() + "::发送数据不能为空！");
    }
  }

  /**
   * 自动发送数据事件
   */
  public void autoTrans() {

    if (COM1 != null && autoTransButt.getText().equals("自动发送")) {
      if (txArea.getText().length() > 0) {
        timer1.setInitialDelay(Integer.parseInt(cycleField.getText()));
        timer1.start();
        autoTransButt.setText("停止发送");
      }
    } else if (autoTransButt.getText().equals("停止发送")) {
      autoTransButt.setText("自动发送");
      timer1.stop();
    }
  }

  /**
   * 参数更新设置
   * 
   * @param name
   * @param row
   */
  public void updateSetting(String name, int row) {
    for (int i = 0; i < 3; i++) {
      fields[row][i * 2].setText(fields[row][i * 2 + 1].getText());
      // fields[row][i*2 + 1].setText("0");
    }
    values[0] = name;
    for (int i = 0; i < 6; i++) {
      values[i + 1] = fields[row][i].getText();
    }
    values[7] = LocalDate.now().toString();
    values[8] = "行程:" + fields[row][6].getText();
    if (PositionTools.update(tableName, values) > 0) {
      JOptionPane.showMessageDialog(null, values[0] + "参数更新成功！");
    }
  }

  /**
   * 去指定位置
   * 
   * @param pos
   * @param x
   * @param y
   * @param z
   * @return
   */
  public void fillPosi(byte[] pos, int x, int y, int z) {
    pos[2] = (byte) ((x >> 24) & 0xff);
    pos[3] = (byte) ((x >> 16) & 0xff);
    pos[4] = (byte) ((x >> 8) & 0xff);
    pos[5] = (byte) (x & 0xff);
    pos[6] = (byte) ((y >> 24) & 0xff);
    pos[7] = (byte) ((y >> 16) & 0xff);
    pos[8] = (byte) ((y >> 8) & 0xff);
    pos[9] = (byte) (y & 0xff);
    pos[10] = (byte) ((z >> 24) & 0xff);
    pos[11] = (byte) ((z >> 16) & 0xff);
    pos[12] = (byte) ((z >> 8) & 0xff);
    pos[13] = (byte) (z & 0xff);
  }

  public void gotoPosi(int row) {
    fillPosi(Commands.GOTO_POSI, Integer.parseInt(fields[row][0].getText()), Integer.parseInt(fields[row][2].getText()),
        Integer.parseInt(fields[row][4].getText()));
    // System.out.println(SerialPortTools.bytesToHex(Commands.GOTO_POSI));
    if (COM1 != null) {
      SerialPortTools.writeBytes(COM1, Commands.GOTO_POSI);
    }
  }

  /**
   * 电机参数按键处理事件
   * 
   * @param command
   */
  public void buttProcess(String command) {
    switch (command) {

    case "去Home位置":
      gotoPosi(0);
      break;
    case "去Back位置":
      gotoPosi(1);
      break;
    case "去Audio位置":
      gotoPosi(2);
      break;
    case "去Navi位置":
      gotoPosi(3);
      break;
    case "去Power位置":
      gotoPosi(4);
      break;
    case "去PowerUp位置":
      gotoPosi(5);
      break;
    case "去PowerDown位置":
      gotoPosi(6);
      break;
    case "去PowerLeft位置":
      gotoPosi(7);
      break;
    case "去PowerRight位置":
      gotoPosi(8);
      break;
    case "更新Home参数":
      updateSetting(Tables.HOME, 0);
      break;
    case "更新Back参数":
      updateSetting(Tables.BACK, 1);
      break;
    case "更新Audio参数":
      updateSetting(Tables.AUDIO, 2);
      break;
    case "更新Navi参数":
      updateSetting(Tables.NAVI, 3);
      break;
    case "更新Power参数":
      updateSetting(Tables.POWER, 4);
      break;
    case "更新PowerUp参数":
      updateSetting(Tables.POWER_UP, 5);
      break;
    case "更新PowerDown参数":
      updateSetting(Tables.POWER_DOWN, 6);
      break;
    case "更新PowerLeft参数":
      updateSetting(Tables.POWER_LEFT, 7);
      break;
    case "更新PowerRight参数":
      updateSetting(Tables.POWER_RIGHT, 8);
      break;
    case "回读Home行程":
      fields[0][6].setText((Integer.parseInt(fields[0][5].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读Back行程":
      fields[1][6].setText((Integer.parseInt(fields[1][5].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读Audio行程":
      fields[2][6].setText((Integer.parseInt(fields[2][5].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读Navi行程":
      fields[3][6].setText((Integer.parseInt(fields[3][5].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读Power行程":
      fields[4][6].setText((Integer.parseInt(fields[4][5].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读PowerUp行程":
      fields[5][6].setText((Integer.parseInt(fields[5][1].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读PowerDown行程":
      fields[6][6].setText((Integer.parseInt(fields[6][1].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读PowerLeft行程":
      fields[7][6].setText((Integer.parseInt(fields[7][3].getText()) / 2 / 1600 * 5) + "");
      break;
    case "回读PowerRight行程":
      fields[8][6].setText((Integer.parseInt(fields[8][3].getText()) / 2 / 1600 * 5) + "");
      break;
    case "X前进":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.X_FORWARD);
      }
      break;
    case "X后退":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.X_BACKWARD);
      }
      break;
    case "Y前进":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.Y_FORWARD);
      }
      break;
    case "Y后退":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.Y_BACKWARD);
      }
      break;
    case "Z前进":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.Z_FORWARD);
      }
      break;
    case "Z后退":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.Z_BACKWARD);
      }
      break;
    case "电机停止":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.MOTOR_STOP);
      }
      break;
    case "回原点":
      if (COM1 != null) {
        SerialPortTools.writeBytes(COM1, Commands.RESET);
      }
      break;

    default:
      break;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * 窗口退出时调用
   */
  public void close() {
    this.dispose();
  }

  /**
   * 提示与技巧菜单事件
   */
  public void tips() {
    int tem = JOptionPane.showConfirmDialog(null, "你确定要查看技巧？", "询问", JOptionPane.YES_NO_OPTION);
    if (tem == JOptionPane.YES_OPTION) {
      JOptionPane.showMessageDialog(null, "并没有什么技巧！哈哈^_^");
    }
  }

  /**
   * 关于菜单事件
   */
  public void about() {
    JOptionPane.showMessageDialog(null, "软件版本：V1.0-2018\r\n技术支持：Loyer");
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  class SerialParam extends JPanel {

    private static final long serialVersionUID = 1L;

    public SerialParam(String paramName, JComboBox<String> box) {
      setLayout(new BorderLayout());
      add(new JLabel(paramName), BorderLayout.WEST);
      add(box, BorderLayout.CENTER);
    }

    public SerialParam(String name, JTextField field) {
      setLayout(new BorderLayout(5, 5));
      add(new JLabel(name), BorderLayout.WEST);
      field.setColumns(10);
      add(field, BorderLayout.CENTER);
    }
  }

}
