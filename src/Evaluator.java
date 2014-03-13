import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Evaluator {

	private static ArrayList<String> testQuerys = new ArrayList<String>();
	private static HashMap<Integer, HashSet<Integer>> htQuerys = new HashMap<Integer, HashSet<Integer>>();
	private static Searcher searcher = new Searcher();
	private static ArrayList<Double> precises = new ArrayList<Double>();
	private static ArrayList<Double> recalls = new ArrayList<Double>();

	//load the relevant file into memory for evalution
	private void loadTestQuerys() throws IOException {
		testQuerys.add("dummy_node");

		try (BufferedReader br = new BufferedReader(
				new FileReader("query.text"))) {
			String sb = "";
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith(".I")) {
					if (sb != "") {
						testQuerys.add(sb.toString());
						sb = "";
					}
				} else if (line.startsWith(".W")) {
					continue;
				} else
					sb += line.trim();
			}
			testQuerys.add(sb);
		}

		/**
		 * for(int i=1;i<testQuerys.size();i++)
		 * System.out.println(i+":"+testQuerys.get(i));
		 */
	}

	//load the relevant file into memory for evalution
	private void loadQuerysEval() throws IOException {

		try (BufferedReader br = new BufferedReader(
				new FileReader("qrels.text"))) {
			String line = "";
			int pre = -1;
			HashSet<Integer> cur = new HashSet<Integer>();
			while ((line = br.readLine()) != null) {
				String[] ary = line.split(" ");
				int index = Integer.parseInt(ary[0]);
				if (index != pre) {
					htQuerys.put(pre, cur);
					cur = new HashSet<Integer>();
					pre = index;
				}
				cur.add(Integer.parseInt(ary[1]));
			}
			htQuerys.remove(-1);
			htQuerys.put(pre, cur);
		}
		/**
		 * for(int i : htQuerys.keySet())
		 * System.out.println(i+":"+htQuerys.get(i).toString());
		 */
	}

	//Evaluate from 1 to 20 across the 225 queries, the get the average, the same evaluation method as asg1 
	public void eval() throws Exception {

		loadTestQuerys();
		loadQuerysEval();		
		for (int k = 1; k <= 20; k++) {
			double precise = 0.0;
			double recall = 0.0;
			for (int i = 1; i < testQuerys.size(); i++) {
				searcher.search(testQuerys.get(i), k);
				HashSet<Integer> res = searcher.getTopResults();
				HashSet<Integer> eval = htQuerys.get(i);
				res.retainAll(eval);
				int tp = res.size();
				precise += tp * 1.0 / k;
				recall += tp * 1.0 / eval.size();
			}
			precises.add(k-1,precise/225);
			recalls.add(k-1,recall/225);
		}
		
		System.out.println("Precise:" + precises.toString());
		System.out.println("Recall:" + recalls.toString());
	}

}
