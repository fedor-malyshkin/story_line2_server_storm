package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.glr_parser.GLRParser;
import ru.nlp_project.story_line2.glr_parser.IFactListener;
import ru.nlp_project.story_line2.glr_parser.IGLRLogger;
import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.FactField;
import ru.nlp_project.story_line2.glr_parser.SentenceProcessingContext;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.ITextAnalyser;
import ru.nlp_project.story_line2.server_storm.model.NewsArticleFact;

public class TextAnalyserImpl implements ITextAnalyser {

	@Inject
	public IConfigurationManager configurationManager;
	private Logger logger;
	private IFactListener myFactListener;
	private GLRLoggreImpl myLogger;
	private GLRParser parser;
	private boolean initMorph;
	private boolean multiThread;

	@Inject
	public TextAnalyserImpl() {
		this(true, false);
	}

	public TextAnalyserImpl(boolean initMorph, boolean multiThread) {
		super();
		this.initMorph = initMorph;
		this.multiThread = multiThread;
		logger = LoggerFactory.getLogger(this.getClass());
	}

	public void initialize() {
		myLogger = new GLRLoggreImpl();
		myFactListener = new FactListenerImpl();
		try {
			// TODO: set correct configuration
			parser = GLRParser.newInstance(null, myLogger, myFactListener, initMorph, multiThread);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		clearFactStorages();
	}

	@Override
	public void shutdown() {}

	public class GLRLoggreImpl implements IGLRLogger {

	}


	public class FactListenerImpl implements IFactListener {

		@Override
		public void factExtracted(SentenceProcessingContext context,
				ru.nlp_project.story_line2.glr_parser.InterpreterImpl.Fact fact) {
			int sentenceIndex = context.getSentence().getIndex();
			int sentenceStartPos = context.getSentence().getStart();
			if (logger.isTraceEnabled())
				logger.trace("Fact extracted for sentence with index {} startPos ''.",
						sentenceIndex, sentenceStartPos);
			Map<String, FactField> fieldsMap = fact.getFieldsMap();
			for (Entry<String, FactField> entry : fieldsMap.entrySet()) {
				String key = entry.getKey();
				FactField factField = entry.getValue();
				// String factFieldName = factField.getName();
				int factFieldFrom = factField.getFrom();
				String value = factField.getValue();
				if (logger.isTraceEnabled())
					logger.trace("Fact extracted: key: '{}', starts from: {}, value: '{}'.", key,
							factFieldFrom, value);
				addFact(sentenceIndex, key, factFieldFrom, value);
			}

		}
	}


	@Override
	public void parseText(String text) throws IOException {
		clearFactStorages();
		parser.processText(text);
	}

	private List<NewsArticleFact> geoFacts = null;
	private List<NewsArticleFact> fioFacts = null;
	private List<NewsArticleFact> nounFacts = null;

	/**
	 * Добавть факты.
	 * 
	 * WARN: при активации много поточности данный метод будет вызываться из разных потоков, что
	 * потребует синхронизации обращения к объектам.
	 * 
	 * @param sentenceIndex
	 * @param factKey
	 * @param factStartPos
	 * @param factValue
	 */
	protected void addFact(int sentenceIndex, String factKey, int factStartPos, String factValue) {
		NewsArticleFact fact = new NewsArticleFact(sentenceIndex, factKey, factStartPos, factValue);
		switch (factKey.toLowerCase()) {
			case "fio":
				fioFacts.add(fact);
				break;
			case "noun":
				nounFacts.add(fact);
				break;
			case "geo":
				geoFacts.add(fact);
				break;
			default:
				logger.warn("Unknown fact extracted: key: '{}', starts from: {}, value: '{}'.",
						factKey, factStartPos, factValue);
				break;
		}
	}

	private void clearFactStorages() {
		geoFacts = new ArrayList<>();
		fioFacts = new ArrayList<>();
		nounFacts = new ArrayList<>();

	}

	@Override
	public List<NewsArticleFact> getGeoFacts() {
		return geoFacts;
	}

	@Override
	public List<NewsArticleFact> getFIOFacts() {
		return fioFacts;
	}

	@Override
	public List<NewsArticleFact> getNounFacts() {
		return nounFacts;
	}



}
