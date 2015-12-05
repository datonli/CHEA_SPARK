package problems;

public class DTLZ3 extends AProblem{

	
	private static DTLZ3 instance;
	
	private DTLZ3(){
		genesDimesion = 10;
		objectiveDimesion = 3;
		limit = 2;
		range = new int[genesDimesion][limit];
		for(int i = 0; i < genesDimesion; i ++){
			range[i][0] = 0;
			range[i][1] = 1;
		}
	} 
	
	public void evaluate(double[] genes, double[] objValue) {
			
	}

	public static DTLZ3 getInstance() {
		if(instance == null)
			instance = new DTLZ3();
		return instance;
	}
	
}
