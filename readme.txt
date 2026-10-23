1. DESCRIERE SI CERINTE FUNCTIONALE
-----------------------------------
Aplicatie Java bazata pe arhitectura pe straturi (Layered Architecture)
pentru gestiunea unei retele de socializare modelata grafic:
- Gestiune Utilizatori: adaugare (ID generat automat incremental),
  stergere (in cascada cu prieteniile aferente), afisare utilizatori.
- Gestiune Prietenii: relatie simetrica/neorientata normalizata prin
  Tuple<Long, Long>, inregistrare data (LocalDateTime), stergere, afisare.
- Statistici Retea (Grafuri):
  * Nr. comunitati: numar componente conexe (algoritm DFS).
  * Cea mai sociabila comunitate: componenta conexa cu cel mai lung drum
    (diametru maxim, calculat prin algoritm BFS).


2. ARHITECTURA PE STRATURI (PACHETE)
------------------------------------
src/
├── domain/       -> Entitati de baza: Entity<ID>, Tuple<E1, E2>,
|                    Utilizator, Friendship
├── Validation/   -> Validare date: Validator<E>, ValidationException,
|                    UtilizatorValidator, FriendshipValidator
├── repository/   -> Persistenta (CRUD generic): Repository<ID, E>,
|                    RepositoryType (enum), RepositoryFactory, RepositoryException
|   ├── memory/   -> InMemoryRepository, FriendshipRepository
|   ├── file/     -> AbstractFileRepository, UtilizatorFileRepository,
|   |                FriendshipFileRepository (data/users.txt, friendships.txt)
|   └── database/ -> UtilizatorDbRepository, FriendshipDbRepository
├── service/      -> Logica aplicatiei: NetworkService, GraphService,
|                    ServiceException
├── ui/           -> Interfata consola: UI
└── main/         -> Punct intrare (Main), resources/config.properties


3. FACTORY PATTERN (DE CE SI CUM S-A FOLOSIT?)
----------------------------------------------
- De ce Factory?
  Pentru decuplarea codului client (clasa Main/Service) de clasele concrete
  de repository. Permite schimbarea mecanismului de stocare (memorie, fisier,
  baza de date) din fisierul `config.properties`, fara a recompila sau
  modifica vreo linie de cod din logica aplicatiei.
- Implementare (`RepositoryFactory`):
  Citeste proprietatea `repository.type` (MEMORY / FILE / DATABASE) din
  `config.properties` si instantiaza polimorfic repository-ul corespunzator:
  * MEMORY   -> InMemoryRepository / FriendshipRepository
  * FILE     -> UtilizatorFileRepository / FriendshipFileRepository
  * DATABASE -> UtilizatorDbRepository / FriendshipDbRepository


4. SABLOANE DE PROIECTARE (DESIGN PATTERNS)
-------------------------------------------
1. Factory (Simple / Parameterized Factory):
   - Clasa: `RepositoryFactory`
   - Scop: Centralizeaza si ascunde instantierea obiectelor repository.
2. Template Method:
   - Clasa: `AbstractFileRepository<ID, E>`
   - Scop: Defineste scheletul algoritmului de persistenta (`loadData`,
     `writeToFile`, `save`, `delete`), delegand subclaselor implementarea
     metodelor abstracte primitive `extractEntity(line)` si
     `createEntityasString(entity)`.
3. Strategy Pattern:
   - Interfata: `Validator<E>`
   - Implementari: `UtilizatorValidator`, `FriendshipValidator`
   - Scop: Permite injectarea si schimbarea la runtime a strategiilor de
     validare in repository, fara a altera logica interna a acestuia.
4. Repository / DAO Pattern:
   - Interfata: `Repository<ID, E>`
   - Scop: Abstractizeaza accesul la date si ofera operatii CRUD standard
     (`findOne`, `findAll`, `save`, `delete`), ascunzand mecanismul de stocare.


5. PRINCIPII DE PROIECTARE (SOLID & OOP)
----------------------------------------
- S (Single Responsibility Principle - SRP):
  Fiecare clasa are o singura responsabilitate bine delimitata:
  * `UtilizatorValidator` valideaza strict entitatea utilizator;
  * `NetworkService` contine doar regulile de business;
  * `GraphService` raspunde exclusiv de algoritmii pe grafuri (DFS/BFS);
  * `UI` interactioneaza exclusiv cu utilizatorul via consola.
- O (Open/Closed Principle - OCP):
  Codul este deschis pentru extindere, dar inchis pentru modificare.
  Adaugarea unui nou mecanism de stocare (ex. XML, DB) necesita doar crearea
  unei noi clase ce implementeaza `Repository`, fara a modifica `Service` sau `UI`.
- L (Liskov Substitution Principle - LSP):
  Orice subclasa (`InMemoryRepository`, `AbstractFileRepository`,
  `UtilizatorFileRepository`) poate substitui interfata `Repository<ID, E>`
  fara a altera corectitudinea comportamentului aplicatiei.
- I (Interface Segregation Principle - ISP):
  Interfetele sunt minimale si specifice (`Repository` defineste strict
  operatiile CRUD, iar `Validator` metoda `validate`).
- D (Dependency Inversion Principle - DIP):
  Modulele de nivel inalt (`NetworkService`, `GraphService`) depind de
  abstraciuni (`Repository<ID, E>`), nu de clase concrete. Dependenta este
  injectata prin constructor (Dependency Injection).
- SoC (Separation of Concerns):
  Separare neta intre Domeniu (Model), Validare, Persistenta, Business Logic si UI.


6. CONFIGURARE SI PERSISTENTA
-----------------------------
Fisier configurare: `src/main/resources/config.properties`
Exemplu:
  repository.type=FILE
  users.file=src/repository/file/data/users.txt
  friendships.file=src/repository/file/data/friendships.txt

Format fisiere CSV (delimitator ';'):
- users.txt:       id;firstName;lastName (ex: 1;Ion;Popescu)
- friendships.txt: idUser1;idUser2;data_creare (ex: 1;2;2026-10-22T14:30:00)


7. MENIU CONSOLA SI TRATARE EXCEPTII
------------------------------------
Optiuni:
  1. Adauga user
  2. Sterge user (stergere in cascada a prieteniilor)
  3. Adauga prieten
  4. Sterge prieten
  5. Nr comunitati (DFS)
  6. Cea mai sociabila comunitate (BFS)
  7. Afiseaza useri
  8. Afiseaza prietenii
  9. Prieteni user (dupa ID)
  0. Exit

Tratare exceptii:
- ValidationException: date invalide (campuri vide, auto-prietenie).
- ServiceException: reguli de business incalcate (duplicat, ID inexistent).
- RepositoryException: erori I/O sau configuratie incorecta.
============================================================