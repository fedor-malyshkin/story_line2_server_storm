package ru.nlp_project.story_line2.server_storm;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ISearchManager {

	void shutdown();

	void indexNewsArticle(Map<String, Object> newsArticle) throws Exception;

	/**
	 * Получить заголовки статей.
	 *
	 * @param source источник
	 * @param count количество записей для получения
	 * @param lastNewsId идентификатор новости с которой необходимо получать заголовки (не включая её
	 * саму) (т.е. аналог идентификатора для сдвига).
	 * @return список ассоциативных массивов с полями для JSON объекта.
	 */
	List<Map<String, Object>> getNewsHeaders(String source, int count, String lastNewsId);

	Map<String, Object> getNewsArticle(String id);

	Map<String, Object> getNewsArticleImageData(String newsId, int position);

	void 	updateNewsArticle(Map<String, Object> newsArticle) throws IOException;
}
