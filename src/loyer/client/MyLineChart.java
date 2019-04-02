package loyer.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import loyer.db.RecordTools;
import loyer.db.RecordTools.RecordData;

public class MyLineChart {

  private ChartPanel frame1;
  private DefaultCategoryDataset data;
  private JFreeChart chart;
  private CategoryPlot plot;
  
  public MyLineChart(List<RecordData> list) {
    
    data = getDataSet(list);
    chart = ChartFactory.createLineChart("测试数据统计", // 图表标题
        "", // 主轴标签（x轴）
        "数量", // 范围轴标签（y轴）
        data, // 数据集
        PlotOrientation.VERTICAL, // 方向 
        true, // 是否包含图例
        true, // 提示信息是否显示
        false);// 是否使用urls
    
    chart.getTitle().setFont(new Font("隶书", Font.BOLD, 20)); // 设置标题字体
    chart.getLegend().setItemFont(new Font("黑体", Font.PLAIN, 14));// 设置图例类别字体
    chart.setBackgroundPaint(new Color(245, 245, 245));// 设置背景色
    
    frame1 = new ChartPanel(chart, true);
    
    plot = (CategoryPlot) chart.getPlot();
    plot.setBackgroundPaint(new Color(245, 245, 245)); // 设置绘图区背景色
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // 设置水平方向背景线颜色
    plot.setRangeGridlinesVisible(true);// 设置是否显示水平方向背景线,默认值为true
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // 设置垂直方向背景线颜色
    plot.setDomainGridlinesVisible(true); // 设置是否显示垂直方向背景线,默认值为false**/
    // 背景色 透明度
    plot.setBackgroundAlpha(1.0f);
    // 前景色 透明度
    plot.setForegroundAlpha(1.0f);
    
    LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
    renderer.setBaseShapesVisible(true); // series 点（即数据点）可见
    renderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见
    renderer.setUseSeriesOffset(false); // 设置偏移量
    renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
    renderer.setBaseItemLabelsVisible(true);
    
    //设置折线线条颜色
    renderer.setSeriesPaint(0, Color.RED);
    renderer.setSeriesPaint(1, Color.BLUE);
    renderer.setSeriesPaint(2, new Color(0, 204, 51));
    
    // 设置X轴
    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setLabelFont(new Font("黑体", Font.PLAIN, 12)); // 设置横轴字体
    domainAxis.setTickLabelFont(new Font("黑体", Font.PLAIN, 12));// 设置坐标轴标尺值字体
    domainAxis.setLowerMargin(0.01);// 左边距 边框距离
    domainAxis.setUpperMargin(0.06);// 右边距 边框距离,防止最后边的一个数据靠近了坐标轴。
    domainAxis.setMaximumCategoryLabelLines(10);
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);// 横轴 lable 的位置 横轴上的 Lable 45度倾斜 DOWN_45**/
    
    // 取得纵轴
    NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
    numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    // 设置纵轴的字体
    numberAxis.setLabelFont(new Font("黑体", Font.PLAIN, 15));
    numberAxis.setUpperMargin(0.15);//设置最高数据显示与顶端的距离
    numberAxis.setLowerMargin(2);//设置最低的一个值与图片底端的距离
    
    StandardChartTheme mChartTheme = new StandardChartTheme("CN");
    mChartTheme.setLargeFont(new Font("黑体", Font.PLAIN, 20));
    mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
    mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
    ChartFactory.setChartTheme(mChartTheme);
  }
  
  public DefaultCategoryDataset getDataSet(List<RecordData> list) {
    DefaultCategoryDataset set = new DefaultCategoryDataset();
    for(RecordData data : list) {
      set.setValue(Double.parseDouble(data.getNg()), "NG", data.getDate());
      set.setValue(Double.parseDouble(data.getSum()), "SUM", data.getDate());
      set.setValue(Double.parseDouble(data.getOk()), "OK", data.getDate());
    }
    return set;
  }
  public ChartPanel getChartPanel() {
    return frame1;
  }
  public JFreeChart getJFreeChart() {
    return chart;
  }
  public CategoryPlot getPlot() {
    return plot;
  }
  /**
   * 导出不良记录为Jpg文件
   * @param tableName
   */
  public static void saveAsJPEG(String tableName) {
    MyLineChart lineChart = new MyLineChart(RecordTools.getAllByDB(tableName));
    try {
      String path = "excl/";
      File pathFile = new File(path);
      if(!pathFile.isDirectory()) {
        pathFile.mkdirs();
      }
      //创建可写入的Excel工作簿
      String fileName = tableName + "不良记录"+ LocalDate.now() +".png";
      File file = new File(pathFile, fileName);
      if(!file.exists()) {
        file.createNewFile();
      }
      ChartUtilities.saveChartAsJPEG(file, // 文件保存物理路径包括路径和文件名
          //1.0f, //图片质量 ，0.0f~1.0f
          lineChart.getJFreeChart(), // 图表对象
          Toolkit.getDefaultToolkit().getScreenSize().width, // 图像宽度 ，这个将决定图表的横坐标值是否能完全显示还是显示省略号
          Toolkit.getDefaultToolkit().getScreenSize().height);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "不良记录导出失败:" + e.getLocalizedMessage());
    }
  }
  
}
