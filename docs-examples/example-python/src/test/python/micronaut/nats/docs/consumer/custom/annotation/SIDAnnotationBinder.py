import java
from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from jakarta.inject import Singleton
    from micronaut.core.convert import ArgumentConversionContext, ConversionService
    from micronaut.nats.bind import NatsAnnotatedArgumentBinder
    from .SID import SID
    from io.nats.client import Message
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import Message

# TODO(python): java.type needed because the annotation type is returned to Java as a runtime java.lang.Class
# (NatsAnnotatedArgumentBinder.getAnnotationType()); the Python annotation function itself is not accepted
# ("Cannot convert '<function SID>' to Java type 'java.lang.Class'")
SIDClass = java.type("micronaut.nats.docs.consumer.custom.annotation.SID")


@Requires(property="spec.name", value="SIDSpec")
# tag::clazz[]
@Singleton  # <1>
class SIDAnnotationBinder(NatsAnnotatedArgumentBinder):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getAnnotationType(self):
        return SIDClass

    def bind(self, context: ArgumentConversionContext, source: Message):
        sid = source.getSID()  # <4>
        return lambda: self.conversion_service.convert(sid, context)  # <5>
# end::clazz[]
