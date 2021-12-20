package academy.devdojo.reactive.test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@Slf4j
public class OperatorsTest {

	@Test
	public void subscribeOnSimple() {
		Flux<Integer> flux = Flux.range(1, 4)
				.map(i -> {
					log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				})
				.subscribeOn(Schedulers.single())
				.map(i -> {
					log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1, 2, 3, 4)
				.verifyComplete()
		;
	}

	@Test
	public void publishOnSimple() {
		Flux<Integer> flux = Flux.range(1, 4)
				.map(i -> {
					log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.single())
				.map(i -> {
					log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				});

		flux.subscribe();
		flux.subscribe();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1, 2, 3, 4)
				.verifyComplete()
		;
	}

	@Test
	public void multipleSubscribeOnSimple() {
		Flux<Integer> flux = Flux.range(1, 4)
				.subscribeOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				})
				.subscribeOn(Schedulers.single())
				.map(i -> {
					log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				});

		flux.subscribe();
		flux.subscribe();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1, 2, 3, 4)
				.verifyComplete()
		;
	}

	@Test
	public void multiplePublishOnSimple() {
		Flux<Integer> flux = Flux.range(1, 4)
				.publishOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1, 2, 3, 4)
				.verifyComplete()
		;
	}

	@Test
	public void publishAndSubscribeOnSimple() {
		Flux<Integer> flux = Flux.range(1, 4)
				.publishOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				})
				.subscribeOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1, 2, 3, 4)
				.verifyComplete()
		;
	}

	@Test
	public void subscribeAndPublishOnSimple() {
		Flux<Integer> flux = Flux.range(1, 4)
				.subscribeOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1, 2, 3, 4)
				.verifyComplete()
		;
	}

	@Test
	public void subscribeOnIO() throws Exception {
		// executa a chamada que esta bloqueando a thread em backgroud
		Mono<List<String>> list = Mono.fromCallable(() -> Files.readAllLines(Paths.get("text-file")))
				.log()
				.subscribeOn(Schedulers.boundedElastic());

		list.subscribe(i -> log.info("{}", i));

		Thread.sleep(200);

		StepVerifier.create(list)
				.expectSubscription()
				.thenConsumeWhile(l -> {
					Assertions.assertFalse(l.isEmpty());
					log.info("Size {}", l.size());
					return true;
				})
				.verifyComplete();
	}

	@Test
	public void switchIfEmptyOperator() {
		Flux<Object> flux = emptyFlux()
				.switchIfEmpty(Flux.just("not empty anymore"))
				.log();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext("not empty anymore")
				.expectComplete()
				.verify();
	}

	private Flux<Object> emptyFlux() {
		return Flux.empty();
	}

	@Test
	public void deferOperator() throws Exception { //adia a execucao do que vc tem dentro do operador
		Mono<Long> just = Mono.just(System.currentTimeMillis());
		Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

		just.subscribe(l -> log.info("just time {}", l));
		Thread.sleep(100);
		just.subscribe(l -> log.info("just time {}", l));
		Thread.sleep(100);
		just.subscribe(l -> log.info("just time {}", l));

		defer.subscribe(l -> log.info("defer time {}", l));
		Thread.sleep(100);
		defer.subscribe(l -> log.info("defer time {}", l));
		Thread.sleep(100);
		defer.subscribe(l -> log.info("defer time {}", l));

		AtomicLong atomicLong = new AtomicLong();
		defer.subscribe(atomicLong::set);
		Assertions.assertTrue(atomicLong.get() > 0);

	}

}
