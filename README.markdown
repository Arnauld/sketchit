

Syntax for Class diagram is based on [yUML](http://yuml.me/diagram/class/draw) syntax


    Class   [Customer]
    Directional [Customer]->[Order]
    Bidirectional   [Customer]<->[Order]
    Aggregation [Customer]+-[Order] or [Customer]<>-[Order]
    Composition [Customer]++-[Order]
    Inheritance [Customer]^[Cool Customer], [Customer]^[Uncool Customer]
    Dependencies    [Customer]uses-.->[PaymentStrategy]
    Cardinality [Customer]<1-1..2>[Address]
    Labels  [Person]customer-billingAddress[Address]
    Notes   [Person]-[Address],[Address]-[note: Value Object]
    Full Class  [Customer|Forename;Surname;Email|Save()]
    Splash of Colour    [Customer{bg:orange}]<>1->*[Order{bg:green}]

    [Foo|valueProp]
    [Foo]entityRef->[Bar]
    [Foo]entityComp++->ownedBy[Baz]
    [Foo]oneToMany->*[FooBar]
    [Bar|name]
    [FooBar|value]
    [FooBar]^[Bar]


    [Customer]<>1->*[Order],
    [Customer]-[note: Aggregate Root{bg:cornsilk}]