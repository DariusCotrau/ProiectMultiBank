Windows Icon (ICO)
==================

Pentru a avea icon personalizat în installer și shortcut-uri, furnizează un fișier `.ico` și pasează calea către Maven prin proprietatea `jpackage.icon`.

Recomandări:
- Dimensiuni incluse în ICO: 16x16, 32x32, 48x48, 256x256 (format PNG comprimat în ICO)
- Nume fișier: `app.ico` (exemplu)

Cum rulezi cu icon:

EXE:
```
mvn -P installer-windows -DskipTests -Djpackage.icon="${project.basedir}/packaging/windows/app.ico" package
```

MSI:
```
mvn -P installer-msi -DskipTests -Djpackage.icon="${project.basedir}/packaging/windows/app.ico" package
```

Notă: Dacă nu transmiți proprietatea `jpackage.icon`, jpackage va folosi icon implicit (nu personalizat).

