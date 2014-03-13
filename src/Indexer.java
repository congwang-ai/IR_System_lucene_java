import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

//The code structrue is similar from the lucene tutorial in the book  "lucene in action"
public class Indexer {

	private IndexWriter writer;
	private static StandardAnalyzer analyzer = new StandardAnalyzer(
			Version.LUCENE_47);

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		Indexer indexer = new Indexer("indexed/");
		int numIndexed;
		try {
			numIndexed = indexer.index("data/", new TextFilesFilter());
		} finally {
			indexer.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Indexing " + numIndexed + " files took "
				+ (end - start) + " milliseconds");
	}

	public Indexer(String indexDir) throws IOException {
		FSDirectory dir = FSDirectory.open(new File(indexDir));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);
		writer = new IndexWriter(dir, config);
	}

	public int index(String dataDir, FileFilter filter) throws Exception {
		File[] files = new File(dataDir).listFiles();
		for (File f : files) {
			if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()
					&& (filter == null || filter.accept(f))) {
				indexFile(f);
			}
		}
		return writer.numDocs();
	}

	public void close() throws IOException {
		writer.close();
	}

	private static class TextFilesFilter implements FileFilter {
		public boolean accept(File path) {
			return true;
		}
	}

	
	//Here I parse each document, and put them into different fields
	protected Document getDocument(File f) throws Exception {
		Document doc = new Document();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith(".I"))
					continue;
				else if (line.startsWith(".T")) {
					continue;
				} else if (line.startsWith(".A")) {
					doc.add(new TextField("title", sb.toString(),
							Field.Store.YES));
					sb = new StringBuilder();
					continue;
				} else if (line.startsWith(".B")) {
					doc.add(new TextField("author", sb.toString(),
							Field.Store.YES));
					sb = new StringBuilder();
					continue;
				} else if (line.startsWith(".W")) {
					doc.add(new TextField("affiliation", sb.toString(),
							Field.Store.YES));
					sb = new StringBuilder();
					continue;
				}
				sb.append(line.trim());
			}
			doc.add(new TextField("content", sb.toString(), Field.Store.YES));
		}
		doc.add(new StringField("path", f.getPath(), Field.Store.YES));
		doc.add(new StringField("filename", f.getName(), Field.Store.YES));
		return doc;
	}

	private void indexFile(File f) throws Exception {
		System.out.println("Indexing " + f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc);
	}
}
