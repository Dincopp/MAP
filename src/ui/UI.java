package ui;

import domain.Friendship;
import domain.Utilizator;
import repository.RepositoryException;
import service.GraphService;
import service.NetworkService;
import service.ServiceException;
import Validation.ValidationException;

import java.util.List;
import java.util.Scanner;

/**
 * User interface component providing a console-based menu for interacting with the social network.
 */
public class UI {
    private final NetworkService service;
    private final GraphService graph_service;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Constructs a {@code UI} instance with the specified network and graph services.
     *
     * @param service       the network service managing users and friendships
     * @param graph_service the graph service computing community statistics
     */
    public UI(NetworkService service, GraphService graph_service) {
        this.service = service;
        this.graph_service = graph_service;
    }

    private void printMenu() {
        System.out.println("\n===== MENIU SOCIAL NETWORK =====");
        System.out.println("1. Adauga user");
        System.out.println("2. Sterge user");
        System.out.println("3. Adauga prieten");
        System.out.println("4. Sterge prieten");
        System.out.println("5. Nr comunitati");
        System.out.println("6. Cea mai sociabila comunitate");
        System.out.println("7. Afiseaza useri");
        System.out.println("8. Afiseaza prietenii");
        System.out.println("9. Prieteni user");
        System.out.println("0. Exit");
        System.out.print("Optiune: ");
    }

    /**
     * Starts the interactive console menu loop.
     */
    public void run() {
        while (true) {
            printMenu();
            String opt = scanner.nextLine().trim();

            if (opt.equals("0")) {
                System.out.println("La revedere!");
                break;
            }

            try {
                switch (opt) {
                    case "1" -> handleAddUser();
                    case "2" -> handleDeleteUser();
                    case "3" -> handleAddFriendship();
                    case "4" -> handleDeleteFriendship();
                    case "5" -> handleNrComunitati();
                    case "6" -> handleCeaMaiSociabilaComunitate();
                    case "7" -> handleShowUsers();
                    case "8" -> handleShowFriendships();
                    case "9" -> handleShowUserFriends();
                    default -> System.out.println("Optiune invalida! Va rugam alegeti o optiune intre 0 si 9.");
                }
            } catch (ValidationException e) {
                System.out.println("Eroare de validare: " + e.getMessage());
            } catch (ServiceException e) {
                System.out.println("Eroare logica (Service): " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Eroare la stocare: " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("Cauza interna: " + e.getCause().getMessage());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Parametru nepermis: " + e.getMessage());
            }
        }
    }

    private void handleAddUser() {
        System.out.print("Prenume: ");
        String fn = scanner.nextLine().trim();
        System.out.print("Nume: ");
        String ln = scanner.nextLine().trim();

        Utilizator u = new Utilizator(fn, ln);
        service.addUser(u);
        System.out.println("Utilizator salvat cu succes!");
    }

    private void handleDeleteUser() {
        System.out.print("ID user: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        service.removeUser(id);
        System.out.println("Utilizator sters cu succes!");
    }

    private void handleAddFriendship() {
        System.out.print("ID user 1: ");
        Long id1 = Long.parseLong(scanner.nextLine().trim());
        System.out.print("ID user 2: ");
        Long id2 = Long.parseLong(scanner.nextLine().trim());

        service.addFriendship(id1, id2);
        System.out.println("Prietenie adaugata cu succes!");
    }

    private void handleDeleteFriendship() {
        System.out.print("ID user 1: ");
        Long id1 = Long.parseLong(scanner.nextLine().trim());
        System.out.print("ID user 2: ");
        Long id2 = Long.parseLong(scanner.nextLine().trim());

        service.removeFriendship(id1, id2);
        System.out.println("Prietenie stearsa cu succes!");
    }

    private void handleNrComunitati() {
        int nr = graph_service.getNumberOfCommunities();
        System.out.println("Numar comunitati: " + nr);
    }

    private void handleCeaMaiSociabilaComunitate() {
        System.out.println("Cea mai sociabila comunitate:");
        List<Utilizator> community = graph_service.getMostSociableCommunity();
        if (community == null || community.isEmpty()) {
            System.out.println("Nu exista nicio comunitate formata.");
        } else {
            community.forEach(System.out::println);
        }
    }

    private void handleShowUsers() {
        System.out.println("Lista utilizatori:");
        Iterable<Utilizator> users = service.getAllUsers();
        boolean empty = true;
        for (Utilizator user : users) {
            System.out.println(user);
            empty = false;
        }
        if (empty) {
            System.out.println("Nu exista utilizatori inregistrati.");
        }
    }

    private void handleShowFriendships() {
        System.out.println("Lista prietenii:");
        Iterable<Friendship> friendships = service.getAllFriendships();
        boolean empty = true;
        for (Friendship f : friendships) {
            System.out.println(f);
            empty = false;
        }
        if (empty) {
            System.out.println("Nu exista prietenii inregistrate.");
        }
    }

    private void handleShowUserFriends() {
        System.out.print("ID user: ");
        Long id = Long.parseLong(scanner.nextLine().trim());

        List<Utilizator> friends = service.getFriendsOfUser(id);
        if (friends == null || friends.isEmpty()) {
            System.out.println("Utilizatorul nu are niciun prieten.");
        } else {
            System.out.println("Prietenii utilizatorului sunt:");
            friends.forEach(System.out::println);
        }
    }
}
