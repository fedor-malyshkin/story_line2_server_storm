package ru.nlp_project.story_line2.server_storm.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager.MasterConfiguration;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;

/**
 * Интерпретатор groovy. Для скриптов, анализирующих html-страницы источников.
 * 
 * @author fedor
 *
 */
public class GroovyInterpreterImpl implements IGroovyInterpreter {
	private static final String SCRIPT_ARCHIVE_FILE_NAME = "script_archive.jar";
	private static final String GROOVY_EXT_NAME = "groovy";
	private static final String SCRIPT_SOURCE_STATIC_FILED = "source";
	private static final String SCRIPT_EXTRACT_DATA_METHOD_NAME = "extractData";

	private GroovyScriptEngine scriptEngine;
	private HashMap<String, Class<?>> sourceMap;
	private Logger log;
	@Inject
	public IConfigurationManager configurationManager;
	protected String directory;


	@Inject
	public GroovyInterpreterImpl() {
		log = LoggerFactory.getLogger(this.getClass());
	}


	public void initialize() {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		directory = downloadScriptsFromURL(configuration.contentExtractionScriptPath);
		extractArchiveInDirectory(directory);
		loadScriptsFromDirectory(directory);
	}

	/**
	 * extract SCRIPT_ARCHIVE_FILE_NAME archive in specified directory
	 * 
	 * @param directory
	 */
	@SuppressWarnings("unchecked")
	protected void extractArchiveInDirectory(String directory) {
		File archive = new File(directory + SCRIPT_ARCHIVE_FILE_NAME);
		int BUFFER = 2048;
		ZipFile zip;
		try {
			zip = new ZipFile(archive);
			Enumeration<ZipEntry> zipFileEntries = (Enumeration<ZipEntry>) zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(directory, currentEntry);
				destFile = new File(directory, destFile.getName());
				// destFile = new File(newPath, destFile.getName());
				// File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				// destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}
			}
		} catch (IOException e) {
			log.error("Error while extracting scripts: " + e.getMessage());
			throw new IllegalStateException(e);
		}
		IOUtils.closeQuietly(zip);

	}

	protected String downloadScriptsFromURL(String contentExtractionScriptPath) {
		Path tempDirectory;
		try {
			URL url = new URL(contentExtractionScriptPath);
			tempDirectory = Files.createTempDirectory("server_storm_scrips");
			File tempDir = tempDirectory.toFile();
			tempDir.mkdirs();
			File archive = new File(tempDir + SCRIPT_ARCHIVE_FILE_NAME);
			// copy
			InputStream inputStream = url.openStream();
			FileOutputStream outputStream = new FileOutputStream(archive);
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			log.error("Error while coping scripts: " + e.getMessage());
			throw new IllegalStateException(e);
		}
		return tempDirectory.toAbsolutePath().toString();
	}

	protected void loadScriptsFromDirectory(String directory) {
		File dir = new File(directory);
		if (!dir.isDirectory() || !dir.exists())
			throw new IllegalStateException(String.format("'%s' not exists.", directory));

		sourceMap = new HashMap<String, Class<?>>();
		try {
			scriptEngine = createGroovyScriptEngine(directory);
			Collection<File> files =
					FileUtils.listFiles(new File(directory), new String[] {GROOVY_EXT_NAME}, false);

			if (files.isEmpty())
				throw new IllegalStateException(
						String.format("No script files in '%s'.", directory));

			for (File file : files) {
				Class<?> scriptClass = loadScriptClassByName(file);
				String source = getSourceFromScriptClass(scriptClass);
				sourceMap.put(source.toLowerCase(), scriptClass);
				log.debug("Loaded script '{}' for '{}'.", file.getName(), source);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	protected GroovyScriptEngine createGroovyScriptEngine(String scriptDir) throws IOException {
		GroovyScriptEngine result = new GroovyScriptEngine(scriptDir);
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.setRecompileGroovySource(true);
		result.setConfig(compilerConfiguration);
		return result;
	}

	/**
	 * Получить значение поля "source" из класса.
	 * 
	 * @param scriptClass
	 * @return
	 */
	protected String getSourceFromScriptClass(Class<?> scriptClass) {
		String source;
		try {
			Field field = scriptClass.getField(SCRIPT_SOURCE_STATIC_FILED);
			source = (String) field.get(null);

		} catch (IllegalAccessException | IllegalArgumentException
				| java.lang.NoSuchFieldException e) {
			throw new IllegalStateException(
					String.format("Error while gettings 'source' member (must be public static): ",
							e.getMessage()));
		}
		return source;

	}

	protected Class<?> loadScriptClassByName(File file) throws ResourceException, ScriptException {
		String name = file.getName();
		Class<?> scriptClass = scriptEngine.loadScriptByName(name);
		return scriptClass;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.server_storm.IGroovyInterpreter#executeScript(java.lang.String,
	 * groovy.lang.Binding)
	 */
	@Override
	public Object executeScript(String script, Binding binding) throws Exception {
		GroovyShell shell = new GroovyShell(scriptEngine.getGroovyClassLoader(), binding);
		return shell.evaluate(script);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.server_storm.IGroovyInterpreter#extractData(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> extractData(String source, String webURL, String html)
			throws IllegalStateException {
		if (webURL == null)
			throw new IllegalArgumentException("webURL is null.");
		if (!sourceMap.containsKey(source.toLowerCase())) {
			log.error("No script with 'extractData' for source: '{}'", source);
			throw new IllegalArgumentException("No script for domain: " + source);
		}

		Class<?> class1 = sourceMap.get(source.toLowerCase());

		try {
			Object instance = class1.newInstance();
			Method method = class1.getMethod(SCRIPT_EXTRACT_DATA_METHOD_NAME, Object.class,
					Object.class, Object.class);
			Map<String, Object> result =
					(Map<String, Object>) method.invoke(instance, source, webURL, html);
			return result;
		} catch (Exception e) {
			log.error("Exception while processing {}:{}", source, webURL, e);
			throw new IllegalStateException(e);
		}
	}


}
