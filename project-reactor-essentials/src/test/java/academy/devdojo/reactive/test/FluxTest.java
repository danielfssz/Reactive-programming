package academy.devdojo.reactive.test;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
public class FluxTest {

	@Test
	public void fluxSubscribe() {

		Flux<String> stringFlux = Flux.just("Daniel", "Souza", "DevDojo", "Academy")
				.log();

		StepVerifier.create(stringFlux)
				.expectNext("Daniel", "Souza", "DevDojo", "Academy")
				.verifyComplete();
	}

	@Test
	public void fluxSubscribeNumbers() {
		Flux<Integer> integerFlux = Flux.range(1, 5)
				.log();

		integerFlux.subscribe(i -> log.info("Number {}", i));

		log.info("-----------------");

		StepVerifier.create(integerFlux)
				.expectNext(1, 2, 3, 4, 5)
				.verifyComplete();
	}

	@Test
	public void fluxSubscribeFromList() {
		Flux<Integer> integerFlux = Flux.fromIterable(Arrays.asList(1, 2, 3, 4, 5))
				.log();

		integerFlux.subscribe(i -> log.info("Number {}", i));

		log.info("-----------------");

		StepVerifier.create(integerFlux)
				.expectNext(1, 2, 3, 4, 5)
				.verifyComplete();
	}

	@Test
	public void fluxSubscribeNumbersError() {
		Flux<Integer> flux = Flux.range(1, 5)
				.log()
				.map(i -> {
					if (i == 4) {
						throw new IndexOutOfBoundsException("index error");
					}
					return i;
				});

		flux.subscribe(
				i -> log.info("Number {}", i),
				Throwable::printStackTrace,
				() -> log.info("DONE!"),
				subscription -> subscription.request(3) // limitando backpressure
		);

		log.info("-----------------");

		StepVerifier.create(flux)
				.expectNext(1, 2, 3)
				.expectError(IndexOutOfBoundsException.class)
				.verify()
		;
	}

	@Test
	public void fluxSubscribeNumbersUglyBackpressure() {
		Flux<Integer> flux = Flux.range(1, 10)
				.log();

		flux.subscribe(new Subscriber<Integer>() {
			private int count = 0;
			private Subscription subscription;
			private int requestCount = 2;

			@Override
			public void onSubscribe(Subscription subscription) {
				this.subscription = subscription;
				subscription.request(requestCount);
			}

			@Override
			public void onNext(Integer integer) {
				count++;
				if (count >= 2) {
					count = 0;
					subscription.request((requestCount));
				}
			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});

		log.info("-----------------");

		StepVerifier.create(flux)
				.expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
				.verifyComplete()
		;
	}

	@Test
	public void fluxSubscribeNumbersNotSoUglyBackpressure() {
		Flux<Integer> flux = Flux.range(1, 10)
				.log();

		flux.subscribe(new BaseSubscriber<Integer>() {
			private int count = 0;
			private final int requestCount = 2;

			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				request(requestCount);
			}

			@Override
			protected void hookOnNext(Integer value) {
				count++;
				if (count >= 2) {
					count = 0;
					request((requestCount));
				}
			}

		});

		log.info("-----------------");

		StepVerifier.create(flux)
				.expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
				.verifyComplete()
		;
	}

	@Test
	public void fluxSubscribePrettyBackpressure() {
		Flux<Integer> integerFlux = Flux.range(1, 10)
				.log()
				.limitRate(3);

		integerFlux.subscribe(i -> log.info("Number {}", i));

		log.info("-----------------");

		StepVerifier.create(integerFlux)
				.expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
				.verifyComplete();
	}

	@Test
	public void fluxSubscribeIntervalOne() throws Exception {
		Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
				.take(10)
				.log();

		interval.subscribe(i -> log.info("Number {}", i));

		Thread.sleep(3000);
	}

	@Test
	public void fluxSubscribeIntervalTwo() throws Exception {
		StepVerifier.withVirtualTime(this::createInterval)
				.expectSubscription()
				.expectNoEvent(Duration.ofDays(1))
				.thenAwait(Duration.ofDays(1))
				.expectNext(0L)
				.thenAwait(Duration.ofDays(1))
				.expectNext(1L)
				.thenCancel()
				.verify()
		;
	}

	private Flux<Long> createInterval() {
		return Flux.interval(Duration.ofDays(1))
				.take(10)
				.log();
	}

	@Test
	public void connectableFlux() throws Exception {
		ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
				.log()
				.delayElements(Duration.ofMillis(100))
				.publish();

//		connectableFlux.connect();
//
//		log.info("Thread sleeping for 300ms");
//		Thread.sleep(300);
//
//		connectableFlux.subscribe(i -> log.info("Sub1 Number {}", i));
//
//		log.info("Thread sleeping for 200ms");
//		Thread.sleep(200);
//
//		connectableFlux.subscribe(i -> log.info("Sub2 Number {}", i));

		StepVerifier.create(connectableFlux)
				.then(connectableFlux::connect)
				.thenConsumeWhile(i -> i <= 5)
				.expectNext(6, 7, 8, 9, 10)
				.expectComplete()
				.verify();
		;
	}

}
