# ProiectMultiBank

Aplicatie Spring Boot care integreaza conturile de la BCR si Banca Transilvania intr-un singur backend, oferind functionalitati de analiza a cheltuielilor, filtrare a tranzactiilor, planuri de economisire si generare de coduri QR pentru plati.

## Functionalitati principale

- Sincronizarea conturilor si tranzactiilor din mai multe banci folosind clienti dedicati (BCR si BT).
- Stocarea in siguranta a conturilor, tranzactiilor si planurilor de economisire intr-o baza de date in-memory (H2).
- Filtrarea tranzactiilor dupa cont, categorie si interval de timp.
- Generarea de agregari pentru grafice de cheltuieli (total lunar si total pe categorie).
- Administrarea planurilor de economisire: creare, actualizare, contributii si stergere.
- Generarea unui cod QR (imagine PNG codificata Base64) pentru orice payload de plata.

## Pornire (Desktop JavaFX)

Aplicația a fost transformată într-un UI desktop JavaFX (fără browser).

- Dezvoltare (rulare rapidă):

```bash
mvn -DskipTests javafx:run
```

- Rulare din jar (cu toate dependențele pe classpath):

```bash
mvn -DskipTests package
java -cp target\multibank-aggregator-0.0.1-SNAPSHOT.jar;target\lib\* com.multibank.desktop.DesktopApp
```

## Installer Windows (EXE) cu jpackage

Am adăugat un profil Maven pentru a genera un installer Windows folosind jpackage. Necesită JDK 17 cu jpackage disponibil în `JAVA_HOME`.

1. Build aplicație și dependențe:

```bash
mvn -P installer-windows -DskipTests package
```

2. Găsești installerul la:

```
target/installer/MultiBank Desktop-*.exe
```

## Endpoints utile (pentru integrare/diagnostic)

Aplicația desktop folosește direct serviciile interne Spring. API-ul HTTP există în continuare pentru diagnostic sau integrare externă (dacă porniți explicit web-ul), dar UI nu mai depinde de browser.

## Configurarea API-urilor bancare reale

In `src/main/resources/application.yml` puteti seta URL-urile si cheile API pentru BCR si BT:

```yaml
multibank:
  bcr:
    base-url: "https://api.bcr.ro"
    api-key: "token-bcr"
  bt:
    base-url: "https://api.bancatransilvania.ro"
    api-key: "token-bt"
```

Daca valorile sunt goale, aplicatia foloseste date demonstrative pentru a oferi functionalitatile principale fara integrare externa.

## Testare

```bash
mvn test
```
