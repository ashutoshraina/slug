# slug

# Build Status

[![CircleCI](https://circleci.com/gh/ashutoshraina/slug/tree/master.svg?style=svg)](https://circleci.com/gh/ashutoshraina/slug/tree/master)

How to run

```
clone the repo

```

```
./gradlew clean built test run
```

Output

In the samples directory : 

* There will be png images of the generated architecture as force-directed graphs.
* There will dot files which can be used to render the architectures in grpahviz.
* If you have installed graphviz, then you can do 

```
dot -Tpng samples/input.dot > output.png
``` 
