# BDM: nekomanju: keep air clean

## Abstraction
**This app is developped as a activity in UTokyo's course named "BDM"**.  
It consists of two main components.
1. IoT environment observer and local Bluetooth server.
   - source is mainly under [/main](/main).
2. Android Demo App.
   - source is mainly under [/app](/app).

## Credits
2021.01.07, work of BDM.  
[smallkirby](https://github.com/smallkirby), [NaomiatLibrary](https://github.com/NaomiatLibrary)

## Functionalities
Watch air condition and record it with various information. Collected data is sent to remote server via local BT server, then utilized via Android app.  
Mainly inspired by [mocha](https://mocha.t.u-tokyo.ac.jp/en)(UTokyo's mobile check-in app for COVID-19).

## Images
### Mapping  
![mapping](/img/1.png)

### Visualization
![vis](/img/2.png)

### System Abstraction 
![abs](/img/3.png)

## Warnings
We make no warranty, especially on security. Remember that this is demo system/app.

## LISCENSE
Most of our code is under [MIT LISENCE](/LICENSE).  
However, library source code under `/main/mains` and `/main/src/` belongs to their own LISCENSE.
