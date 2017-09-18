package ru.nlp_project.story_line2.server_storm;

import java.util.Map;

import groovy.lang.Binding;


public interface IGroovyInterpreter {
	public static final String EXTR_KEY_IMAGE_URL = "image_url";
	public static final String EXTR_KEY_PUB_DATE = "publication_date";
	public static final String EXTR_KEY_TITLE = "title";
	public static final String EXTR_KEY_CONTENT = "content";


	/**
	 * Выполнить извлечение данных.
	 * 
	 * При анализе извлекаются следующие данные:
	 * <ul>
	 * <li>"publication_date" - дата публикации в формате java.util.Date</li>
	 * <li>"content" - содежание страницы</li>
	 * <li>"title" - заголовок страницы</li>
	 * <li>"image_url" - сылка на первую картинку</li>
	 * </ul>
	 * 
	 * @param source источник данных
	 * @param html html контент страницы
	 * @param webURL ссылка на страницу
	 * @return ассоциативный массив или null в случае неверной (не поддерживаемой) страницу
	 * @throws IllegalStateException
	 */
	Map<String, Object> extractData(String source, String webURL, String html)
			throws IllegalStateException;
}
