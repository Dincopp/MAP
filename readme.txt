================================================================================
LABORATOR 4 - METODE AVANSATE DE PROGRAMARE (MAP)
REFACTORIZARE JAVA 8: LAMBDA, STREAM API, INTERFETE FUNCTIONALE, OPTIONAL
================================================================================

1. DESCRIERE GENERALA
--------------------------------------------------------------------------------
Laboratorul 4 a realizat refactorizarea integrala a solutiei de la Laboratorul 3
in stilul functional specific Java 8+:
- Eliminarea buclelor clasice (for, for-each, while, do-while) pentru
  parcurgerea colectiilor in pachetele domain, service, repository si UI.
- Utilizarea Stream API cu operatii intermediare (filter, map, flatMap, sorted,
  distinct) si operatii terminale (collect, forEach, anyMatch, max, mapToInt).
- Trecerea la interfete functionale standard (Predicate, Function, Supplier,
  Consumer, Comparator) si crearea unei interfete functionale proprii (@FunctionalInterface).
- Utilizarea celor patru tipuri de referinte la metode.
- Utilizarea si justificarea tipului Optional<E> in interfata Repository si in Service.
- Mentinerea comportamentului functional si a tratarii fisierelor cu linii corupte.


2. TABEL INAINTE / DUPA PENTRU BUCLE INLOCUITE
--------------------------------------------------------------------------------
Mai jos este prezentat tabelul detaliat pentru buclele inlocuite cu Stream API:

+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| Nr | Clasa                      | Metoda                              | Inainte (Laborator 3 - Bucla clasica)      | Dupa (Laborator 4 - Stream API & Operatii)                  |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 1  | NetworkService             | addUser                             | for (Utilizator u : userRepo.findAll())    | StreamSupport.stream(userRepo.findAll().spliterator(),      |
|    |                            |                                     |    if (nume_duplicat) throw ...            |   false).anyMatch(u -> Objects.equals(...))                 |
|    |                            |                                     |                                            | Operatii: anyMatch                                          |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 2  | NetworkService             | generateId                          | long maxId = 0L;                           | StreamSupport.stream(userRepo.findAll().spliterator(),      |
|    |                            |                                     | for (Utilizator u : userRepo.findAll())    |   false).map(Utilizator::getId).filter(Objects::nonNull)    |
|    |                            |                                     |    if (u.getId() > maxId) maxId = ...;     |   .max(Long::compareTo).orElse(0L) + 1L;                    |
|    |                            |                                     | return maxId + 1;                          | Operatii: map, filter, max, orElse                          |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 3  | NetworkService             | removeUser                          | for (Friendship f : friendshipRepo.find...)| StreamSupport.stream(friendshipRepo.findAll().spliterat...)|
|    |                            |                                     |    if (f implicat) toRemove.add(f.getId()) |   .filter(f -> id.equals(...) || ...).map(Entity::getId)    |
|    |                            |                                     | for (Tuple k : toRemove) repo.delete(k);   |   .collect(Collectors.toList()).forEach(repo::delete);      |
|    |                            |                                     |                                            | Operatii: filter, map, collect, forEach                     |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 4  | NetworkService             | getFriendIds                        | for (Friendship f : friendshipRepo.find...)| StreamSupport.stream(friendshipRepo.findAll().spliterat...)|
|    |                            |                                     |    if (userId.equals(u1)) friends.add(u2); |   .filter(f -> userId.equals(...) || ...)                   |
|    |                            |                                     |    else if (userId.equals(u2)) ...         |   .map(f -> userId.equals(u1) ? u2 : u1).collect(toList)    |
|    |                            |                                     |                                            | Operatii: filter, map, collect                              |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 5  | NetworkService             | getFriendsOfUser                    | for (Long friendId : getFriendIds(userId)) | getFriendIds(userId).stream()                              |
|    |                            |                                     |    userRepo.findOne(id).ifPresent(f::add); |   .map(userRepo::findOne).flatMap(Optional::stream)         |
|    |                            |                                     |                                            |   .collect(Collectors.toList());                            |
|    |                            |                                     |                                            | Operatii: map, flatMap, collect                             |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 6  | GraphService               | getAdjacencyList                    | for (Utilizator u : userRepo.findAll()) ...| userRepo.findAll().stream()                                 |
|    |                            |                                     | for (Friendship f : friendshipRepo.find...)|   .collect(Collectors.toMap(Utilizator::getId, u -> new...))|
|    |                            |                                     |    adj.get(u1).add(u2); adj.get(u2).add... | friendshipRepo.findAll().stream().forEach(f -> { ... });    |
|    |                            |                                     |                                            | Operatii: collect(toMap), forEach                           |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 7  | GraphService               | getNumberOfCommunities              | for (Utilizator u : userRepo.findAll())    | getAllCommunities().size() unde getAllCommunities foloseste:|
|    |                            |                                     |    if (!visited.contains(id)) { ... }      | userRepo stream().filter(u -> !visited.contains(u.getId())) |
|    |                            |                                     |                                            |   .map(u -> dfsCollect(...)).collect(Collectors.toList())   |
|    |                            |                                     |                                            | Operatii: filter, map, collect                              |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 8  | GraphService               | getMostSociableCommunity            | for (Utilizator u : userRepo.findAll())    | getAllCommunities().stream()                                |
|    |                            |                                     |    path = calculateLongestPath(...)        |   .max(Comparator.comparingInt(community ->                 |
|    |                            |                                     |    if (path > maxPath) max = ...;          |       calculateLongestPathInCommunity(community, adj)))     |
|    |                            |                                     |                                            |   .orElseGet(Collections::emptyList);                       |
|    |                            |                                     |                                            | Operatii: max, orElseGet                                    |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 9  | GraphService               | calculateLongestPathInCommunity     | for (Utilizator u : community)             | community.stream()                                          |
|    |                            |                                     |    path = Math.max(path, bfs(u.getId()));  |   .mapToInt(u -> bfsMaxDistance(u.getId(), adj))            |
|    |                            |                                     |                                            |   .max().orElse(0);                                         |
|    |                            |                                     |                                            | Operatii: mapToInt, max, orElse                             |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 10 | GraphService               | dfsVisit / dfsCollect / bfs         | for (Long neighborId : neighbors)          | neighbors.stream()                                          |
|    |                            |                                     |    if (!visited.contains(neighborId)) ...  |   .filter(Predicate.not(visited::contains))                 |
|    |                            |                                     |                                            |   .forEach(neighborId -> dfsVisit(...));                    |
|    |                            |                                     |                                            | Operatii: filter, forEach                                   |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 11 | AbstractFileRepository     | loadData                            | BufferedReader br = ...;                   | try (Stream<String> s = Files.lines(path, UTF_8)) { ... }   |
|    |                            |                                     | while ((line = br.readLine()) != null)     | IntStream.range(0, lines.size())                            |
|    |                            |                                     |    lineNumber++; parse(line);              |   .forEach(index -> { parse(lines.get(index)); });          |
|    |                            |                                     |                                            | Operatii: Files.lines, IntStream.range, forEach             |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 12 | AbstractFileRepository     | writeToFile                         | for (E entity : findAll())                 | List<String> lines = StreamSupport.stream(findAll()...      |
|    |                            |                                     |    bw.write(createEntityasString(entity)); |   .map(this::createEntityasString).collect(toList);         |
|    |                            |                                     |                                            | Files.write(path, lines, StandardCharsets.UTF_8);           |
|    |                            |                                     |                                            | Operatii: map, collect, Files.write                         |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 13 | FriendshipRepository       | removeFriendshipsOfUser             | for (Friendship f : findAll())             | findAll().stream().filter(...).map(Entity::getId)           |
|    |                            |                                     |    keys.add(f.getId()); for (k) delete(k); |   .collect(toList()).forEach(this::delete);                 |
|    |                            |                                     |                                            | Operatii: filter, map, collect, forEach                     |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+
| 14 | UI                         | handleShowUsers / ShowFriendships   | for (Utilizator user : users)              | users.forEach(System.out::println);                         |
|    |                            |                                     |    System.out.println(user);               | Operatii: forEach                                           |
+----+----------------------------+-------------------------------------+--------------------------------------------+-------------------------------------------------------------+


3. INTERFETE FUNCTIONALE UTILIZATE SI UNDE
--------------------------------------------------------------------------------
1. Validator<E> (@FunctionalInterface):
   - Adnotata cu @FunctionalInterface; contine metoda default `Validator<E> andThen(Validator<E> other)`.
   - In `Main.java`, regula de validare pentru lungimea minima a numelui (>= 2 caractere)
     este scrisa exclusiv ca expresie lambda si compusa cu `UtilizatorValidator` existent
     fara a modifica clasa `UtilizatorValidator` si fara a crea clase noi:
     `Validator<Utilizator> composed = baseValidator.andThen(u -> { if (u.getFirstName().length() < 2) ... });`

2. java.util.function.Predicate<T>:
   - In `Repository.java`: metoda default `List<E> findAll(Predicate<E> filter)`.
   - In `CriteriiUtilizator.java`: metode statice ce returneaza `Predicate<Utilizator>`:
     * `hasLastName(String lastName)`
     * `hasFirstName(String firstName)`
     * `nameStartsWith(String prefix)`
     * `hasIdGreaterThan(Long minId)`
   - Combinari logice de predicate via `and()`, `or()`, `negate()` si `Predicate.not()`.
   - In `GraphService`: filtrarea nodurilor nevizitate folosind `Predicate.not(visited::contains)`
     si `Predicate.not(dist::containsKey)`.

3. java.util.function.Function<T, R>:
   - In `RepositoryFactory.java`: map-urile `userRepoCreators` si `friendshipRepoCreators`
     asociaza fiecarui `RepositoryType` o functie `Function<Validator<...>, Repository<...>>`.
     Astfel s-a eliminat complet instructiunea `switch`.
   - In Stream API: utilizata pe scara larga in operatiile `.map(...)` si `.flatMap(...)`.

4. java.util.function.Supplier<T>:
   - Utilizata in extragerea valorilor din `Optional`:
     `graph_service.getMostSociableCommunity().orElseGet(Collections::emptyList);`
     si la aruncarea exceptiilor:
     `userRepo.findOne(id).orElseThrow(() -> new ServiceException("User not found"));`

5. java.util.function.Consumer<T>:
   - In operatiile terminale `forEach`:
     `friendshipsToRemove.forEach(friendshipRepo::delete);`
     `users.forEach(System.out::println);`

6. java.util.Comparator<T>:
   - In `GraphService.getMostSociableCommunity`:
     `Comparator.comparingInt(community -> calculateLongestPathInCommunity(community, adj))`


4. REFERINTE LA METODE (CELE 4 TIPURI)
--------------------------------------------------------------------------------
Toate cele 4 tipuri de referinte la metode apar in proiect si sunt utilizate acolo unde aduc claritate:

1. Metoda statica:
   - `CriteriiUtilizator::faraPrieteni`
     Semnatura `boolean (Utilizator)` corespunde perfect interfetei `Predicate<Utilizator>`.
   - `Objects::nonNull` in `NetworkService.generateId`.

2. Metoda de instanta a unui obiect particular (bound instance method):
   - `System.out::println` in `UI.java` la afisarea utilizatorilor si prieteniilor.
   - `visited::contains` in `GraphService` (`Predicate.not(visited::contains)`).
   - `dist::containsKey` in `GraphService` (`Predicate.not(dist::containsKey)`).
   - `this::delete` in `FriendshipRepository.removeFriendshipsOfUser`.
   - `this::createEntityasString` in `AbstractFileRepository.writeToFile`.
   - `friendshipRepo::delete` in `NetworkService.removeUser`.

3. Metoda de instanta a unui obiect arbitrar de un tip dat (unbound instance method):
   - `Utilizator::getId` in extragerea cheilor de utilizator.
   - `Entity::getId` in extragerea ID-ului prieteniilor de sters.
   - `Utilizator::getLastName` in criterii si comparatori.
   - `Long::compareTo` in cautarea ID-ului maxim: `.max(Long::compareTo)`.

4. Constructor:
   - `InMemoryRepository::new` in `RepositoryFactory.java`.
   - `FriendshipRepository::new` in `RepositoryFactory.java`.
   - `ArrayList::new` in `GraphService` (`Collectors.toMap(Utilizator::getId, u -> new ArrayList<>())`).
   - `Utilizator::new` in parsarea entitatilor.


5. ALEGEREA PENTRU OPTIONAL IN REPOSITORY
--------------------------------------------------------------------------------
In interfata `Repository<ID, E>`:
- `Optional<E> findOne(ID id)`
- `Optional<E> save(E entity)`
- `Optional<E> delete(ID id)`

Justificare:
1. Eliminarea erorilor de tip `NullPointerException`: forțează apelantul (stratul Service)
   sa trateze explicit situatia in care o entitate nu exista sau salvarea nu a produs conflict.
2. Compozabilitate functionala: permite inlantuirea eleganta cu metode functionale
   cum ar fi `.orElseThrow(...)`, `.ifPresent(...)`, `.map(...)`, si `.flatMap(Optional::stream)`.
3. Auto-documentare a API-ului: tipul de retur `Optional<E>` exprima clar faptul ca
   valoarea poate lipsi, spre deosebire de o referinta `null` ambigua.
4. Nicio apelare de tip `.get()` "orb" (neprotejat) nu este prezenta in cod.


6. TEMPLATE METHOD VS. FUNCTION IN REPOSITORY-URILE PE FISIER
--------------------------------------------------------------------------------
Alegere: Pastrarea sablonului Template Method (`AbstractFileRepository<ID, E>`),
cu rescrierea scheletului de persistenta folosind Java 8 Stream API si NIO.2.

Justificare:
1. Single Responsibility & Encapsulation: Fiecare entitate (`Utilizator`, `Friendship`)
   isi pastreaza logica specifica de parsare CSV si serializare in propria clasa
   (`UtilizatorFileRepository`, `FriendshipFileRepository`), fara a aglomera apelantul
   sau fabrica de repository-uri cu functii lambda complexe de serializare/deserializare.
2. Separarea algoritmului de persistenta: `AbstractFileRepository` implementeaza
   scheletul invariant de citire prin `Files.lines` si scriere prin `Files.write`,
   delegand doar pasii variabili (`extractEntity`, `createEntityasString`) subclaselor.
3. Tratarea robusta a liniilor corupte: `loadData()` parcurge liniile folosind
   `IntStream.range(0, lines.size())`, raportand cu precizie numarul liniei corupte
   si mesajul exceptiei, conform specificatiilor.


7. BUCLE PASTRATE SI JUSTIFICARE
--------------------------------------------------------------------------------
In conformitate cu cerintele si restrictiile laboratorului, TOATE buclele care
parcurg colectii au fost eliminate si inlocuite cu Stream API.

Singurele structuri repetitive pastrate sunt:
1. `while(true)` in `UI.run()`:
   - Aceasta reprezinta bucla interactiva a meniului in consola (CLI event loop).
   - Nu parcurge o colectie de date, ci asteapta comenzi de la utilizator pana la optiunea 0 (Exit).
2. `while(!queue.isEmpty())` in `GraphService.bfsMaxDistance()`:
   - Aceasta reprezinta structura de control a algoritmului clasic BFS (Breadth-First Search)
     pentru explorarea pe niveluri a cozii de distante.
   - Explorarea tuturor vecinilor unui nod extras din coada a fost insa refactorizata
     complet functional prin Stream API (`filter(Predicate.not(dist::containsKey)).forEach(...)`).


8. RULARE SI TESTARE
--------------------------------------------------------------------------------
Proiectul se compileaza si ruleaza cu Java 17+ (testat pe Java 25):
- Rulare teste automate:
  Clasa `SocialNetwork.TestRunner` executa testele unitare din `SocialNetwork.Lab4Test`
  verificand validatoarele compuse, cautarile cu Predicate/Criterii, comportamentul
  stream-urilor din NetworkService si GraphService, si instantierea din RepositoryFactory.
- Rulare interfata:
  Clasa `SocialNetwork.main.Main`.
- Generare documentatie Javadoc in directorul `Javadoc/`.
================================================================================
