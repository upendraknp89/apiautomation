package apiautomation.apiautomation;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestCountryAPI extends TestBase{
	String baseURL;
	String userName;
	String password;
	String statusCode;
	String postStatusCode;
	String countryDomain;
	int rowSize;
	String[] countries;
	String inExistentMessage;
	/**
	 * We know that each API consist 3 kind of information like BaseURL,Domain and parameters
	 * so as per my approach first we will initialize base url and as per required will append the domain a
	 * and fetch the record. and read all the static data from property file and expected data from excel file.
	 */

	//This will be used to get the whole Excel Sheet Data
	private enum ColumnData{
		COUNTRIES(0),
		INEXISTENT(1),
		THIRDINDEX(2);	
		int index;
		private ColumnData(int index) {
			this.index = index;
		}
		public int getIndex() {
			return index;
		}
	}

	//This method will use to initialize to get basic details from property file
	@BeforeClass
	public void actionTestBefore() {
		initialize();
		Object [][] countryAPIData 	= getData(datatable, this.getClass().getSimpleName());
		assertNotNull("Test Country API Data is not available", countryAPIData);
		this.baseURL                       = config.getProperty("base.url");
		this.userName                      = config.getProperty("user");
		this.password                      = config.getProperty("password");
		this.statusCode                    = config.getProperty("ok.status.code");
		this.postStatusCode                = config.getProperty("post.success.code");
		this.countryDomain                 = config.getProperty("country.get.all");
		rowSize                            = countryAPIData.length;
		this.countries                     = new String[rowSize];
		this.inExistentMessage             = String.valueOf(countryAPIData[0][ColumnData.INEXISTENT.getIndex()]);
		//Initialize all data from excel
		for (int row=0;row<rowSize;row++){
			this.countries[row]            = String.valueOf(countryAPIData[row][ColumnData.COUNTRIES.getIndex()]);
		}
		//Remove empty strings from array if any
		countries                          = removeEmptyStringFromArray(countries);
	}

	@Test
	public void getCountryDetails() {
		// Specify the base URL to the RESTful web service
		RestAssured.baseURI             = this.baseURL;
		RequestSpecification httpRequest= RestAssured.given();

		//Do the basic authentication to check authenticity of user and status code of api had hit
		httpRequest.auth().basic(this.userName,this.password).when().then().statusCode(Integer.parseInt(this.statusCode));

		//Get the response from server and store the response in a variable which contains all the country information
		Response response = httpRequest.request(Method.GET,this.countryDomain);
		String bodyAsString=response.getBody().asString();
		//1.validate response which have data of US,DE and GB collectively
		for(int row=0;row<countries.length;row++) {
			assertTrue(bodyAsString.contains(countries[row].trim()), countries[row]+" is not found on entire reponse body");	
		}

		//2.Get each country individually
		List<Country> country=response.jsonPath().getList("country",Country.class);
		Iterator<Country> countryIterator=country.iterator();
		while(countryIterator.hasNext()) {
			Country countryCode=countryIterator.next();
			if(countryCode.alpha2_code.trim().equals(countries[ColumnData.COUNTRIES.getIndex()].trim())) {
				System.out.println(ColumnData.COUNTRIES.getIndex()+" country is found on entire reponse body individually");
			}else if(countryCode.alpha2_code.trim().equals(countries[ColumnData.INEXISTENT.getIndex()].trim())) {
				System.out.println(ColumnData.INEXISTENT.getIndex()+" country is found on entire reponse body individually");
			}else if(countryCode.alpha2_code.trim().equals(countries[ColumnData.THIRDINDEX.getIndex()].trim())) {
				System.out.println(ColumnData.THIRDINDEX.getIndex()+" country is found on entire reponse body individually");
			}
		}

		//3.Get the response for non existent countries and validate them
		response = httpRequest.request(Method.GET,this.countryDomain);
		List<Message> message=response.jsonPath().getList("message",Message.class);
		while(message.iterator().hasNext()) {
			Message messageText=message.iterator().next();
			assertTrue(messageText.message.trim().contains(inExistentMessage), "Resopnse body for inexistent message does not exist!");
		}

		//4.Creating post request to add new country
		JSONObject requestParams = new JSONObject();
		HashMap<String,String> countryData= new HashMap<String,String>();
		//forming the data for new country in a collection 
		countryData.put("name", "INDIA");
		countryData.put("alpha2_code", "IN");
		countryData.put("alpha3_code", "IND");
		requestParams.put("result", countryData);
		// Add a header stating the Request body is a JSON
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toString());
		response=httpRequest.post(this.countryDomain);
		int statusCode = response.getStatusCode();
		assertEquals(statusCode, this.postStatusCode);
	}
}
