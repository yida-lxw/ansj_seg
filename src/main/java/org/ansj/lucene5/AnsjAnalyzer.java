package org.ansj.lucene5;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public class AnsjAnalyzer extends Analyzer {
	/**是否分析词干.进行单复数,时态的转换*/
	private boolean pstemming;
	/**自定义停用词*/
	private Set<String> filter;
	/**自定义停用词加载路径*/
	private String stopwordsDir;
	/**是否查询分词*/
	private boolean query;

	/**
	 * @param filter
	 *            停用词
	 * @param pstemming
	 *            是否分析词干
	 */
	public AnsjAnalyzer(Set<String> filter, boolean pstemming) {
		this.filter = filter;
		this.pstemming = pstemming;
	}
	
	public AnsjAnalyzer(String stopwordsDir, boolean pstemming) {
		this.stopwordsDir = stopwordsDir;
		this.pstemming = pstemming;
	}
	
	public AnsjAnalyzer(String stopwordsDir, boolean pstemming,boolean query) {
		this.stopwordsDir = stopwordsDir;
		this.pstemming = pstemming;
		this.query = query;
	}
	
	public AnsjAnalyzer(boolean pstemming,boolean query) {
		this.query = query;
		this.pstemming = pstemming;
	}
	
	public AnsjAnalyzer(String stopwordsDir) {
		this.stopwordsDir = stopwordsDir;
	}
	
	public AnsjAnalyzer(Set<String> filter) {
		this.filter = filter;
	}

	/**
	 * @param pstemming
	 *            是否分析词干.进行单复数,时态的转换
	 */
	public AnsjAnalyzer(boolean pstemming) {
		this.pstemming = pstemming;
	}

	public AnsjAnalyzer() {
		super();
	}

	@Override
	protected TokenStreamComponents createComponents(String text) {
		Reader reader = new BufferedReader(new StringReader(text));
		Tokenizer tokenizer = null;
		if(query) {
			if(null != stopwordsDir && !"".equals(stopwordsDir)) {
				tokenizer = new AnsjTokenizer(new ToAnalysis(reader), stopwordsDir, pstemming);
			} else {
				tokenizer = new AnsjTokenizer(new ToAnalysis(reader), filter, pstemming);
			}
		} else {
			if(null != stopwordsDir && !"".equals(stopwordsDir)) {
				tokenizer = new AnsjTokenizer(new IndexAnalysis(reader), stopwordsDir, pstemming);
			} else {
				tokenizer = new AnsjTokenizer(new IndexAnalysis(reader), filter, pstemming);
			}
		}
		return new TokenStreamComponents(tokenizer);
	}
}
