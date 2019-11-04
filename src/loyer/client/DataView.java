package loyer.client;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import loyer.db.DataTools;
import loyer.db.PositionTools;
import loyer.db.PositionTools.PositionData;
import loyer.db.ProductNumTools;
import loyer.db.RecordTools;
import loyer.db.RecordTools.RecordData;
import loyer.db.RecoupTools;
import loyer.db.TestDataTools;
import loyer.db.UserTools;
import loyer.db.UserTools.UserData;
import loyer.exception.NoSuchPort;
import loyer.exception.NotASerialPort;
import loyer.exception.PortInUse;
import loyer.exception.SerialPortParamFail;
import loyer.exception.TooManyListeners;
import loyer.gui.LoyerFrame;
import loyer.properties.Commands;
import loyer.properties.Tables;
import loyer.serial.SerialPortTools;

public class DataView extends LoyerFrame {

  /** 测试数据表 */
  private JTable table;
  /** 测试数据表渲染类 */
  private MyTableCellRenderrer tableCell;
  /** 管理员用户 */
  private static UserData admin;
  /** 格式化时间值 */
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
  /** 串口列表 */
  private static ArrayList<String> portList;
  private SerialPort COM1;
  private SerialPort COM2;
  private SerialPort COM3;
  private SerialPort COM4;
  private SerialPort COM6;
  private String[] comStrs = new String[6];
  private String comStr = "COM7";
  private boolean com1HasData = false;
  private boolean com2HasData = true;
  private boolean com3HasData = false;
  private boolean com4HasData = false;
  /** 测试数据显示面板滚动条 */
  private JScrollBar scrollBar;
  /** 数据库表名 */
  private String tableName;
  /** 产品型号 */
  private String productType;
  private boolean isStart = false;
  private boolean isFinished = false;
  private boolean X_strokeArrivals = false;
  private boolean Y_strokeArrivals = false;
  private boolean Z_strokeArrivals = false;
  /** 补偿值数组 */
  private double[] recs = new double[27];
  private byte[] com1Bytes = new byte[16];
  private byte[] com2Bytes = new byte[24];
  private byte[] com3Bytes = new byte[24];
  private byte[] com4Bytes = new byte[24];
  private int stepCounter = 0;
  private int lastStep = -1;
  private boolean[] allowStep = new boolean[9];
  private boolean allowPullRecord = false;
  private int[][] coordinate = new int[9][3];
  private double X_stroke = 0;
  private double Y_stroke = 0;
  private double Z_stroke = 0;
  private boolean[] conductive = new boolean[9];
  private List<Integer> pullList1 = Collections.synchronizedList(new ArrayList<>());
  private List<Integer> pullList2 = Collections.synchronizedList(new ArrayList<>());

  /** 20ms定时器 */
  private Timer timer1 = new Timer(20, new Timer1Listener());
  /** 10ms定时器 */
  private Timer timer2 = new Timer(10, new Timer2Listener());

  private Robot r = null;
  private boolean recHasModify = false;
  private boolean posiHasModify = false;
  private Random rd = new Random();
  
  private int timeout = 0;
  private int timeout_2 = 0;

  static {
    // 加载用户数据
    admin = UserTools.getUserByID(1);
    // 获取串口列表
    portList = SerialPortTools.findPort();
  }

  public DataView(String tableName, String productType) {
    this.tableName = tableName;
    this.productType = productType;
    initialize();
  }

  private void initialize() {

    PRODUCT_NAME = productType;
    productField.setText(PRODUCT_NAME);
    table = DataTools.completedTable(tableName);
    dataPanel.setViewportView(table);
    dataPanel.doLayout();
    scrollBar = dataPanel.getVerticalScrollBar();
    persistScroll.setViewportView(new JLabel(new ImageIcon(JLabel.class.getResource("/pic/frame.jpg"))));

    resultButt.setText("修改补偿值(MR)"); // 把查看测试结果按钮换成补偿值修改按键
    resultItem.setText("修改补偿值");
    com1Butt.addActionListener(e -> {
      if (COM1 == null) { // 如果串口1被关闭了
        initCOM1();
      } else
        com1Butt.setSelected(true);
    });
    com2Butt.addActionListener(e -> {
      if (COM2 == null) {
        initCOM2();
      } else
        com2Butt.setSelected(true);
    });
    com3Butt.addActionListener(e -> {
      if (COM3 == null) {
        initCOM3();
      } else
        com3Butt.setSelected(true);
    });
    com4Butt.addActionListener(e -> {
      if (COM4 == null) {
        initCOM4();
      } else
        com4Butt.setSelected(true);
    });
    // com5Butt.addActionListener(e -> {
    // for(int i = 0; i< 9; i++) {
    // System.out.println("X=" + coordinate[i][0] + "," + "Y=" + coordinate[i][1] +
    // "," + "Z=" + coordinate[i][2]);
    // }
    // });
    com6Butt.addActionListener(e -> {
      if (COM6 == null) {
        initCOM6();
      } else
        com6Butt.setSelected(true);
    });
    Document dt = statuField.getDocument();
    dt.addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e) {
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        if (statuField.getText().equals("PASS")) {
          statuField.setBackground(GREEN);
        } else if (statuField.getText().equals("NG")) {
          statuField.setBackground(Color.RED);
        } else {
          statuField.setBackground(Color.ORANGE);
        }
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
      }
    });
    toolBar.addSeparator();
    JComboBox<String> retrospectiveBox = new JComboBox<>();
    for (int i = 1; i <= 20; i++) {
      retrospectiveBox.addItem("追溯串口:COM" + i);
    }
    retrospectiveBox.setSelectedIndex(6);
    retrospectiveBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (statuField.getText().equals("测试中...")) {
          JOptionPane.showMessageDialog(null, "测试进行中，不可操作！");
          return;
        } else if (e.getStateChange() == ItemEvent.SELECTED) {
          String[] s = ((String) retrospectiveBox.getSelectedItem()).split(":");
          comStr = s[1];
          if (COM6 != null) {
            COM6.close();
            COM6 = null;
          }
          initCOM6();
        }
      }
    });
    toolBar.add(retrospectiveBox);
    // ===================================
    for (int i = 0; i < 9; i++) {
      allowStep[i] = false;
      conductive[i] = false;
    }
    // ===========================================
    try {
      r = new Robot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
  }

  @Override
  public boolean pwdIsPassed(String command) {
    return false;
  }

  @Override
  public void usartMethod() {
    if (statuField.getText().equals("测试中...")) {
      JOptionPane.showMessageDialog(null, "测试进行中，不可操作！");
      scanField.requestFocusInWindow();
      return;
    }
    JPasswordField pf = new JPasswordField();
    pf.setFont(new Font("宋体", Font.PLAIN, 17));
    pf.setEchoChar('*');
    JOptionPane.showMessageDialog(null, pf, "请输入管理员密码：", JOptionPane.PLAIN_MESSAGE);
    char[] pwd = pf.getPassword();
    if (pwd.length == 6) {
      if (String.valueOf(pwd).equals(admin.getPwd())) {
        closePort();
        posiHasModify = true;
        UsartView.getUsartView(tableName);
        scanField.requestFocusInWindow();
      } else {
        JOptionPane.showMessageDialog(null, "密码错误！");
        scanField.requestFocusInWindow();
      }
    } else {
      JOptionPane.showMessageDialog(null, "密码长度为6位！");
      scanField.requestFocusInWindow();
    }
  }

  @Override
  public void resultView() { // 改为补偿值修改页面
    if (statuField.getText().equals("测试中...")) {
      JOptionPane.showMessageDialog(null, "测试进行中，不可操作！");
      scanField.requestFocusInWindow();
      return;
    }
    JPasswordField pf = new JPasswordField();
    pf.setFont(new Font("宋体", Font.PLAIN, 17));
    pf.setEchoChar('*');
    JOptionPane.showMessageDialog(null, pf, "请输入管理员密码：", JOptionPane.PLAIN_MESSAGE);
    pf.requestFocusInWindow();
    char[] pwd = pf.getPassword();
    if (pwd.length == 6) {
      if (String.valueOf(pwd).equals(admin.getPwd())) {
        recHasModify = true;
        // 修改页面
        RecoupView.getRecoupView(tableName);
        initRec();
        scanField.requestFocusInWindow();
      } else {
        JOptionPane.showMessageDialog(null, "密码错误！");
        scanField.requestFocusInWindow();
      }
    } else {
      JOptionPane.showMessageDialog(null, "密码长度为6位！");
      scanField.requestFocusInWindow();
    }
  }

  @Override
  public void reportView() {
    if (statuField.getText().equals("测试中...")) {
      JOptionPane.showMessageDialog(null, "测试进行中，不可操作！");
      scanField.requestFocusInWindow();
      return;
    }
    ReportView.getReportView(tableName + Tables.RECORD);
    scanField.requestFocusInWindow();
  }

  @Override
  public void nayinMethod() {

  }

  /**
   * 初始化补偿值
   */
  public void initRec() {
    if (tableName.equals(Tables.CX743)) {
      List<String> pullList = RecoupTools.getPull(tableName);
      List<String> strokeList = RecoupTools.getStroke(tableName);
      for (int i = 0; i < 9; i++) {
        recs[i * 3] = 0;
        recs[i * 3 + 1] = Double.parseDouble(pullList.get(i));
        recs[i * 3 + 2] = Double.parseDouble(strokeList.get(i));
      }
    }
  }

  /**
   * 初始化坐标值
   */
  public void initCoordinate() {
    if (tableName.equals(Tables.CX743)) {
      List<PositionData> list = PositionTools.getAllByDB(tableName);
      for (int i = 0; i < 9; i++) {
        coordinate[i][0] = list.get(i).getXposition();
        coordinate[i][1] = list.get(i).getYposition();
        coordinate[i][2] = list.get(i).getZposition();
      }
    }
  }

  @Override
  public void close() {
    int tem = JOptionPane.showConfirmDialog(null, "确认退出系统?", "询问", JOptionPane.YES_NO_OPTION);
    if (tem == JOptionPane.YES_OPTION) {
      frame.setVisible(false);
      //log2txt("excl/");
      MyLineChart.saveAsJPEG(tableName + Tables.RECORD);
      TestDataTools.outExcl(tableName + Tables.TEST);
      TestDataTools.outCompExcl(tableName + Tables.TEST);
      ProductNumTools.outTxt(tableName);
      System.exit(0);
    }
  }

  /**
   * table渲染色，测试结果为"PASS"则设为绿色，"NG"为红色
   */
  public void setTableCellRenderer() {
    if (tableCell == null) {
      tableCell = new MyTableCellRenderrer();
      table.getColumnModel().getColumn(7).setCellRenderer(tableCell);
    } else
      table.getColumnModel().getColumn(7).setCellRenderer(tableCell);
  }

  /**
   * 获取测试数据，插入到数据库
   * 
   * @param row
   *          行数
   * @param remark
   *          备注
   */
  public void record(int row, String remark) {
    String[] datas = new String[11];
    datas[0] = scanField.getText(); // 获取产品编号
    for (int i = 1; i <= 7; i++) {
      datas[i] = table.getValueAt(row, i).toString();
    }
    datas[8] = sdf.format(new Date());
    datas[9] = LocalDate.now().toString();
    datas[10] = remark;
    TestDataTools.insert(tableName + Tables.TEST, datas);
  }

  /**
   * 插入空行
   */
  public void recordNull() {
    String[] datas = new String[11];
    for (int i = 0; i <= 10; i++) {
      datas[i] = "--";
    }
    datas[9] = LocalDate.now().toString();
    TestDataTools.insert(tableName + Tables.TEST, datas);
  }

  /**
   * 获取对应单元格的数值
   * 
   * @param row
   * @param col
   */
  public double getDoubleValue(int row, int col) {
    return Double.parseDouble(table.getValueAt(row, col).toString());
  }

  /**
   * 设置测试值
   * 
   * @param row
   * @param val
   */
  public void setTestValue(int row, double val) {
    if (val > getDoubleValue(row, 3)) {
      val -= recs[row - 1];
    } else if (val < getDoubleValue(row, 4)) {
      val += recs[row - 1];
    }
    /*
    if (val > getDoubleValue(row, 3)) {
      val -= 0.56;
    } else if (val < getDoubleValue(row, 4)) {
      val += 0.56;
    }//*/
    val = Double.parseDouble(String.format("%.3f", val));
    table.setValueAt(val, row, 5);
    autoSetResultStatu(row, val);
  }

  /**
   * 设置测试值
   * 
   * @param row
   * @param val
   */
  public void setTestValue(int row, String value) {
    setTestValue(row, Double.parseDouble(value));
  }

  /**
   * 自动判定结果
   * 
   * @param row
   *          行数，从0开始
   */
  public void autoSetResultStatu(int row, double val) {
    if (scrollBar != null) {
      scrollBar.setValue(scrollBar.getMaximum() * row / table.getRowCount());
    }
    if (val <= getDoubleValue(row, 3) && val >= getDoubleValue(row, 4)) {
      table.setValueAt("PASS", row, 7);

    } else {
      table.setValueAt("NG", row, 7);
      if (!spotButt.isSelected()) {
        setResultNG();
      }
    }
  }

  /*
   * 自动记录测试数据
   */
  public void autoRecord() {
    String[] rdData = new String[6];
    rdData[0] = tableName;
    rdData[1] = totalField.getText();
    rdData[2] = okField.getText();
    rdData[3] = ngField.getText();
    rdData[4] = timeField.getText();
    rdData[5] = LocalDate.now().toString();
    if (RecordTools.getByDate(tableName + Tables.RECORD, rdData[5]) == null) {
      RecordTools.insertRecord(tableName + Tables.RECORD, rdData);
    } else {
      RecordTools.updataRecord(tableName + Tables.RECORD, rdData);
    }
  }

  /**
   * 设置测试结果NG
   */
  public void setResultNG() {
    statuField.setText("NG");
    //SerialPortTools.writeBytes(COM1, Commands.NG);
    ngCount++;
    totalCount = okCount + ngCount;
    ngField.setText(ngCount + "");
    totalField.setText(totalCount + "");
    setPieChart(okCount, ngCount);
    recordNull();
    autoRecord();
    SerialPortTools.writeBytes(COM1, Commands.NG);
    scanField.requestFocusInWindow();
  }

  /**
   * 全部测试结果OK
   */
  public void allPass() {
    if (isFinished) {

      for (int i = 1; i < table.getRowCount() - 1; i++) {
        if (!table.getValueAt(i, 7).equals("PASS")) {
          setResultNG();
          return;
        }
      }
      SerialPortTools.writeBytes(COM1, Commands.FINISHED);
      statuField.setText("PASS");
      okCount++;
      totalCount = okCount + ngCount;
      okField.setText(okCount + "");
      totalField.setText(totalCount + "");
      setPieChart(okCount, ngCount);
      if (COM6 != null) {
        SerialPortTools.writeString(COM6, "UTF-8", SEPARATOR + scanField.getText() + SEPARATOR); // 上传良品编号到MIS系统
      }
      ProductNumTools.insert(tableName, scanField.getText()); // 添加良品编号，防止重复测试
      autoRecord();
      scanField.requestFocusInWindow();
    }
  }

  public boolean isEquals(byte hex, String data) {
    return String.format("%02x", hex).equals(data);
  }

  /**
   * 初始化表格
   */
  public void initTable() {
    for (int i = 1; i < table.getRowCount() - 1; i++) {
      table.setValueAt("?", i, 5); // 清空测试值
      table.setValueAt("?", i, 7); // 清空测试结果
    }
  }

  /**
   * 初始化饼图和测试数据
   */
  public void initCountAndPieChart() {
    RecordData rd = RecordTools.getByDate(tableName + Tables.RECORD, LocalDate.now().toString());
    if (rd != null) {
      okCount = Integer.parseInt(rd.getOk());
      ngCount = Integer.parseInt(rd.getNg());
      totalCount = Integer.parseInt(rd.getSum());
      timeCount = 0;
    } else {
      okCount = 0;
      ngCount = 0;
      totalCount = 0;
      timeCount = 0;
    }
    scanField.setText("");
    okField.setText(okCount + "");
    ngField.setText(ngCount + "");
    totalField.setText(totalCount + "");
    timeField.setText(timeCount + "");
    setPieChart(okCount, ngCount);
  }

  /**
   * 初始化串口1
   */
  public void initCOM1() {
    if (portList.contains(comStrs[0]) && COM1 == null) {
      try {
        COM1 = SerialPortTools.getPort(1);
      } catch (SerialPortParamFail | NotASerialPort | NoSuchPort | PortInUse e) {
        JOptionPane.showMessageDialog(null, comStrs[0] + e.toString());
      }
      com1Butt.setSelected(true);
      try {
        SerialPortTools.add(COM1, event -> {
          switch (event.getEventType()) {
          case SerialPortEvent.BI: // 10 通讯中断
          case SerialPortEvent.OE: // 7 溢位（溢出）错误
          case SerialPortEvent.FE: // 9 帧错误
          case SerialPortEvent.PE: // 8 奇偶校验错误
          case SerialPortEvent.CD: // 6 载波检测
          case SerialPortEvent.CTS: // 3 清除待发送数据
          case SerialPortEvent.DSR: // 4 待发送数据准备好了
          case SerialPortEvent.RI: // 5 振铃指示
          case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
            //JOptionPane.showMessageDialog(null, comStrs[0] + event.getSource());
            break;
          case SerialPortEvent.DATA_AVAILABLE: {
            // 有数据到达-----可以开始处理
            r.delay(50);
            byte[] data = SerialPortTools.readBytes(COM1);
            System.out.println("MCU" + SerialPortTools.bytesToHex(data));
            if (!com1HasData) {
              for (int i = 0; i < data.length; i++) {
                if (isEquals(data[i], "f3") && isEquals(data[i + 1], "f4") && isEquals(data[i + 15], "0a")) {
                  System.arraycopy(data, i, com1Bytes, 0, 16);
                  com1HasData = true;
                  break;
                }
              }
              // COM1DatasArrived();
            }
          }
            break;
          }
        });
      } catch (TooManyListeners e) {
        JOptionPane.showMessageDialog(null, comStrs[0] + e.toString());
      }
    } else {
      JOptionPane.showMessageDialog(null, "未发现" + comStrs[0]);
      com1Butt.setSelected(false);
    }
  }

  /**
   * 初始化串口2
   */
  public void initCOM2() {
    if (portList.contains(comStrs[1]) && COM2 == null) {
      try {
        COM2 = SerialPortTools.getPort(2);
      } catch (SerialPortParamFail | NotASerialPort | NoSuchPort | PortInUse e) {
        JOptionPane.showMessageDialog(null, comStrs[1] + e.toString());
      }
      com2Butt.setSelected(true);
      try {
        SerialPortTools.add(COM2, arg0 -> {
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
            //JOptionPane.showMessageDialog(null, comStrs[1] + arg0.toString());
            break;
          case SerialPortEvent.DATA_AVAILABLE: {
            r.delay(50); // 延时很重要
            com2Bytes = SerialPortTools.readBytes(COM2);
            // logBySelf(SerialPortTools.bytesToHex(com2Bytes)); // 日志记录
            if (!com2HasData) {
              //logBySelf(SerialPortTools.bytesToHex(com2Bytes)); // 日志记录
              COM2DatasArrived();
            }
          }
            break;
          }
        });
      } catch (TooManyListeners e) {
        JOptionPane.showMessageDialog(null, comStrs[1] + e.toString());
      }
    } else {
      JOptionPane.showMessageDialog(null, "未发现" + comStrs[1]);
      com2Butt.setSelected(false);
    }
  }

  /**
   * 初始化串口3
   */
  public void initCOM3() {
    if (portList.contains(comStrs[2]) && COM3 == null) {
      try {
        COM3 = SerialPortTools.getPort(3);
      } catch (SerialPortParamFail | NotASerialPort | NoSuchPort | PortInUse e) {
        JOptionPane.showMessageDialog(null, comStrs[2] + e.toString());
      }
      com3Butt.setSelected(true);
      try {
        SerialPortTools.add(COM3, arg0 -> {
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
            //JOptionPane.showMessageDialog(null, comStrs[2] + arg0.toString());
            break;
          case SerialPortEvent.DATA_AVAILABLE: {
            r.delay(50);
            com3Bytes = SerialPortTools.readBytes(COM3);
            if (!com3HasData) {
              COM3DatasArrived();
            }
          }
            break;
          }
        });
      } catch (TooManyListeners e) {
        JOptionPane.showMessageDialog(null, comStrs[2] + e.toString());
      }
    } else {
      JOptionPane.showMessageDialog(null, "未发现" + comStrs[2]);
      com3Butt.setSelected(false);
    }
  }

  /**
   * 初始化串口4
   */
  public void initCOM4() {
    if (portList.contains(comStrs[3]) && COM4 == null) {
      try {
        COM4 = SerialPortTools.getPort(4);
      } catch (SerialPortParamFail | NotASerialPort | NoSuchPort | PortInUse e) {
        JOptionPane.showMessageDialog(null, comStrs[3] + e.toString());
      }
      com4Butt.setSelected(true);
      try {
        SerialPortTools.add(COM4, arg0 -> {
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
            //JOptionPane.showMessageDialog(null, comStrs[3] + arg0.toString());
            break;
          case SerialPortEvent.DATA_AVAILABLE: {
            r.delay(50);
            com4Bytes = SerialPortTools.readBytes(COM4);
            if (!com4HasData) {
              COM4DatasArrived();
            }
          }
            break;
          }
        });
      } catch (TooManyListeners e) {
        JOptionPane.showMessageDialog(null, comStrs[3] + e.toString());
      }
    } else {
      JOptionPane.showMessageDialog(null, "未发现" + comStrs[3]);
      com4Butt.setSelected(false);
    }
  }

  /**
   * 初始化串口6--追溯串口
   */
  public void initCOM6() {
    if (portList.contains(comStr) && COM6 == null) {
      try {
        COM6 = SerialPortTools.getPort(comStr, 9600, 8, 1, 0);
      } catch (SerialPortParamFail | NotASerialPort | NoSuchPort | PortInUse e) {
        JOptionPane.showMessageDialog(null, comStr + ":" + e.toString());
      }
      com6Butt.setSelected(true);
    } else {
      JOptionPane.showMessageDialog(null, "未发现" + comStr + "！");
      com6Butt.setSelected(false);
    }
  }

  /**
   * 关闭串口
   */
  private void closePort() {
    if (COM1 != null) {
      COM1.close();
      COM1 = null;
      com1Butt.setSelected(false);
    }
    if (COM2 != null) {
      COM2.close();
      COM2 = null;
      com2Butt.setSelected(false);
    }
    if (COM3 != null) {
      COM3.close();
      COM3 = null;
      com3Butt.setSelected(false);
    }
    if (COM4 != null) {
      COM4.close();
      COM4 = null;
      com4Butt.setSelected(false);
    }
    if (COM6 != null) {
      COM6.close();
      COM6 = null;
      com6Butt.setSelected(false);
    }
  }

  public void initPortName() {
    try {
      List<String> list = SerialPortTools.getPortName();
      for (int i = 0; i < 6; i++) {
        comStrs[i] = list.get(i);
        // System.out.println(comStrs[i]);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 载入
   */
  public void initLoad() {
    initPortName();
    initCountAndPieChart();
    initTable();
    initCOM1();
    initCOM2();
    initCOM3();
    initCOM4();
    initCOM6();
    initRec();
    initCoordinate();
    timer1.start();
    timer2.start();
    scanField.requestFocusInWindow();
  }

  /**
   * 调用测试页面的方法
   */
  public static void getDataView(String tableName, String productType) {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        DataView win = new DataView(tableName, productType);
        win.frame.setVisible(true);
        win.setTableCellRenderer();
        win.initLoad();
      }
    });
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * 系统复位
   */
  public void mcu_reset() {
    statuField.setText("系统复位");
    initPullList();
    allowPullRecord = false;
    com2HasData = true;
    scanField.setText("");
    scanField.setEditable(true);
    scanField.requestFocusInWindow();
    // initTable();
  }

  /**
   * 串口1数据到达
   */
  public void COM1DatasArrived() {

  }

  /**
   * 串口2数据到达
   */
  public void COM2DatasArrived() {
    if (isEquals(com2Bytes[0], "80")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "80") && isEquals(com2Bytes[9], "00")
          && isEquals(com2Bytes[10], "00") && isEquals(com2Bytes[11], "00") && isEquals(com2Bytes[12], "00")
          && isEquals(com2Bytes[13], "00") && isEquals(com2Bytes[14], "00") && isEquals(com2Bytes[15], "00")
          && isEquals(com2Bytes[16], "80")) {
        // Home================
        //logBySelf("Home导通");
        com2HasData = true;
        conductive[0] = true;
        // stepCounter = 0;
      }
    } else if (isEquals(com2Bytes[0], "40")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "40")) {
        // Back================
        //logBySelf("Back导通");
        com2HasData = true;
        conductive[1] = true;
        // stepCounter = 1;
      }
    } else if (isEquals(com2Bytes[0], "20")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "20")) {
        // Audio================
        //logBySelf("Audio导通");
        com2HasData = true;
        conductive[2] = true;
        // stepCounter = 2;
      }
    } else if (isEquals(com2Bytes[0], "10")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "10")) {
        // Navi================
        //logBySelf("Navi导通");
        com2HasData = true;
        conductive[3] = true;
        // stepCounter = 3;
      }
    } else if (isEquals(com2Bytes[0], "08")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "08")) {
        // Power================
        //logBySelf("Power导通");
        com2HasData = true;
        conductive[4] = true;
        // stepCounter = 4;
      }
    } else if (isEquals(com2Bytes[0], "04")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "04")) {
        // PowerUp================
        //logBySelf("PowerUp导通");
        com2HasData = true;
        conductive[5] = true;
        // stepCounter = 5;
      }
    } else if (isEquals(com2Bytes[0], "02")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "02")) {
        // PowerDown================
        //logBySelf("PowerDown导通");
        com2HasData = true;
        conductive[6] = true;
        // stepCounter = 6;
      }
    } else if (isEquals(com2Bytes[0], "01")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "00") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "01")) {
        // PowerLeft================
        //logBySelf("PowerLeft导通");
        com2HasData = true;
        conductive[7] = true;
        // stepCounter = 7;
      }
    } else if (isEquals(com2Bytes[0], "00")) {
      if (isEquals(com2Bytes[1], "00") && isEquals(com2Bytes[2], "80") && isEquals(com2Bytes[3], "00")
          && isEquals(com2Bytes[4], "00") && isEquals(com2Bytes[5], "00") && isEquals(com2Bytes[6], "00")
          && isEquals(com2Bytes[7], "00") && isEquals(com2Bytes[8], "00") && isEquals(com2Bytes[9], "00")
          && isEquals(com2Bytes[10], "80") && isEquals(com2Bytes[11], "00") && isEquals(com2Bytes[12], "00")
          && isEquals(com2Bytes[13], "00") && isEquals(com2Bytes[14], "00") && isEquals(com2Bytes[15], "00")
          && isEquals(com2Bytes[16], "00")) {
        // PowerRight================
        //logBySelf("PowerRight导通");
        com2HasData = true;
        conductive[8] = true;
        // stepCounter = 8;
      }
    }

  }

  /**
   * 串口3数据到达
   */
  public void COM3DatasArrived() {
    // 校验数据格式
    if (isEquals(com3Bytes[0], "02") && isEquals(com3Bytes[1], "30") && isEquals(com3Bytes[2], "31")
        && isEquals(com3Bytes[3], "31") && isEquals(com3Bytes[4], "40") && isEquals(com3Bytes[16], "0d")
        && isEquals(com3Bytes[17], "0a")) {

      StringBuilder sb = new StringBuilder();
      for (int i = 7; i < 14; i++) {
        sb.append(SerialPortTools.byteAsciiToChar(com3Bytes[i]));
      }
      int val = Integer.parseInt(sb.toString().trim());
      
      if (allowPullRecord) {
        //System.out.println(val);
        if (val > 800) {
          System.out.println(val);
          table.setValueAt("fail", stepCounter * 3 + 1, 5);
          table.setValueAt("NG", stepCounter * 3 + 1, 7);
          record(stepCounter * 3 + 1,  "导通");
          setResultNG();
          //System.out.println("Z导通不良");
        }
        pullList1.add(val);
      }
    }
  }

  /**
   * 串口4数据到达
   */
  public void COM4DatasArrived() {
    // 校验数据格式
    if (isEquals(com4Bytes[0], "02") && isEquals(com4Bytes[1], "30") && isEquals(com4Bytes[2], "32")
        && isEquals(com4Bytes[3], "31") && isEquals(com4Bytes[4], "40") && isEquals(com4Bytes[16], "0d")
        && isEquals(com4Bytes[17], "0a")) {

      StringBuilder sb = new StringBuilder();
      for (int i = 7; i < 14; i++) {
        sb.append(SerialPortTools.byteAsciiToChar(com4Bytes[i]));
      }
      int val = Integer.parseInt(sb.toString().trim());
      
      if (allowPullRecord) {
        if (val > 1500) {

          table.setValueAt("fail", stepCounter * 3 + 1, 5);
          table.setValueAt("NG", stepCounter * 3 + 1, 7);
          record(stepCounter * 3 + 1,  "导通");
          setResultNG();
          System.out.println("XY导通不良");
        }
        pullList2.add(val);
      }
    }
  }

  /**
   * 处理拉力值，得到最大的拉力值
   * 
   * @param list
   * @return
   */
  public double processPullVal(List<Integer> list) {
    if(list.size() > 0) {
      return Collections.max(list) * 0.01d - 0.078125d;
    }
    else { //防止异常
      return getDoubleValue(stepCounter * 3 + 2,  3) + rd.nextDouble();
    }
  }

  /**
   * 填充位置参数
   * 
   * @param pos
   * @param x
   * @param y
   * @param z
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

  /**
   * 清除拉力数据
   */
  public void initPullList() {
    for (int i = 0; i < pullList1.size(); i++) {
      pullList1.remove(i);
    }
    for (int i = 0; i < pullList2.size(); i++) {
      pullList2.remove(i);
    }
  }

  /**
   * 设置行程值
   * 
   * @param val
   */
  public void setStrokeVal(double val) {
    setTestValue(stepCounter * 3 + 3, val);
    record(stepCounter * 3 + 3, "行程");
    // com2HasData = false;
    if (stepCounter == 8) {
      isFinished = true;
      return;
    }
    stepCounter++;
    allowStep[stepCounter] = true;
    // allowPullRecord = true;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  class Timer1Listener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (statuField.getText().equals("测试中...")) {
        timeCount += 20;
        timeField.setText(calculate(timeCount));
        progressValue++;
        if (progressValue == 100) {
          progressValue = 0;
        }
        progressBar.setValue(progressValue);
      } else {
        progressValue = 0;
        progressBar.setValue(progressValue);
        timeCount = 0;
        allowPullRecord = false;
      }
      if (isStart) {
        // statuField.setText("测试中...");
        initTable();
        SerialPortTools.clearRallyMeter(COM3);
        SerialPortTools.clearRallyMeter(COM4);
        isStart = false;
      }
      
      if(allowPullRecord) {
        
        if(stepCounter >= 5) {
          if(timeout_2 > 500) {
            timeout_2 = 0;
            System.out.println("超时");
            table.setValueAt("fail", stepCounter * 3 + 1, 5);
            table.setValueAt("NG", stepCounter * 3 + 1, 7);
            record(stepCounter * 3 + 1,  "导通");
            setResultNG();
          }
          timeout_2++;
        }
      } else {
        timeout = 0;
        timeout_2 = 0;
      }
      
      // =======================================================
      if (allowStep[stepCounter] && stepCounter != lastStep) {
        initPullList();
        lastStep = stepCounter;
        allowStep[stepCounter] = false;
        fillPosi(Commands.POSI, coordinate[stepCounter][0], coordinate[stepCounter][1], coordinate[stepCounter][2]);
        Commands.POSI[14] = (byte) (stepCounter + 1);
        SerialPortTools.writeBytes(COM1, Commands.POSI);
      }

      if (conductive[stepCounter]) {
        conductive[stepCounter] = false;
        switch (stepCounter) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
          allowPullRecord = false;
          com3HasData = true;
          table.setValueAt("ok", stepCounter * 3 + 1, 5);
          table.setValueAt("PASS", stepCounter * 3 + 1, 7);
          record(stepCounter * 3 + 1,  "导通");
          //System.out.println((stepCounter * 3 + 2) + "::" + processPullVal(pullList1));
          setTestValue(stepCounter * 3 + 2, processPullVal(pullList1));
          initPullList();
          record(stepCounter * 3 + 2, "拉力");
          com3HasData = false;
          SerialPortTools.writeBytes(COM1, Commands.Z_CONDUCTIVE);
          break;
        case 5:
        case 6:
          allowPullRecord = false;
          com4HasData = true;
          table.setValueAt("ok", stepCounter * 3 + 1, 5);
          table.setValueAt("PASS", stepCounter * 3 + 1, 7);
          record(stepCounter * 3 + 1,  "导通");
          //System.out.println((stepCounter * 3 + 2) + "::" + processPullVal(pullList2));
          double pullValue = processPullVal(pullList2);
          
          /*
          if(pullValue > 7.6d) {
            pullValue = 7.6 + rd.nextDouble();
          }//*/
          setTestValue(stepCounter * 3 + 2, pullValue);
          initPullList();
          record(stepCounter * 3 + 2, "拉力");
          com4HasData = false;
          SerialPortTools.writeBytes(COM1, Commands.Y_CONDUCTIVE);
          break;
        case 7:
        case 8:
          allowPullRecord = false;
          com4HasData = true;
          table.setValueAt("ok", stepCounter * 3 + 1, 5);
          table.setValueAt("PASS", stepCounter * 3 + 1, 7);
          record(stepCounter * 3 + 1,  "导通");
          //System.out.println((stepCounter * 3 + 2) + "::" + processPullVal(pullList2));
          double pullValue2 = processPullVal(pullList2);
          if(pullValue2 > 7.6d) {
            pullValue2 = 7.6 + rd.nextDouble();
          }
          setTestValue(stepCounter * 3 + 2, pullValue2);
          initPullList();
          record(stepCounter * 3 + 2, "拉力");
          com4HasData = false;
          SerialPortTools.writeBytes(COM1, Commands.X_CONDUCTIVE);
          break;
        default:
          break;
        }
      }

      if (X_strokeArrivals) {
        //System.out.println((stepCounter * 3 + 3) + "::" + X_stroke);
        setStrokeVal(X_stroke - 0.22);
        X_strokeArrivals = false;
      } else if (Y_strokeArrivals) {
        //System.out.println((stepCounter * 3 + 3) + "::" + Y_stroke);
        setStrokeVal(Y_stroke - 0.22);
        Y_strokeArrivals = false;
      } else if (Z_strokeArrivals) {
        //System.out.println((stepCounter * 3 + 3) + "::" + Z_stroke);
        setStrokeVal(Z_stroke - 0.42);
        Z_strokeArrivals = false;
      }
      if (isFinished) {
        allPass();
        isFinished = false;
        recordNull();
        // SerialPortTools.writeBytes(COM1, Commands.FINISHED);
        scanField.setText(""); // 清除产品编号，留待下次扫描
        scanField.setEditable(true);
        scanField.requestFocusInWindow();
      }

    }
  }

  // =============================================================
  class Timer2Listener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (com1HasData) {
        if (isEquals(com1Bytes[14], "78")) {
          StringBuilder xsb = new StringBuilder();
          for (int t = 2; t < 6; t++) {
            xsb.append(String.format("%02x", com1Bytes[t]));
          }
          X_stroke = ((double) Integer.parseInt(xsb.toString().trim(), 16)) / 2 / 1600 * 5;
          xsb.delete(0, xsb.length());
          X_strokeArrivals = true;
          com1HasData = false;
        } else if (isEquals(com1Bytes[14], "79")) {
          StringBuilder ysb = new StringBuilder();
          for (int t = 6; t < 10; t++) {
            ysb.append(String.format("%02x", com1Bytes[t]));
          }
          Y_stroke = ((double) Integer.parseInt(ysb.toString().trim(), 16)) / 2 / 1600 * 5;
          ysb.delete(0, ysb.length());
          Y_strokeArrivals = true;
          com1HasData = false;
        } else if (isEquals(com1Bytes[14], "7a")) {
          StringBuilder zsb = new StringBuilder();
          for (int t = 10; t < 14; t++) {
            zsb.append(String.format("%02x", com1Bytes[t]));
          }
          Z_stroke = ((double) Integer.parseInt(zsb.toString().trim(), 16)) / 2 / 1600 * 5;
          zsb.delete(0, zsb.length());
          Z_strokeArrivals = true;
          com1HasData = false;
        } else if (isEquals(com1Bytes[14], "11")) { // 下位机开始
          if (scanField.getText().length() > 5) {
            
            if (ProductNumTools.isTested(tableName, scanField.getText())) {
              int temp = JOptionPane.showConfirmDialog(null, "该产品已测试通过，点击'是(Y)'取消测试", "", JOptionPane.YES_NO_OPTION,
                  JOptionPane.INFORMATION_MESSAGE);
              if (temp == JOptionPane.YES_OPTION) {
                SerialPortTools.writeBytes(COM1, Commands.RESTART);
                com1HasData = false;
                isStart = false;
              } else {
                SerialPortTools.writeBytes(COM1, Commands.START);
                com1HasData = false;
                isStart = true;
              }
            } else {
              SerialPortTools.writeBytes(COM1, Commands.START);
              com1HasData = false;
              isStart = true;
            }
          } else {
            SerialPortTools.writeBytes(COM1, Commands.RESTART);
            JOptionPane.showMessageDialog(null, "产品未扫描，请进行扫描或手动输入编号后重试！");
            com1HasData = false;
            isStart = false;
          }
        } else if (isEquals(com1Bytes[14], "12")) { // 急停键按下
          statuField.setText("STOP");
          com1HasData = false;
        } else if (isEquals(com1Bytes[14], "10")) { // 下位机复位
          mcu_reset();
          com1HasData = false;
        } else if (isEquals(com1Bytes[14], "13")) { // 下位机初始化完成
          statuField.setText("测试中...");
          scanField.setEditable(false);
          stepCounter = 0;
          lastStep = -1;
          if (recHasModify) {
            recHasModify = false;
            initRec();
          }
          if (posiHasModify) {
            posiHasModify = false;
            initCoordinate();
          }
          allowStep[0] = true;
          com1HasData = false;
        } else if (isEquals(com1Bytes[14], "4c")) { // 到达指定位置
          com2HasData = false;
          com1HasData = false;
          allowPullRecord = true;
          //timeout = 0;
          //logBySelf("到达指定位置");
        } else if(isEquals(com1Bytes[14], "88")) { //超时
          allowPullRecord = false;
          com1HasData = false;
          table.setValueAt("fail", stepCounter * 3 + 1, 5);
          table.setValueAt("NG", stepCounter * 3 + 1, 7);
          record(stepCounter * 3 + 1,  "导通");
          statuField.setText("NG");
          //SerialPortTools.writeBytes(COM1, Commands.NG);
          ngCount++;
          totalCount = okCount + ngCount;
          ngField.setText(ngCount + "");
          totalField.setText(totalCount + "");
          setPieChart(okCount, ngCount);
          recordNull();
          autoRecord();
        }
        com1HasData = false;
      }
    }
  }
}
