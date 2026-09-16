# tag::imports[]
from micronaut.core.bind.annotation import Bindable
# end::imports[]


# tag::clazz[]
@Bindable  # <1>
def SID():
    def decorator(target):
        return target
    return decorator
# end::clazz[]
