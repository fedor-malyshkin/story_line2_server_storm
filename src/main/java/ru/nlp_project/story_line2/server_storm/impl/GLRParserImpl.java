package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.glr_parser.GLRParser;
import ru.nlp_project.story_line2.glr_parser.IFactListener;
import ru.nlp_project.story_line2.glr_parser.IGLRLogger;
import ru.nlp_project.story_line2.glr_parser.SentenceProcessingContext;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGLRParser;

public class GLRParserImpl implements IGLRParser {

	@Inject
	public IConfigurationManager configurationManager;
	private Logger logger;
	private IFactListener myFactListener;
	private GLRLoggreImpl myLogger;
	private GLRParser parser;

	@Inject
	public GLRParserImpl() {
		super();
		logger = LoggerFactory.getLogger(this.getClass());
	}

	public void initialize() {
		myLogger = new GLRLoggreImpl();
		myFactListener = new FactListenerImpl();
		try {
			parser = GLRParser.newInstance(myLogger, myFactListener, true, true);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void shutdown() {}

	public class GLRLoggreImpl implements IGLRLogger {

	}


	public class FactListenerImpl implements IFactListener {
		public void factExtracted(SentenceProcessingContext context, Fact fact) {
		}

	}


	@Override
	public void parseText(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Fact> getGeoFacts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fact> getFIOFacts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fact> getNounFacts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fact> getAdjFacts() {
		// TODO Auto-generated method stub
		return null;
	}


}
