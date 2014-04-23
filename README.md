![Preferanto](logo.png "Preferanto")


Preferanto is a specification language for incorporating decision maker preferences into multiobjective evolutionary algorithms. It has been introduced as a companion tool to the framework presented in the following paper:

  * Iordache, R., Iordache, S., Moldoveanu, F. [A Framework for the Study of Preference Incorporation in Multiobjective Evolutionary Algorithms](http://iordache.com/publications/gecco2014.pdf). In: GECCO 2014: Proceedings of the 16th Genetic and Evolutionary Computation Conference, Vancouver, Canada, ACM Press, 2014 (to appear).

How to build
------------

Clone the Preferanto repository, go to the *projects* subdirectory found in your installation main directory and build all Preferanto projects by executing:

    ./gradlew assemble

To generate Eclipse project files, run:

    ./gradlew eclipse


Preferanto consists of three projects:

  * **core** - the library implementing the Preferanto language.

  * **moeap** - a version of the [MOEA Framework](http://www.moeaframework.org/), modified to allow incorporating user preferences described in Preferanto.

  * **experiment** - a collection of tools and programs for experimenting with multiobjective evolutionary algorithms that incorporate user preferences.
