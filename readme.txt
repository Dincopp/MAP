================================================================================
LABORATOR 4 - MAP: REFACTORIZARE JAVA 8 (STREAM API, LAMBDA, OPTIONAL)
================================================================================

1. BUCLE INLOCUITE CU STREAM API (INAINTE / DUPA)
--------------------------------------------------------------------------------
1. NetworkService.addUser
   - Inainte: for (Utilizator u : userRepo.findAll()) if (duplicat) throw ...;
   - Dupa: userRepo.findAll().stream().anyMatch(u -> Objects.equals(...));
   - Operatii: anyMatch

2. NetworkService.generateId
   - Inainte: for (Utilizator u : userRepo.findAll()) if (u.getId() > maxId) maxId = u.getId();
   - Dupa: userRepo.findAll().stream().map(Utilizator::getId).filter(Objects::nonNull).max(Long::compareTo).orElse(0L) + 1L;
   - Operatii: map, filter, max, orElse

3. NetworkService.removeUser
   - Inainte: for (Friendship f : friendshipRepo.findAll()) if (implicat) toRemove.add(f.getId()); for (key) delete(key);
   - Dupa: friendshipRepo.findAll().stream().filter(f -> id.equals(u1) || id.equals(u2)).map(Entity::getId).collect(toList()).forEach(repo::delete);
   - Operatii: filter, map, collect, forEach

4. NetworkService.getFriendsOfUser
   - Inainte: for (Long id : friendIds) userRepo.findOne(id).ifPresent(friends::add);
   - Dupa: friendIds.stream().map(userRepo::findOne).flatMap(Optional::stream).collect(Collectors.toList());
   - Operatii: map, flatMap, collect

5. GraphService.getMostSociableCommunity
   - Inainte: for (Utilizator u : userRepo.findAll()) { int path = dfs/bfs; if (path > max) { max = path; ... } }
   - Dupa: getAllCommunities().stream().max(Comparator.comparingInt(c -> calculateLongestPathInCommunity(c, adj))).orElseGet(Collections::emptyList);
   - Operatii: max, orElseGet

6. AbstractFileRepository.writeToFile
   - Inainte: for (E entity : findAll()) bw.write(createEntityasString(entity));
   - Dupa: List<String> lines = StreamSupport.stream(findAll().spliterator(), false).map(this::createEntityasString).collect(toList()); Files.write(path, lines, UTF_8);
   - Operatii: map, collect, Files.write


2. INTERFETE FUNCTIONALE UTILIZATE
--------------------------------------------------------------------------------
- Validator<E> (@FunctionalInterface): are metoda default andThen(other). In Main, o regula noua de validare (lungime nume >= 2) a fost adaugata doar ca lambda si compusa prin andThen, fara modificarea validatorului existent.
- Predicate<T>: metoda default Repository.findAll(Predicate<E>), clase statice in CriteriiUtilizator (hasLastName, nameStartsWith, hasIdGreaterThan) combinate cu and/or/negate/Predicate.not.
- Function<T, R>: in RepositoryFactory, map-urile userRepoCreators si friendshipRepoCreators elimina switch-ul asociind fiecarui RepositoryType constructorul potrivit.
- Supplier<T>: utilizat la extragerea din Optional via orElseGet / orElseThrow.
- Consumer<T>: in operatiile terminale forEach (stergeri in cascada, afisare).
- Comparator<T>: Comparator.comparingInt in GraphService pentru calculul celei mai sociabile comunitati.


3. REFERINTE LA METODE (CELE 4 TIPURI)
--------------------------------------------------------------------------------
1. Metoda statica: CriteriiUtilizator::faraPrieteni, Objects::nonNull
2. Metoda de instanta pe un obiect particular: System.out::println, this::delete, this::createEntityasString, repo::delete, visited::contains
3. Metoda de instanta pe un obiect arbitrar: Utilizator::getId, Entity::getId, Long::compareTo
4. Constructor: InMemoryRepository::new, FriendshipRepository::new, ArrayList::new


4. ALEGEREA PENTRU OPTIONAL IN REPOSITORY
--------------------------------------------------------------------------------
Metodele findOne, save si delete din Repository intorc Optional<E>.
Justificare: forteaza tratarea explicita a lipsei valorilor sau a conflictelor (evitand NullPointerException) si permite compunerea fluenta in service cu orElseThrow, ifPresent, map si flatMap. Niciun get() neprotejat nu este apelat.


5. TEMPLATE METHOD VS. FUNCTION PE FISIER
--------------------------------------------------------------------------------
S-a pastrat sablonul Template Method (AbstractFileRepository cu subclase).
Justificare: fiecare entitate (Utilizator, Friendship) isi mentine propria responsabilitate si parsare CSV in clasa dedicata, in timp ce scheletul comun de citire (Files.lines + IntStream.range pentru raportarea precisa a liniilor corupte) si scriere (Files.write) este centralizat curat in clasa abstracta.


6. BUCLE PASTRATE SI JUSTIFICARE
--------------------------------------------------------------------------------
Nicio colectie nu mai este parcursa cu bucle clasice. Singurele bucle pastrate sunt:
- while(true) in UI.run() -> bucla de asteptare a comenzilor din consola (event loop).
- while(!queue.isEmpty()) in GraphService.bfsMaxDistance() -> mecanismul de niveluri BFS (in care vecinii sunt insa filtrati si parcursi cu Stream).
================================================================================
