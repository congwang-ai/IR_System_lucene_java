import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

	private static StandardAnalyzer analyzer = new StandardAnalyzer(
			Version.LUCENE_47);
	private HashSet<Integer> res = new HashSet<Integer>();

	//From commmand line, we accepct two arguments, query string, and the number of returned list you want
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("Usage: java "
					+ Searcher.class.getName() + " <index dir> <query>");
		}
	    String q = args[0];
	    int top = Integer.parseInt(args[1]);
		new Searcher().search(q,top);
	}

	
	//accept two arguments, query String s, and the number of returned list (like top 20)
	public void search(String s,int top) throws IOException, ParseException, Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
				"indexed/")));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(top, true);
		try {
			Query titleQuery = new QueryParser(Version.LUCENE_47, "title",
					analyzer).parse(s);
			Query contentQuery = new QueryParser(Version.LUCENE_47, "content",
					analyzer).parse(s);
			
			//Using two query and boolean query to combine them
			//each query is for one field, and we set different weights for two fields.
			titleQuery.setBoost((float) 0.2);
			contentQuery.setBoost((float) 0.8);

			BooleanQuery q = new BooleanQuery();
			q.add(titleQuery, Occur.SHOULD); // or Occur.SHOULD if this clause
												// is optional
			q.add(contentQuery, Occur.SHOULD); // or Occur.MUST if this clause
												// is required

			long start = System.currentTimeMillis();
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				res.add(Integer.parseInt(d.get("path").replace("data\\", "")
						.trim()));
				System.out.println((i + 1) + ". " + d.get("path") + " score="
						+ hits[i].score);
			}

			long end = System.currentTimeMillis();

			System.err.println("Found " + hits.length + " document(s) (in "
					+ (end - start) + " milliseconds) that matched query '" + q
					+ "':");
		} catch (Exception e) {
			System.out.println("Error searching " + s + " : " + e.getMessage());
		}

	}

	public HashSet<Integer> getTopResults() {
		return res;
	}

}
