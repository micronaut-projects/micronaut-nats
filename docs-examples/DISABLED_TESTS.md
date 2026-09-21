# Python Docs Disabled Test Inventory

This file tracks the Python documentation examples under `docs-examples/example-python`
that are disabled, reduced, or carry a workaround because the direct port of the Java example does
not compile or does not behave like the Java example yet. It is the bug-fixing task list for the Python compiler
(`micronaut-inject-python` / `micronaut-context-python`); every row references a `TODO(python)`
comment in the sources.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\(" docs-examples/example-python/src/test/python`.
- Last full-suite command: `./gradlew :micronaut-docs-examples:micronaut-example-python:test -Ppython-ci` (needs a
  container runtime for the NATS test container).
- Last full-suite result (micronaut-core 5.2.3, micronaut-build 8.1.2): build successful, 14 tests executed, 0 skipped, 0 failures.

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
  `from org.reactivestreams import Publisher`, `from io.nats.client import Message`); `java.type(...)` is only used
  where a Python-defined annotation must be passed to Java as a runtime `java.lang.Class` (see "java.type usages"
  below).

## Active `@Disabled` Tests

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.nats.docs.jetstream.os.ObjectStoreHolder` | The `io.nats.client.ObjectStore` type, whose simple name clashes with the `@ObjectStore` qualifier, is imported `as NatsObjectStore` (an ordinary import alias). |
| `io.micronaut.nats.docs.consumer.custom.annotation.SIDAnnotationBinder` | With the generic base `NatsAnnotatedArgumentBinder[SID]`, a `bind` method *without* a return annotation gets the inherited `BindingResult<Object>` signature but its result is converted with `PythonConversion.convertObject(...)`, so the returned lambda is not converted to the functional interface (`NatsListenerException: An error occurred binding the message to the method ... Caused by: java.lang.ClassCastException: class com.oracle.truffle.polyglot.PolyglotMapAndFunction cannot be cast to class io.micronaut.core.bind.ArgumentBinder$BindingResult`; the raw base converts with the `BindingResult` class). `bind` therefore declares `-> BindingResult[object]` like the Java `@Override` (`ProductInfoTypeBinder` declares its `Argument[ProductInfo]` / `BindingResult[ProductInfo]` types the same way). `TODO(python)`. |
| `io.micronaut.docs.nats.NatsTestConfigurer` (Java, `src/test/java`) | `TestPropertyProvider.getProperties()` is called by Micronaut Test before the application context, and with it the GraalPy runtime, exists, so a Python test class cannot provide the container's `nats.addresses`; the `@ContextConfigurer` adding the `nats.addresses` / `nats.port` property source in `configure(ApplicationContext)` (the builder overload runs before `@MicronautTest` selects the `nats` environment) is written in Java. `ConnectionSpec` derives `nats.product-cluster.addresses` from it with a `@Property` placeholder. |

## Intentionally Unsupported Snippet Targets

None.

## java.type usages

Every remaining `java.type(...)` call carries a `# TODO(python)` comment naming the reason.

| Location | Reason |
| --- | --- |
| `consumer/custom/annotation/SIDAnnotationBinder.py` (`SIDClass`) | `NatsAnnotatedArgumentBinder.getAnnotationType()` returns the annotation type to Java as a runtime `java.lang.Class`; returning the Python-defined annotation function still fails with core 5.2.3 (`Cannot convert '<function SID at 0x...>'(language: Python, type: function) to Java type 'java.lang.Class': Unsupported target type.`, verified on the identical RabbitMQ binder). Python *classes* passed as `Class` arguments work. |

## Verified with micronaut-core 5.2.3 (workarounds removed)

- `from io.nats.client import ...` imports work at runtime (the `except ImportError` fallbacks to the generated
  `nats.client` packages are gone).
- `@Subject(value="product", queue="product-queue")` on the listener methods (`QueueSpec` re-enabled).
- `PullSubscribeOptions.builder().stream("events").configuration(...).build()` (the methods inherited from the
  generic `SubscribeOptions.Builder` are exposed).
- `ProductInfoSerDes.serialize(...) -> bytes | None`.
- `PythonRuntimeInitializer` (Java) removed: the GraalPy runtime is created on demand for the Python binders,
  serdes and listeners the consumer advices instantiate.
- Generic binder bases (`NatsAnnotatedArgumentBinder[SID]`, `NatsTypeArgumentBinder[ProductInfo]`).
