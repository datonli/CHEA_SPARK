package mop;

import java.io.IOException;
import utilities.WrongRemindException;

public interface DataOperator {
	public String mop2Str();
	public void str2Mop(String popStr) throws WrongRemindException;
	public boolean write2File(String filename,String str) throws IOException;
}
