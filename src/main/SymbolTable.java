package main;

import java.util.HashMap;

/**
 * SymbolTable:
 *    key => Symbolname
 *    value => Inner table:
 *      key => NodeID
 *      value => Symbol
 */
public class SymbolTable extends HashMap<String, HashMap<String, Symbol>>{
  public boolean insert(String symbolName, SymbolType type, int size, String nodeID, int lineNumber){
    HashMap<String, Symbol> temp = get(symbolName);
    Symbol tempSymbol = new Symbol(symbolName, type, size, lineNumber, nodeID);
    if(temp == null){
      temp = new HashMap<>();
      temp.put(nodeID, tempSymbol);
      put(symbolName, temp);
    } else if(temp.get(nodeID) == null){
      temp.put(nodeID, tempSymbol);
    } else {
      return false;
    }
    return true;
  }
  public Symbol lookup(String symbolName, String nodeID){
    HashMap<String, Symbol> temp = get(symbolName);
    if(temp == null) return null;
    return temp.get(nodeID);
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    for(String key : keySet()){
      HashMap<String, Symbol> hs = get(key);
      for(String scope : hs.keySet()){
        Symbol s = hs.get(scope);
        sb.append(s.toString()).append("\n");
      }
    }
    return sb.toString();
  }

}
