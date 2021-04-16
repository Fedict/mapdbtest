/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.bosa.mapdbtest;

import java.io.IOException;
import java.util.Set;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 *
 * @author Bart.Hanssens
 */
@Warmup(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1,
		jvmArgs= {"-XX:StartFlightRecording=delay=5s,duration=200s,filename=c:\\data\\flight308.jfr,settings=profile", 
				"-XX:FlightRecorderOptions=samplethreads=true,stackdepth=1024",
				"-XX:+UseSerialGC"})
public class Main {
	BindingSet[] bs;
	ValueFactory vf = SimpleValueFactory.getInstance();
	DB db;
	Set s;

	public static void main(String args[]) throws RunnerException, IOException {

        Options opt = new OptionsBuilder()
                .include(Main.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
	}
	

	
	@Setup
	public void setup() {
		// v1
		//db = DBMaker.newTempFileDB().make();
		//s = db.createHashSet("test1").make();
		// v3
		db = DBMaker.tempFileDB().fileChannelEnable().allocateStartSize(16*1024*1024).make();
		s = (Set) db.hashSet("test3").serializer(Serializer.JAVA).create();

		bs = new BindingSet[5000];
		for (int j = 0; j < 5000; j++) {
			MapBindingSet ms = new MapBindingSet(1);
			ms.addBinding("s", vf.createLiteral(j));
			bs[j] = ms;
		}
	}

	@Benchmark
	public void addCommit() {
		for (int j = 0; j < 50; j++) {
			for (int i = 0; i < 100; i++) {
				s.add(bs[j*100+i]);
			}
			db.commit();
		}
	}
	
	//@Benchmark
	public void addNoCommit() {
		for (int j = 0; j < 50; j++) {
			for (int i = 0; i < 100; i++) {
				s.add(bs[j*100+i]);
			}
		}
	}
}
