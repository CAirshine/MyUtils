package utils;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

public class MySummariserLogsUtils {

	public static void main(String[] args) {

		logToPic("log.log");
	}

	public static void logToPic(String filePath) {

		try {

			TreeMap<String, ArrayList<Double>> total = new TreeMap<String, ArrayList<Double>>();
			TreeMap<String, ArrayList<Double>> delta = new TreeMap<String, ArrayList<Double>>();

			BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
			String line = "";

			// 分别用来匹配report所属的线程组与TPS数据
			Pattern reportPattern = Pattern.compile("\\[\\S+\\]");
			Pattern tpsPattern = Pattern.compile("\\d+.\\d*/s");

			while ((line = reader.readLine()) != null) {
				if (line.contains("+")) {
					Matcher matcher_1 = reportPattern.matcher(line);
					if (matcher_1.find()) {
						Matcher matcher_2 = tpsPattern.matcher(line);
						if (matcher_2.find()) {
							ArrayList<Double> tpsArry = delta.get(matcher_1.group());
							if (tpsArry == null) {
								tpsArry = new ArrayList<Double>();
								delta.put(matcher_1.group(), tpsArry);
							}
							tpsArry.add(Double.parseDouble(matcher_2.group().replace("/s", "")));
						}
					}
				} else if (line.contains("=")) {
					Matcher matcher_1 = reportPattern.matcher(line);
					if (matcher_1.find()) {
						Matcher matcher_2 = tpsPattern.matcher(line);
						if (matcher_2.find()) {
							ArrayList<Double> tpsArry = total.get(matcher_1.group());
							if (tpsArry == null) {
								tpsArry = new ArrayList<Double>();
								total.put(matcher_1.group(), tpsArry);
							}
							tpsArry.add(Double.parseDouble(matcher_2.group().replace("/s", "")));
						}
					}
				}
			}
			reader.close();

			handleInfo(total, "Total");
			handleInfo(delta, "Delta");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void handleInfo(TreeMap<String, ArrayList<Double>> map, String picName) {

		System.out.println("-----------------------" + picName + " start-----------------------");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			Set<Map.Entry<String, ArrayList<Double>>> entries = map.entrySet();
			Iterator<Entry<String, ArrayList<Double>>> iterator = entries.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, ArrayList<Double>> entry = iterator.next();
				System.out.println(entry.getKey());
				for (int i = 0; i < entry.getValue().size(); i++) {
					System.out.println(entry.getValue().get(i));
					dataset.addValue(entry.getValue().get(i), entry.getKey(), i + "");
				}
			}

			JFreeChart chart = ChartFactory.createLineChart(picName, "Time", "Tps", dataset, PlotOrientation.VERTICAL,
					true, false, false);
			
			chart.getLegend().setVisible(true);
			chart.getLegend().setPosition(RectangleEdge.RIGHT);
			
			/*
			 * 以下是Copy的
			 * http://blog.csdn.net/lt870730439/article/details/5899502
			 */
			CategoryPlot plot = chart.getCategoryPlot();
//			plot.setBackgroundPaint(Color.white);
			plot.setDomainGridlinesVisible(false); // 设置背景网格线是否可见
			plot.setDomainGridlinePaint(Color.BLACK); // 设置背景网格线颜色
			plot.setRangeGridlinePaint(Color.GRAY);
			plot.setNoDataMessage("没有数据");// 没有数据时显示的文字说明。

			// 数据轴属性部分
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			rangeAxis.setAutoRangeIncludesZero(true); // 自动生成
			rangeAxis.setUpperMargin(0.20);
			rangeAxis.setLabelAngle(Math.PI / 2.0);
			rangeAxis.setAutoRange(false);

			// 数据渲染部分 主要是对折线做操作
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setBaseItemLabelsVisible(true);
//			renderer.setSeriesPaint(0, Color.black); // 设置折线的颜色
			renderer.setBaseShapesFilled(true);
			renderer.setBaseItemLabelsVisible(true);
			renderer.setBasePositiveItemLabelPosition(
					new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			renderer.setBaseItemLabelFont(new Font("Dialog", 1, 14)); // 设置提示折点数据形状
			plot.setRenderer(renderer);

			// 输出图像
			ChartUtilities.saveChartAsJPEG(new File(picName + ".jpg"), chart, 1366, 768);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("-----------------------" + picName + " ended-----------------------");
	}
}
