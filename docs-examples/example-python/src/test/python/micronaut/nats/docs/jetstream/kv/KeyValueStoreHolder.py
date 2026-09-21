from typing import Annotated

from micronaut.context.annotation import Requires
# tag::imports[]
from jakarta.inject import Inject, Singleton
from micronaut.nats.jetstream.annotation import KeyValueStore
from io.nats.client import KeyValue
# end::imports[]


@Requires(property="spec.name", value="KeyValueTest")
# tag::clazz[]
@Singleton
class KeyValueStoreHolder:
    store: Annotated[KeyValue, Inject, KeyValueStore("examplebucket")]  # <1>

    def put(self, key: str, value: str) -> None:
        self.store.put(key, value)
# end::clazz[]
