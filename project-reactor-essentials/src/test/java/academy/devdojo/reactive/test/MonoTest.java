package academy.devdojo.reactive.test;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/**
 * Reactive Streams
 * 1. Codigo Assincrono
 * 2. Nao bloqueante
 * 3. Saber lidar com Backpressure
 *
 * Publisher - Emite os eventos (Chefe de cozinha) ele eh cold, se ninguem se conectar a ele, ele nao ira disparar eventos
 * Subscription eh criado quando da um subscribe no Publisher
 *
 * Publisher <- (subcribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 *
 * Entao:
 * 1. Publisher envia todos os objetiso solicitados
 * 2. Publisher envia todos os objetos que ele possui. (onComplete) subscriber e subscription sao cancelados
 * 3. Quando um erro eh disparado. (onError)  subscriber e subscription sao cancelados
 */
public class MonoTest {

	@Test
	public void monoSubscriber() {
		String name = "Daniel Souza";
		Mono<String> mono = Mono.just(name).log();

		mono.subscribe();

		log.info("-------------");

		StepVerifier.create(mono) //
				.expectNext("Daniel Souza") //
				.verifyComplete();
	}

	@Test
	public void monoSubscriberConsumer() {
		String name = "Daniel Souza";
		Mono<String> mono = Mono.just(name).log();

		mono.subscribe((s) -> {
			log.info("Valor {}", s);
		});

		log.info("-------------");

		StepVerifier.create(mono) //
				.expectNext("Daniel Souza") //
				.verifyComplete();
	}

	@Test
	public void monoSubscriberConsumerError() {
		String name = "Daniel Souza";
		Mono<String> mono = Mono.just(name) //
				.map(s -> {
					throw new RuntimeException("Teste do mono com erro");
				});

		mono.subscribe((s) -> {
			log.info("Nome {}", s);
		}, e -> {
			log.error("algum erro foi capturado");
		});

		log.info("-------------");

		mono.subscribe((s) -> {
			log.info("Nome {}", s);
		}, e -> {
			log.error("algum erro foi capturado, logando stack de erro", e);
		});

		StepVerifier.create(mono) //
				.expectError(RuntimeException.class)
				.verify();
	}

	@Test
	public void monoSubscriberConsumerComplete() {
		String name = "Daniel Souza";
		Mono<String> mono = Mono.just(name) //
				.log() //
				.map(String::toUpperCase);

		mono.subscribe((s) -> {
					log.info("Valor {}", s);
				},
				Throwable::printStackTrace, //
				() -> log.info("Finalizado"));

		log.info("-------------\n");

		StepVerifier.create(mono) //
				.expectNext(name.toUpperCase()) //
				.verifyComplete();
	}

	@Test
	public void monoSubscriberConsumerSubscription() {
		String name = "Daniel Souza";
		Mono<String> mono = Mono.just(name) //
				.log() //
				.map(String::toUpperCase);

		mono.subscribe((s) -> {
					log.info("Valor {}", s);
				},
				Throwable::printStackTrace, //
				() -> log.info("Finalizado"), //
				subscription -> subscription.request(5));

		log.info("-------------\n");

		StepVerifier.create(mono) //
				.expectNext(name.toUpperCase()) //
				.verifyComplete();
	}

	@Test
	public void monoDoOnMethods() {
		String name = "Daniel Souza";

		// PUBLISHER
		Mono<Object> mono = Mono.just(name) //
				.log() //
				.map(String::toUpperCase) //
				.doOnSubscribe(subscription -> log.info("subscribed")) //
				.doOnRequest(longNumber -> log.info("Request received, starting doing something...")) //
				.doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
				.flatMap(s -> Mono.empty())
				.doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
				.doOnSuccess(s -> log.info("doOnSuccess executed {}", s));

		// SUBSCRIBER
		mono.subscribe((s) -> {
					log.info("Valor {}", s);
				},
				Throwable::printStackTrace, //
				() -> log.info("Finalizado"));

		log.info("-------------\n");

//		StepVerifier.create(mono) //
//				.expectNext(name.toUpperCase()) //
//				.verifyComplete();
	}

	@Test
	public void monoDoOnError() {
		Mono<Object> error = Mono.error(new IllegalArgumentException("Message error")) //
				.doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage())) //
				.doOnNext(s -> log.info(
						"executing this doOnNext")) // doOnNext nao eh executado pois quando um erro eh disparado o publisher fecha
				.log() //
				;

		StepVerifier.create(error) //
				.expectError(IllegalArgumentException.class) //
				.verify();
	}

	@Test
	public void monoDoOnErrorResume() {
		String name = "Daniel Souza";

		Mono<Object> error = Mono.error(new IllegalArgumentException("Message error")) //
				.doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage())) //
				.onErrorResume(s -> {
					log.info("executing this doOnNext");
					return Mono.just(name);
				})
				.log() //
				;

		StepVerifier.create(error) //
				.expectNext(name)
				.verifyComplete();
	}

	@Test
	public void monoDoOnErrorReturn() {
		String name = "Daniel Souza";

		Mono<Object> error = Mono.error(new IllegalArgumentException("Message error")) //
				.onErrorReturn("EMPTY")
				.doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage())) //
				.onErrorResume(s -> {
					log.info("executing this doOnNext");
					return Mono.just(name);
				})
				.log() //
				;

		StepVerifier.create(error) //
				.expectNext("EMPTY")
				.verifyComplete();
	}

}