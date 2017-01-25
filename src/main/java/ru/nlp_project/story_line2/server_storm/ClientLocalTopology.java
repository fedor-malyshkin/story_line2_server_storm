package ru.nlp_project.story_line2.server_storm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.storm.Config;
import org.apache.storm.ILocalDRPC;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.Stream;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.MapFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

/**
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * 
 * @author fedor
 *
 */
public class ClientLocalTopology {

	private static final String TOPOLOGY_NAME = "drpc-test";
	private static File confDir;

	public static void main(String args[]) throws IOException {
		// // dir
		// confDir = new File("/tmp/server_storm_drpc");
		// FileUtils.forceMkdir(confDir);
		// // main
		// InputStream resourceAsStream =
		// Thread.currentThread().getContextClassLoader().getResourceAsStream(
		// "ru/nlp_project/story_line2/server_storm/server_storm_config.yml");
		// File mainConf =
		// new File(confDir.getAbsolutePath() + File.separator + "server_storm_config.yml");
		// FileOutputStream fos = new FileOutputStream(mainConf);
		// IOUtils.copy(resourceAsStream, fos);
		// FileUtils.forceDeleteOnExit(confDir);
		// // init conf
		// System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
		// mainConf.toURI().toString());

		new ClientLocalTopology().run();
	}

	private void run() {

		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(1);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60 * 5);


		LocalDRPC drpc = new LocalDRPC();
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(TOPOLOGY_NAME, conf, ClientLocalTopologyBuilder.build(drpc));

		String res = drpc.execute("exclamation", "hello");
		System.out.println("Results for 'hello': " + res);

		Utils.sleep(999_999_999);

		cluster.killTopology(TOPOLOGY_NAME);
		cluster.shutdown();
		drpc.shutdown();
	}

	public static class ClientLocalTopologyBuilder {

		public static StormTopology build(ILocalDRPC drpc) {
			TridentTopology topology = new TridentTopology();

			Stream drpcStream = topology.newDRPCStream("exclamation", drpc);
			drpcStream.each(new Fields("args") , new AddExclamationFunction(), new Fields("res"));

			return topology.build();
		}

	}


	public static class AddExclamationFunction implements Function {

		private static final long serialVersionUID = -96856553155699283L;

		@Override
		public void prepare(Map conf, TridentOperationContext context) {

		}

		@Override
		public void cleanup() {

		}

		@Override
		public void execute(TridentTuple tuple, TridentCollector collector) {
			String string = tuple.getString(0);
			collector.emit(new Values(string + "!"));
			collector.emit(new Values(string + "!" ));
		}


	}



}
