import java
from micronaut.context.annotation import Requires
# tag::imports[]
from jakarta.inject import Singleton
from micronaut.core.bind.ArgumentBinder import BindingResult
from micronaut.core.convert import ArgumentConversionContext, ConversionService
from micronaut.nats.bind import NatsAnnotatedArgumentBinder
from .SID import SID
from io.nats.client import Message
# end::imports[]

# TODO(python): java.type needed because the annotation type is returned to Java as a runtime java.lang.Class
# (NatsAnnotatedArgumentBinder.getAnnotationType()); a Python-defined annotation function is not accepted
# ("Cannot convert '<function SID>' (language: Python, type: function) to Java type 'java.lang.Class'")
SIDClass = java.type("micronaut.nats.docs.consumer.custom.annotation.SID")


@Requires(property="spec.name", value="SIDSpec")
# tag::clazz[]
@Singleton  # <1>
class SIDAnnotationBinder(NatsAnnotatedArgumentBinder[SID]):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getAnnotationType(self) -> type[SID]:
        return SIDClass

    # TODO(python): the return annotation mirrors the Java @Override; without it the returned lambda is not
    # converted to the BindingResult functional interface (see DISABLED_TESTS.md)
    def bind(self, context: ArgumentConversionContext[object], source: Message) -> BindingResult[object]:
        sid = source.getSID()  # <4>
        return lambda: self.conversion_service.convert(sid, context)  # <5>
# end::clazz[]
