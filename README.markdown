

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


# Libraries & Inspirations

* [yUML](http://yuml.me/)
  * [Class diagram](http://yuml.me/diagram/scruffy/class/samples)

* [Graphviz](http://www.graphviz.org/)
  * [Arrows](http://www.graphviz.org/doc/info/arrows.html)
  * [Shapes](http://www.graphviz.org/doc/info/shapes.html)
  * [Colors](http://www.graphviz.org/doc/info/colors.html)


* [Web sequence diagrams](http://www.websequencediagrams.com/)

* [Pic language](http://en.wikipedia.org/wiki/Pic_language)
  * http://floppsie.comp.glam.ac.uk/Glamorgan/gaius/web/pic.html
  * [pic2plot](http://www.gnu.org/software/plotutils/manual/en/html_node/pic2plot.html#pic2plot)

* [UmlGraph](http://www.umlgraph.org/doc.html)
  * [Sequence Diagrams](http://www.umlgraph.org/doc/seq-intro.html)


* [Inkscape](http://inkscape.org/)
  * http://wiki.inkscape.org/wiki/index.php/MacOS_X

* SVG
  * Drop Shadow: http://xn--dahlstrm-t4a.net/svg/filters/arrow-with-dropshadow-lighter.svg