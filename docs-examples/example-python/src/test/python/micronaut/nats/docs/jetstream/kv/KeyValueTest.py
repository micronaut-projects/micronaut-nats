from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .KeyValueStoreHolder import KeyValueStoreHolder


@Property(name="spec.name", value="KeyValueTest")
@MicronautTest(environments=["nats"])
class KeyValueTest:
    key_value_store_holder: Annotated[KeyValueStoreHolder, Inject]

    @Test
    def simple_test(self):
        self.key_value_store_holder.put("test", "myvalue")

        assert self.key_value_store_holder.store.get("test").getValueAsString() == "myvalue"
