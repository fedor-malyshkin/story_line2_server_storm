package ru.nlp_project.story_line2.server_storm.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.storm.generated.GlobalStreamId;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.MessageId;
import org.apache.storm.tuple.Tuple;

public class TestUtils {

	public static class TupleStub implements Tuple {
		Map<String, Object> map = new HashMap<String, Object>();

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean contains(String field) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Fields getFields() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int fieldIndex(String field) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<Object> select(Fields selector) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getValue(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getString(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getInteger(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getLong(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Boolean getBoolean(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Short getShort(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Byte getByte(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Double getDouble(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Float getFloat(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] getBinary(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getValueByField(String field) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getStringByField(String field) {
			return (String) map.get(field);
		}

		@Override
		public Integer getIntegerByField(String field) {
			return (Integer) map.get(field);
		}

		@Override
		public Long getLongByField(String field) {
			return (Long) map.get(field);
		}

		@Override
		public Boolean getBooleanByField(String field) {
			return (Boolean) map.get(field);
		}

		@Override
		public Short getShortByField(String field) {
			return (Short) map.get(field);
		}

		@Override
		public Byte getByteByField(String field) {
			return (Byte) map.get(field);
		}

		@Override
		public Double getDoubleByField(String field) {
			return (Double) map.get(field);
		}

		@Override
		public Float getFloatByField(String field) {
			return (Float) map.get(field);
		}

		@Override
		public byte[] getBinaryByField(String field) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Object> getValues() {
			return (List<Object>) map.values();
		}


		public void put(String key, String value) {
			map.put(key, value);
		}

		@Override
		public GlobalStreamId getSourceGlobalStreamid() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GlobalStreamId getSourceGlobalStreamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSourceComponent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getSourceTask() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getSourceStreamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MessageId getMessageId() {
			// TODO Auto-generated method stub
			return null;
		}
	}


	public static class OutputCollectorStub extends OutputCollector {
		List<Object> tuples = new ArrayList<Object>();

		public List<Object> getTuples() {
			return tuples;
		}


		public OutputCollectorStub() {
			super(null);
		}


		@Override
		public List<Integer> emit(Tuple anchor, List<Object> tuple) {
			tuples.add(tuple);
			return null;
		}



		@Override
		public void ack(Tuple input) {
		}



	}

	/**
	 * Распаковать zip-файл во временную директорию и вернуть путь к ней.
	 * 
	 * @param cpZipFile
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static String unzipClasspathToDir(String cpZipFile, File newDir) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(cpZipFile);
		File glrZipFile = File.createTempFile("glr-parser-config", ".zip");
		FileUtils.forceDeleteOnExit(glrZipFile);
		FileOutputStream fos = new FileOutputStream(glrZipFile);
		IOUtils.copy(resourceAsStream, fos);

		int BUFFER = 2048;

		ZipFile zip = new ZipFile(glrZipFile);
		String newPath = null;
		if (newDir == null)
			newPath = glrZipFile.getAbsolutePath().substring(0,
					glrZipFile.getAbsolutePath().length() - 4);
		else
			newPath = newDir.getAbsolutePath();

		new File(newPath).mkdir();
		Enumeration<ZipEntry> zipFileEntries = (Enumeration<ZipEntry>) zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath, currentEntry);
			// destFile = new File(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos2 = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos2, BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		}
		IOUtils.closeQuietly(zip);
		FileUtils.forceDeleteOnExit(new File(newPath));
		return newPath;
	}

}
