import java.util.List;
import java.util.ArrayList;

class listTest {
	public static void main(String[] args) {
		List<int[]> l = new ArrayList<int[]>(2);
		int[] a = {1,2};
		l.add(a);
		int[] b = {4,5};
		l.add(b);
		for(int i = 0; i < l.get(0).length; i ++) l.get(0)[i] = l.get(1)[i];
		System.out.println(l.get(0)[0]);
		System.out.println(l.get(0)[1]);
	}
}
