# Обработчик сообщений проекта

Реализован на базе [Apache Storm](http://storm.apache.org/)

### Конфигурация
Конфигурирование кластера осуществляется посредством конфигурационных файлов, расположенных на веб-сервере nginx (разворачиваемом в рамках подпроекта "server_web"), при этом путь главному конфигурационному файлу указывается в качестве первого аргумента при вызове
`storm jar` (например, `storm jar path/to/allmycode.jar org.me.MyTopology arg1`)
(поле 'server_storm.config.file'), располженного в /server_storm/apache-storm-${server_storm_version}/conf/. Данные представлены в формате YAML.

# Топологии
## "crawler_data_processing_topology"
Занимается обработко данных от краулера. Входными данными являются данные от краулера в БД "crawler":
```json
{
    "_id" : ObjectId("587cbc11aca9f3482120b052"),
    "publication_date" : ISODate("2017-01-13T16:06:00.000Z"), // datetime in UTC
	"processing_date" : ISODate("2017-01-13T16:06:00.000Z"), // datetime in UTC
	"content" : "Около ... фактическим исполнением.",
    "path" : "/data/news/58212/",
    "source" : "bnkomi.ru",
    "title" : "Сыктывкарец ради отпуска за границей полностью погасил долг по кредиту",
    "image_url" : "bnkomi.ru/content/news/images/51898/6576-avtovaz-nameren-uvelichit-eksport-lada_mainPhoto.jpg",
    "image_data" : ........,
    "url" : "https://www.bnkomi.ru/data/news/58212/"
}
```
1. Извлечением занимате SPOUT "crawler_news_article_reader" (CrawlerNewsArticleReaderSpout)
	1. Выходные поля:
		- **source** источник новости
		- **objectId** идентификатор записи о новости в основной базе данных
	1. Алгоритм работы:
		- считывает  данные из БД crawler'а
		- делает в объетке отметку "in_process"
		- создаёт объект в БД основного приложения (storyline) со ссылкой на объект из БД crawler'а
		- идентификатор из БД storyline используется как идентификатор сообщения.
