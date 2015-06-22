package org.ansj.lucene.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.domain.TermNatures;
import org.ansj.splitWord.Analysis;
import org.ansj.util.AnsjReader;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeFactory;

public class AnsjTokenizer extends Tokenizer {
	// 当前词
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	// 偏移量
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	// 距离
	private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);

	protected Analysis ta = null;
	/**自定义停用词*/
	private Set<String> filter;
	/**是否分析词干.进行单复数,时态的转换*/
	private boolean pstemming;

	private final PorterStemmer stemmer = new PorterStemmer();

	public AnsjTokenizer(Analysis ta,String stopwordsDir, boolean pstemming) {
		this.ta = ta;
		this.pstemming = pstemming;
		addStopwords(stopwordsDir);
	}
	
	public AnsjTokenizer(Analysis ta, boolean pstemming) {
		this.ta = ta;
		this.pstemming = pstemming;
	}
	
	public AnsjTokenizer(Analysis ta, Set<String> filter, boolean pstemming) {
		this.ta = ta;
		this.filter = filter;
		this.pstemming = pstemming;
	}
	
	public AnsjTokenizer(AttributeFactory factory, Analysis ta, String stopwordsDir, boolean pstemming) {
		super(factory);
		this.ta = ta;
		this.pstemming = pstemming;
		addStopwords(stopwordsDir);
	}
	
	public AnsjTokenizer(AttributeFactory factory, Analysis ta, Set<String> filter, boolean pstemming) {
		super(factory);
		this.ta = ta;
		this.filter = filter;
		this.pstemming = pstemming;
	}
	
	public AnsjTokenizer(AttributeFactory factory, Analysis ta, boolean pstemming) {
		super(factory);
		this.ta = ta;
		this.pstemming = pstemming;
	}
	
	public AnsjTokenizer(AttributeFactory factory, Analysis ta) {
		super(factory);
		this.ta = ta;
	}
	
	public AnsjTokenizer(Analysis ta) {
		this.ta = ta;
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		int position = 0;
		Term term = null;
		String name = null;
		int length = 0;
		boolean flag = true;
		do {
			term = ta.next();
			if (term == null) {
				break;
			}
			name = term.getName();
			length = name.length();
			if (pstemming && term.termNatures() == TermNatures.EN) {
				name = stemmer.stem(name);
				term.setName(name);
			}

			if (filter != null && filter.contains(name)) {
				continue;
			} else {
				position++;
				flag = false;
			}
		} while (flag);
		if (term != null) {
			positionAttr.setPositionIncrement(position);
			termAtt.setEmpty().append(term.getName());
			offsetAtt.setOffset(term.getOffe(), term.getOffe() + length);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 必须重载的方法，否则在批量索引文件时将会导致文件索引失败
	 */
	@Override
	public void reset() throws IOException {
		super.reset();
		ta.resetContent(new AnsjReader(this.input));
	}
	
	/**
	 * 添加停用词
	 * @param dir
	 */
	private void addStopwords(String dir) {
        if (dir == null || "".equals(dir)) {
            return;
        }
        this.filter = new HashSet<String>();
        InputStreamReader reader;
        try {
        	InputStream is = this.getClass().getClassLoader().getResourceAsStream(dir);
            reader = new InputStreamReader(is,"UTF-8");
            BufferedReader br = new BufferedReader(reader); 
            String word = br.readLine();  
            while (word != null) {
                this.filter.add(word);
                word = br.readLine(); 
            }  
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No custom stopword file found");
        } catch (IOException e) {
        	throw new RuntimeException("Custom stopword file io exception");
        }      
    }
}
