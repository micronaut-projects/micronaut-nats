# tag::clazz[]
from dataclasses import dataclass


@dataclass
class ProductInfo:
    size: str | None  # <1>
    count: int  # <2>
    sealed: bool  # <3>
# end::clazz[]
