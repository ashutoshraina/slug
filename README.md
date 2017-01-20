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
./gradlew clean build test run
```

Output

In the samples directory : 

* There will be png images of the generated architecture as force-directed graphs.
* There will dot files which can be used to render the architectures in grpahviz.
* If you have installed graphviz and it is in your path, then you can do 


``` bash
dot -Tpng samples/i_1/e2e.dot > output.png
```

samples is the output directory you specify in the configuration, it defaults to "samples"

"i_1" is the iteration number, it will go to the number of iterations you specify i_n

There are srveral dot files in each directory, pick the one you want to visualise.

You can also visualise them online at http://www.webgraphviz.com/

## Configuration


Modify the properties file in resources for various configuration options.

You can control the number of runs for the generators using he ```iterations``` config parameter.

You can specify the output directory where all the generated files will go (we generate dot files as well the plots for that iteration and put them in respective directories)


| Config Parameter | Description | Options
| --- | --- | --- |
|display.swing |  enables or disables the force directed visualisation (Swing based) | true, false |
| display.dot | enables or disables the generation of dot files that can be fed into GraphViz| true, false|
|style|style file force directed visualisation| true, false|

Unless you are very keen on seeing visualisations in a certain way, it is probably wise to leave the above parameters as is.


| Config Parameter | Description | Options
| --- | --- | --- |
|densityFromDistribution | controls the density of the services i.e. how many instances of a service are there; this applies to web applications | sparse, dense, hyperdense |
| replication | replication factor of the persistence mechanism | minimal, medium, high|
|iterations|how many iterations to run the generator for| any integer|

You can specify the values for replication and density in respective property files i.e. density.properties, replication.properties.

For each iteration we generate the artifacts in the output folder specified by the outputDirectory property.

The metrics for the generated artifacts are also put inside the same iteration directory, these are .m files which can be directly rendered in Octave or Matlab.

Metrics for the all the iterations are combined and written to the metrics directory. This can also be directly used on Octave or Matlab.

A Sample is below :

``` yml
@subplot(2,3,1)
x = [1,2,3,4,5,6,7,8,9];
y = [0.18,0.18,0.14,0.12,0.09,0.09,0.05,0.05,0.06];
plot (x,y);
xlabel ("Graph Id");
ylabel ("Density");
legend ("Density", "position", "north");
title ("Density");

@subplot(2,3,2)
x = [1,2,3,4,5,6,7,8,9];
y = [1.82,2.0,3.56,3.29,2.09,2.0,2.13,2.31,3.94];
plot (x,y);
xlabel ("Graph Id");
ylabel ("Average Degree");
legend ("Average Degree", "position", "north");
title ("Average Degree");

... more plots
```
