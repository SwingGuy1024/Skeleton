# Skeleton
Experiment of a simple embedded database application 

This application is an experiment and a learning exercise. 

## Building
More details on building and running may be found in the [project wiki](https://github.com/SwingGuy1024/Skeleton/wiki/Skeleton-Key-Application), but here's a quick overview.

Before building the project, you must run the code generator. The jOOQ database classes are generated from the empty database at `src/main/resources/sql/generateFromSkeleton.db`

After ensuring the maven runner is using Java 8, you may generate the code and build the Mac OSX executable with this command:

    mvn jooq-codegen:generate clean install

Generated code goes into the `com.neptunedreams.skeleton.gen` package.

Once the code has been generated, it need not be generated again unless the schema changes. (The maven clean command does not delete the generated code.) So at this point, you can build by just typing

    mvn clean install

To run on any other platform, you need to build an executable jar file. For that, you should run

    mvn clean assembly:assembly

### Troubleshooting Building

To build, type `mvn clean install`

### Assembly
The pom.xml file will build an application bundle for the Mac. As it is currently configured, it does not bundle the
JDK with the app. This is done for size reasons. I used to be able to include the JDK by setting a jrePath property in
the pom file, but the plugin I was using became obsolete and I had to find a new one. I haven't looked into how to do
this with the new one yet.

## Why?

For more information on the purpose of this project, see the [project wiki](https://github.com/SwingGuy1024/Skeleton/wiki/Skeleton-Key-Application).
