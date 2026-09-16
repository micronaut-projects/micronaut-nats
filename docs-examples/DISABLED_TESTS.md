# Python Docs Disabled Test Inventory

This file tracks the Python documentation examples under `docs-examples/example-python`
that are disabled, reduced, or carry a workaround because the direct port of the Java example does
not compile or does not behave like the Java example yet. It is the bug-fixing task list for the Python compiler
(`micronaut-inject-python` / `micronaut-context-python`); every row references a `TODO(python)`
comment in the sources.

## Reconciliation

- Last generated active `@Disabled` count: 1.
- Last generated command: `rg -n "@Disabled\(" docs-examples/example-python/src/test/python`.
- Last full-suite command: `./gradlew :micronaut-docs-examples:micronaut-example-python:test -Ppython-ci` (needs a
  container runtime for the NATS test container).
- Last full-suite result: build successful, 14 tests executed, 1 skipped (`QueueSpec`, see below), 0 failures.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. The
  Micronaut and NATS annotations are imported from their Java packages (`micronaut.nats.annotation`,
  `micronaut.nats.jetstream.annotation`, `micronaut.messaging.annotation`).
- `@NatsClient` / `@JetStreamClient` interfaces are abstract classes (`ABC`) whose abstract methods have `...`
  bodies; `@NatsListener` / `@JetStreamListener` beans are plain classes with `@Subject` / `@PushConsumer`
  methods. Parameter annotations use `Annotated[str, MessageHeader("name")]`, `Annotated[str, Subject]`, ...
- Python methods cannot be overloaded, so the overloaded `send` / `receive` methods of the Java examples get
  one Python method per signature (`send_with_headers`, `receive_product_sizes`, ...); the guide carries a
  `[.lang-python]` note where it matters.
- Models are plain `@dataclass` classes without getters or setters; `int` is used for `Long` members.
- Methods that implement or override a Java interface keep the Java (camelCase) name; other methods are
  snake_case. Java `byte[]` is `bytes`; 64-bit header parameters are declared as `java.lang.Long`.
- A Python test class is a `@MicronautTest(environments=["nats"])` with injected `@NatsClient` and listener beans
  (`product_client: Annotated[ProductClient, Inject]`). The `nats.addresses` / `nats.port` properties of the shared
  NATS test container are supplied by the Java `io.micronaut.docs.nats.NatsTestConfigurer` (`@ContextConfigurer`)
  of this project (`src/test/java`), see below.
- Java classes are imported (`from java.lang import Long`, `from reactor.core.publisher import Mono`,
  `from org.reactivestreams import Publisher`; `io.nats.client.*` types through the `except ImportError` fallback
  described below); `java.type(...)` is only used where a Python class must be passed to Java as a runtime
  `java.lang.Class` (see "java.type usages" below).

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `io.micronaut.nats.docs.consumer.queue.QueueSpec` | The `queue` member of `@Subject` aliases `@NatsListener.queue`, and `@NatsListener` carries the `@MessageListener` (`@Bean`) stereotype, so the Python compiler treats a Python method annotated with `@Subject(value="product", queue="product-queue")` as a `@Bean` factory method (`Factory methods declared with @Bean must specify a return type`). The consumer advice only reads the queue from the `@Subject` annotation, so it cannot be declared on the listener class either; the Python listener subscribes without a queue group and the test, which expects a single delivery, is disabled. |

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| Every module importing `io.nats.client.*` types (`consumer.types.ProductListener`, `headers.ProductClient`, `headers.ProductListener`, `headers.HeadersSpec`, `consumer.custom.annotation.SIDAnnotationBinder`, `consumer.custom.type.ProductInfoTypeBinder`, `serdes.ProductInfoSerDes`, `jetstream.ProductClient`, `jetstream.PullConsumerHelper`, `jetstream.kv.KeyValueStoreHolder`, `jetstream.os.ObjectStoreHolder`, `jetstream.JetstreamTest`) | the Python compiler resolves `from io.nats.client import Message` at compile time, but at runtime only `io.micronaut.*` imports are rewritten, so the `io.nats` package cannot be imported (`No module named 'io.nats'; 'io' is not a package`); the sources import the generated `nats.client` shim packages in an `except ImportError` fallback (the `imports` tag is rendered with `indent=0`). The `io.nats.client.ObjectStore` type, whose simple name clashes with the `@ObjectStore` qualifier, is imported `as NatsObjectStore`. |
| `io.micronaut.nats.docs.jetstream.PullConsumerHelper` | The methods `PullSubscribeOptions.Builder` inherits from the generic `SubscribeOptions.Builder<B, SO>` (`stream(...)`, `configuration(...)`, `durable(...)`) are not exposed to GraalPy (`foreign object has no attribute 'stream'`), so the pull options are built with `ConsumerConfiguration.builder()...buildPullSubscribeOptions()` and the stream is resolved from the subject. |
| `io.micronaut.nats.docs.serdes.ProductInfoSerDes` | A bridged method declared to return `bytes \| None` is coerced with `Value.asByte()` (`Cannot convert 'b'...'' to Java type 'byte'`), so `serialize` is declared to return `bytes` (a missing return annotation yields an `Object` return type whose `bytes` value becomes a `PolyglotList`). |
| `io.micronaut.docs.nats.PythonRuntimeInitializer` (Java, `src/test/java`) | The executable method processors of `@MessageListener` beans (the NATS consumer advices) are created before the `@Context` beans, and their constructors instantiate every `NatsArgumentBinder` / `NatsMessageSerDes` bean, so a Python binder, serdes or listener would be instantiated before the GraalPy runtime exists (`GraalPy context has not been initialized`); the initializer creates the GraalPy context bean when the first bean of the context is created. |
| `io.micronaut.docs.nats.NatsTestConfigurer` (Java, `src/test/java`) | `TestPropertyProvider.getProperties()` is called by Micronaut Test before the application context, and with it the GraalPy runtime, exists, so a Python test class cannot provide the container's `nats.addresses`; the `@ContextConfigurer` adding the `nats.addresses` / `nats.port` property source in `configure(ApplicationContext)` (the builder overload runs before `@MicronautTest` selects the `nats` environment) is written in Java. `ConnectionSpec` derives `nats.product-cluster.addresses` from it with a `@Property` placeholder. |

## Intentionally Unsupported Snippet Targets

None.

## java.type usages

Every remaining `java.type(...)` call carries a `# TODO(python)` comment naming the reason.

| Location | Reason |
| --- | --- |
| `consumer/custom/annotation/SIDAnnotationBinder.py` (`SIDClass`) | `NatsAnnotatedArgumentBinder.getAnnotationType()` returns the annotation type to Java as a runtime `java.lang.Class`; returning the imported Python annotation function fails with `Cannot convert '<function SID>' (language: Python, type: function) to Java type 'java.lang.Class'`. |
