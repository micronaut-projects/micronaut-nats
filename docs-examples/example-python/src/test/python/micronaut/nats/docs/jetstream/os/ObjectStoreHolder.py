from typing import Annotated

from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from jakarta.inject import Inject, Singleton
    from java.io import InputStream
    from micronaut.nats.jetstream.annotation import ObjectStore
    from io.nats.client import ObjectStore as NatsObjectStore
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import ObjectStore as NatsObjectStore


@Requires(property="spec.name", value="ObjectStoreTest")
# tag::clazz[]
@Singleton
class ObjectStoreHolder:
    store: Annotated[NatsObjectStore, Inject, ObjectStore("examplebucket")]  # <1>

    def put(self, key: str, input_stream: InputStream) -> None:
        self.store.put(key, input_stream)
# end::clazz[]
