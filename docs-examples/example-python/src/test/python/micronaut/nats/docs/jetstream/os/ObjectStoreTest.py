from typing import Annotated

from jakarta.inject import Inject
from java.io import ByteArrayInputStream
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ObjectStoreHolder import ObjectStoreHolder


@Property(name="spec.name", value="ObjectStoreTest")
@MicronautTest(environments=["nats"])
class ObjectStoreTest:
    object_store_holder: Annotated[ObjectStoreHolder, Inject]

    @Test
    def simple_test(self):
        self.object_store_holder.put("test", ByteArrayInputStream("myvalue".encode()))

        assert self.object_store_holder.store.getInfo("test") is not None
