# Skeleton
Experiment of a simple embedded database application 

This application is an experiment and a learning exercise. 

## Building
More details on building and running may be found in the [project wiki](https://github.com/SwingGuy1024/Skeleton/wiki/Skeleton-Key-Application), but here's a quick overview.

Maven must run under Java 8. *Running under Java 9 or later doesn't work.* (I have read that a later version of JOOQ, 3.11, will fix this problem. I have not yet confirmed this.)

The project is written to be built in JDK 1.8. It has not been tested with any later versions. 

Before building the project, you must run the code generator. The jOOQ database classes are generated from the empty database at `src/main/resources/sql/generateFromSkeleton.db`

After ensuring the maven runner is using Java 8, you may generate the code and build the Mac OSX executable with this command:

    mvn jooq-codegen:generate clean install

Generated code goes into the `com.neptunedreams.skeleton.gen` package.

Once the code has been generated, it need not be generated again unless the schema changes. (The maven clean command does not delete the generated code.) So at this point, you can build by just typing

    mvn clean install

To run on any other platform, you need to build an executable jar file. For that, you should run

    mvn clean assembly:assembly

## Why?

For more information on the purpose of this project, see the [project wiki](https://github.com/SwingGuy1024/Skeleton/wiki/Skeleton-Key-Application).
