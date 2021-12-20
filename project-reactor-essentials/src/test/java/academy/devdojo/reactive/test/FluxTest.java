package academy.devdojo.reactive.test;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
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
		Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5)
				.log();

		integerFlux.subscribe(i -> log.info("Number {}", i));

		log.info("-----------------");

		StepVerifier.create(integerFlux)
				.expectNext(1, 2, 3, 4, 5)
				.verifyComplete();
	}

}
