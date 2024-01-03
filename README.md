# Connect_Four_Project
My own connect 4 solver, available at http://dcfoursolve.com/.

Project was modeled after and meant to recreate https://connect4.gamesolver.org/en/.

The purpose of pursuing this project is for a fun challenge and to learn several larger concepts.
These include web development and techniques for solving deterministic, turn-based games.
These types of games always fascinated me from a theoretical perspective.
Because finding the perfect move is generally a non-polynomial problem,
the solution is naturally full of clever optimizations which allow the answer to be found in a practical amount of time.
In the spirit of learning, I decided to create this project with no external dependencies or frameworks.
The backend is written in Java and built with Gradle. The front end is written purely in HTML, CSS, and Javascript.

The solver was modeled after http://blog.gamesolver.org/solving-connect-four/01-introduction/.

* The solution from the blog served as an introduction to concepts such as:
  * The Min Max Algorithm
  * Alpha Beta Pruning
  * Iterative Deepening
  * Transposition Tables
* This repo serves as a fork of that solution which changes and adds several features:
  * First this solution was rewritten from C++ to Java.
  * A couple of small performance optimizations were added.
  * An opening book was added, which serializes the keys and solved evaluations of all possible positions that can be reached by the first 0-12 moves.
  * A web server was added.
    * This server searches for the answer if it has already been serialized. If not then it solves it with a variant of the alpha-beta algorithm.
    * Serves html, css, and javascript to the client.
    * Returns where an alignment has occurred in the case where the game has been won.

# Running the Server
The entire source code for the server is made available in the repo.
After cloning this repository and building, users are able to have a complete copy of the server on their machines.
They can then run a single command to start their own copy of the server, and go to `localhost` in their web browser and see that it is working.

## Building
* After cloning the repository, navigate to the directory called Connect_Four_Project. It must be named this.
* Then run `gradle build`.
  * This compiles all java code, runs unit tests, and unzips the opening book.
  * The opening book takes up about 200 MB of storage in total. This is necessary for maintaining the server's performance.

## Running
* Navigate to the built classes directory and run the server.
  * This subdirectory should be `build/classes/java/main`.
  * The command to run the server is `java server.Server`.
  * If this works, the terminal should log the message `The server is running`.
* Alternatively one can stay in the project's root directory and run the server by specify the classpath with the `-cp` flag.

# Versions
* Gradle 8.1.1
  * This may not be strictly necessary for building the server.
* Java 17.0.7
  * Java 17 is strictly necessary for running the server.
