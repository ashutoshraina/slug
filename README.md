# slug

Slug is a microservice architecture generator, primarily used for generating architecutres that enable fast simulation for Lineage Driven Fault Injection.

# Build Status

[![CircleCI](https://circleci.com/gh/ashutoshraina/slug/tree/master.svg?style=svg)](https://circleci.com/gh/ashutoshraina/slug/tree/master)

How to run

Clone the repo

```
git clone git@github.com:ashutoshraina/slug.git

```

```
./gradlew clean built test run
```

Output

In the samples directory : 

* There will be png images of the generated architecture as force-directed graphs.
* There will dot files which can be used to render the architectures in grpahviz.
* If you have installed graphviz and it is in your path, then you can do 

``` bash
dot -Tpng samples/input.dot > output.png

```

Configuration

Modify the properties file in resources for various configuration options.
 

``` yml
display.swing=true # enables or disables the force directed visualisation (Swing based)

display.dot=true # enables or disables the generation of dot files that can be fed into GraphViz

style=style.css # style file force directed visualisation
```

``` yml
densityFromDistribution=dense # service densityFromDistribution
#options for densityFromDistribution sparse,dense,hyperdense

replication= medium # replication factor
#options for replication minimal, medium, high

```

```yml
iterations controls the number of iterations for which the generator will run. 
For each iteration we generate the artifacts 
in the output folder specifed by the outputDirectory property.

The metrics for the generated artifacts are also put inside the same iteration directory, 
these are .m files which can be directly rendered in Octave or Matlab.
```