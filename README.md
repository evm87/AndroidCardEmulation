# AndroidCardEmulation

Google's HBCE uses ISO-144467 for broadcasting data, meaning any card readers must be setup to receive data along that transmission frequency. This application allows for the broadcast of five pieces of information: the card holder name, card number, security code, flag for the security code, and timezone the user is currently in (sorted by filtering the Java timezone list using regex).

RxAndroid is also used to a significant degree for writing data to Realm and for handling the search function of the timezone list.

Utilizes the following technologies and techniques:

RxAndroid, Realm, MVP Architecture, Fragments, Dependency Injection, Generics, Data Binding, Regex
RxAndroid,
Realm,
MVP Architecture,
Fragments,
Dependency Injection,
Generics,
Data Binding
