# ProiectMultiBank

Aplicatie Java (fără Spring Boot) care integreaza conturile bancare intr-un singur backend JVM/JavaFX, oferind functionalitati de analiza a cheltuielilor, filtrare a tranzactiilor, planuri de economisire si generare de coduri QR pentru plati.

## Functionalitati principale

- Sincronizarea conturilor si tranzactiilor din mai multe banci folosind clienti dedicati (BCR si BT).
- Stocarea in siguranta a conturilor, tranzactiilor si planurilor de economisire intr-o baza de date in-memory (H2).
- Filtrarea tranzactiilor dupa cont, categorie si interval de timp.
- Generarea de agregari pentru grafice de cheltuieli (total lunar si total pe categorie).
- Administrarea planurilor de economisire: creare, actualizare, contributii si stergere.
- Generarea unui cod QR (imagine PNG codificata Base64) pentru orice payload de plata.

## Pornire (Desktop JavaFX)

Aplicația rulează ca UI desktop JavaFX (fără browser), fără Spring. Persistența este realizată cu JPA/Hibernate + H2, iar legarea componentelor (servicii, repository‑uri) este făcută manual pentru un runtime mai ușor pe desktop.

- Dezvoltare (rulare rapidă):

```bash
mvn -DskipTests javafx:run
```

- Rulare din jar (cu toate dependențele pe classpath):

```bash
mvn -Dmaven.test.skip=true package
java -cp target\multibank-aggregator-0.0.1-SNAPSHOT.jar;target\lib\* com.multibank.desktop.DesktopApp
```

## Installer Windows (EXE) cu jpackage

Există un profil Maven pentru a genera un installer Windows folosind jpackage. Necesită JDK 17 cu jpackage disponibil în `JAVA_HOME`.

1. Build aplicație și dependențe:

```bash
mvn -P installer-windows -DskipTests package
```

2. Găsești installerul la:

```
target/installer/MultiBank Desktop-*.exe
```

## Observații

Aplicația nu mai include controlere REST și nu pornește niciun server web. Interfața JavaFX interacționează direct cu serviciile interne.

## Configurarea API-urilor bancare reale

Configurarea API-urilor bancare reale: URL-urile și cheile pot fi gestionate ulterior. Implicit, dacă nu există URL/cheie, clienții folosesc date demo.


## Testare

```bash
mvn test
```
