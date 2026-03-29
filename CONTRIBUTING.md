# Contribuer à HireHub

1. Forkez ou clonez [HireHub---PROJET-JEE](https://github.com/anadouae/HireHub---PROJET-JEE).
2. Créez une branche depuis `main`.
3. Installez **JDK 17** et définissez **`JAVA_HOME`** (obligatoire pour `mvnw` sous Windows).
4. À la racine, compilez avec le **Maven Wrapper** (aucune installation Maven globale requise) :
   - Windows : `.\mvnw.cmd -q -DskipTests package`
   - Linux / macOS : `./mvnw -q -DskipTests package`
5. Ouvrez une **pull request** avec une description claire (module touché, comportement, captures si UI).

Voir aussi `docs/COLLABORATION.md` et `docs/ARCHITECTURE.md`.
