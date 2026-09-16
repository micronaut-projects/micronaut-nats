from typing import Annotated

from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from jakarta.inject import Inject, Singleton
    from micronaut.nats.jetstream.annotation import KeyValueStore
    from io.nats.client import KeyValue
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import KeyValue


@Requires(property="spec.name", value="KeyValueTest")
# tag::clazz[]
@Singleton
class KeyValueStoreHolder:
    store: Annotated[KeyValue, Inject, KeyValueStore("examplebucket")]  # <1>

    def put(self, key: str, value: str) -> None:
        self.store.put(key, value)
# end::clazz[]
