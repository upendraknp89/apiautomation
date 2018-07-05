package apiautomation.apiautomation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import apiautomation.apiautomation.XlsReader;

/**
 * Automation test base class
 * This class will take care to initialize all the basic initialization.
 * @author upendra
 */
public class TestBase 
{
	private final String CONFIG_PROPERTIES = File.separator+"apiautomation"+File.separator+"apiautomation"+File.separator+"config.properties";//Final so that no body can change the value
	protected  static Properties config;
	private final static String TEST_XLS_PATH = File.separator+"apiautomation"+File.separator+"apiautomation"+File.separator+"TestData1.xlsx";
	protected  static XlsReader datatable;


	/**
	 * to load excel file , this will be used to load the excel file to get the run mode for a test case 
	 */
	public static void loadExcelFiles()
	{
		datatable = new XlsReader(System.getProperty("user.dir") + TEST_XLS_PATH);
	}

	/**
	 * This method will read all the data from excel
	 * @param datatable : Data table object
	 * @param testName : Test case Name
	 * @return
	 */
	public static Object[][] getData(XlsReader datatable, String testName) {

		try {
			if (datatable == null) {
				datatable = new XlsReader(System.getProperty("user.dir")
						+File.separator+"apiautomation"+File.separator+"apiautomation"+File.separator+"TestData1.xlsx");
			}
			int rows = datatable.getRowCount(testName);
			if (rows <= 0) {
				Object[][] testdata = null;
				return testdata;
			}
			rows = datatable.getRowCount(testName);
			int cols = datatable.getColumnCount(testName);
			Object data[][] = new Object[rows - 1][cols];

			for (int rowNum = 2; rowNum <= rows; rowNum++) {
				for (int colNum = 0; colNum < cols; colNum++) {
					data[rowNum - 2][colNum] = datatable.getCellData(testName,
							colNum, rowNum);
				}

			}
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * This method to initialize basic 
	 */
	public void initialize(){
		this.config                  =  new Properties();
		FileInputStream fileInputStr = null;
		try {
			fileInputStr = new FileInputStream(System.getProperty("user.dir") + CONFIG_PROPERTIES);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			config.load(fileInputStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to remove blank strings from array string
	 * @param array
	 * @return
	 */
	public String[] removeEmptyStringFromArray(String[] array)
	{
		List<String> list = new ArrayList<String>();
		for(String s : array) {
			if(s != null && s.length() > 0) {
				list.add(s);
			}
		}
		array = list.toArray(new String[list.size()]);
		return array;
	}


}
