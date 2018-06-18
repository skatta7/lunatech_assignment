# lunatech-query-reports-app


This is a play scala web application for Lunatech Assignment

## Assignment:

1. Write a web application in Java or Scala that will ask the user for two actions : Query or Reports.

1.1 Query Option will ask the user for the country name or code and print the airports & runways at each airport. The input can be country code or country name. For bonus points make the test partial/fuzzy. e.g. entering zimb will result in Zimbabwe :)

1.2 Choosing Reports will print the following:

10 countries with highest number of airports (with count) and countries with lowest number of airports.
Type of runways (as indicated in "surface" column) per country
Bonus: Print the top 10 most common runway identifications (indicated in "le_ident" column)

## How to run

Start the Play app:

```bash
sbt run
```

And open [http://localhost:9000/](http://localhost:9000/)

