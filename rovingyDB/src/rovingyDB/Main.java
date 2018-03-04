/**
 * 
 */
package rovingyDB;


public class Main {

	static Functions f=new Functions();
	
	static bookFunctions bookFunctions=new bookFunctions();
	
	static bookFunctionsEng bookFunctionsEng=new bookFunctionsEng();
	
	static cityFunctions cityFunctions=new cityFunctions();
	
	static footballFunctions footballFunctions=new footballFunctions();
	
	static siirFunctions siirFunctions = new siirFunctions();
	
	static siirFunctionsEng siirFunctionsEng = new siirFunctionsEng();
	
	public static void main(String[] args){
	
		try {
			siirFunctionsEng.readDataFromAuthorID();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	
	
}
