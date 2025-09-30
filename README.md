# ProiectMultiBank

Aplicatie Spring Boot care integreaza conturile de la BCR si Banca Transilvania intr-un singur backend, oferind functionalitati de analiza a cheltuielilor, filtrare a tranzactiilor, planuri de economisire si generare de coduri QR pentru plati.

## Functionalitati principale

- Sincronizarea conturilor si tranzactiilor din mai multe banci folosind clienti dedicati (BCR si BT).
- Stocarea in siguranta a conturilor, tranzactiilor si planurilor de economisire intr-o baza de date in-memory (H2).
- Filtrarea tranzactiilor dupa cont, categorie si interval de timp.
- Generarea de agregari pentru grafice de cheltuieli (total lunar si total pe categorie).
- Administrarea planurilor de economisire: creare, actualizare, contributii si stergere.
- Generarea unui cod QR (imagine PNG codificata Base64) pentru orice payload de plata.

## Pornire

1. Asigurati-va ca aveti instalat Java 17 si Maven.
2. Rulati urmatoarea comanda pentru a porni aplicatia:

```bash
mvn spring-boot:run
```

Aplicatia va porni pe `http://localhost:8080`.

## Endpoints utile

- `POST /api/banks/sync` – sincronizeaza toate conturile si tranzactiile disponibile (foloseste date mock daca nu sunt configurate API-urile reale).
- `GET /api/banks/accounts` – listeaza conturile existente in platforma.
- `GET /api/transactions` – filtreaza tranzactii dupa cont, categorie si interval de timp.
- `GET /api/analytics/monthly-spending` – totalul cheltuielilor pe luni pentru criteriile selectate.
- `GET /api/analytics/category-totals` – totalul cheltuielilor pe categorii pentru criteriile selectate.
- `POST /api/savings` – creeaza un nou plan de economisire.
- `POST /api/savings/{id}/contribute` – adauga o contributie la un plan.
- `POST /api/qr` – genereaza un cod QR pe baza unui payload.

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
