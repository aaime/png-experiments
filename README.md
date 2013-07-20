png-experiments
===============

Experiments on a pure Java PNG writer that's both faster than JDK own one, and the ImageIO CLib one.
In order to build this module you'll need to install PNGJ (https://code.google.com/p/pngj/downloads/detail?name=pngj-1.1.2.zip), which is not available on the official maven repos, with the following command:

 mvn install:install-file -Dfile=pngj.jar -DgroupId=ar.com.hjg -DartifactId=pngj -Dversion=1.1.2 -Dpackaging=jar 


