package ru.nlp_project.story_line2.server_storm.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsArticleFact {
	@JsonProperty("sentence_index")
	public int sentenceIndex;
	@JsonProperty("fact_key")
	public String factKey;
	@JsonProperty("fact_sentence_start_pos")
	public int factStartPos;
	@JsonProperty("fact_value")
	public String factValue;

	public NewsArticleFact() {}

	public NewsArticleFact(int sentenceIndex, String factKey, int factStartPos, String factValue) {
		super();
		this.sentenceIndex = sentenceIndex;
		this.factKey = factKey;
		this.factStartPos = factStartPos;
		this.factValue = factValue;
	}


}
