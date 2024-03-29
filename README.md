# Обработчик сообщений проекта

Реализован на базе [Apache Storm](http://storm.apache.org/)

### Конфигурация
Конфигурирование кластера осуществляется посредством конфигурационных файлов, расположенных на веб-сервере nginx (разворачиваемом в рамках подпроекта "server_web"), при этом путь главному конфигурационному файлу указывается в качестве первого аргумента при вызове
`storm jar` (например, `storm jar path/to/allmycode.jar org.me.MyTopology arg1`)
(поле 'server_storm.config.file'), располженного в /server_storm/apache-storm-${server_storm_version}/conf/. Данные представлены в формате YAML.

# Топологии
## crawler_data_processing_topology
Занимается обработкой данных от краулера. Входными данными являются данные от краулера в Mongo БД "crawler":
```json
{
    "_id" : ObjectId("587cbc11aca9f3482120b052"),
    "publication_date" : ISODate("2017-01-13T16:06:00.000Z"), // datetime in UTC
	"processing_date" : ISODate("2017-01-13T16:06:00.000Z"), // datetime in UTC
	"content" : "Около ... фактическим исполнением.",
    "raw_content" : "<html>....",  // только в случае невозможности предварительного извлечения данных....
    "path" : "/data/news/58212/",
    "source" : "bnkomi.ru",
    "title" : "Сыктывкарец ради отпуска за границей полностью погасил долг по кредиту",
    "image_url" : "bnkomi.ru/content/news/images/51898/6576-avtovaz-nameren-uvelichit-eksport-lada_mainPhoto.jpg",
    "url" : "https://www.bnkomi.ru/data/news/58212/"
}
```
При этом в случае если контент сайта не был получен с использованием готовых feed-ов, поля "publication_date", "title", "image_url" и "content" могут отсуствовать и требуется дополнительный анализ и извлечение информации (а поле "raw_content" -- присутствовать).

### Шаги работы
- SPOUT (crawler_entry_reader):
	- ищем "crawler_entry" (in_process != true AND processed != true AND archived != true)
	- ищем "crawler_entry" по совпадению "source:path", если находим обновляем crawler_id, если не находим - создаём новый
	- полученный или найденный идентификатор используем как идентифактор tuple
- BOLT (content_extractor):
	- по полученному идентификатору получить "news_article", по идентификатору из него - "crawler_entry"
	- если crawler_entry.rawContent == null - выходим, т.к. всё обработано ранее
	- если crawler_entry.rawContent != null извлекаем данные скриптом
	- если дата публикации (publication_date) оказалась непустой - обновляем её и в "crawler_entry"
	- загружаем картинки в news_article
	- обновляем news_article
- BOLT (text_processor):
	- nothing for the moment
- BOLT (elasticsearch_indexer):
	- simple indexation

## maintenance_topology
Занимается обслуживанием данных продукта: вычистка старых записей, вычистка неиспользуемых записей, укомпоновка объектов...

### Шаги работы
TBD

## server_web_request_processing_topology
Занимается обработкой запросов от server_web клиента. Trident топология для DRPC клиента с потоком, соотвествующим методу. Все аргументы метода записываются в простой JSON-объект, передаваемый в поток.

### Шаги работы
TBD

# Команды обслуживания
В связис необходимостью подачи команд топологии для изменения её работы (без перезапуска или ручных манипуляций в БД) имеются возможности отправки команд обслуживания. Реализуется через создание специального  документа в mongodb в коллекции "maintenance_entries". Содержит в себе 3 поля: 'command', 'param1' и 'param2'.

## Комманда: рекраулинг конкретного сайта (источника)
Используется, когда требуется выполнить рекраулиг одного из источников (опасная операция).
- command: "recrawl"
- param1: "имя_источника"
- param2: ""

## Комманда: перезагрузка скриптов для извлечения контента
Используется, когда требуется выполнить перзагрузку скриптов извлечения данных.
- command: "reload_scripts"
- param1: ""
- param2: ""

## Комманда: реиндексация одного из источников
Используется, когда требуется выполнить переиндексацию конкретного источника.
- command: "reindex_source"
- param1: "имя_источника"
- param2: ""

## Комманда: реиндексация ВСЕХ источников
Используется, когда требуется выполнить переиндексацию **ВСЕХ** источников.
- command: "reindex_source"
- param1: "ALL"
- param2: ""
