package cop5556fa17;
import cop5556fa17.AST.Declaration;
import java.util.HashMap;
public class SymbolTable {
	HashMap<String, Declaration> symbTable = new HashMap<>();
	
	public boolean insert(String name, Declaration dec){
		if(symbTable.containsKey(name)){
			return false;
		}
//		System.out.println("Inserting:" + name);
		symbTable.put(name, dec);
		return true;
	}
	public Declaration lookUp(String name){
		if(!symbTable.containsKey(name)) return null;
//		System.out.println("LookingUp:" + name);
		return symbTable.get(name);
	}
}
