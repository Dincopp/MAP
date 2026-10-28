package SocialNetwork.service;

import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service responsible for graph-related operations on the social network using Stream API,
 * such as calculating the number of communities (connected components)
 * and finding the most sociable community (the component with the longest path / maximum diameter).
 */
public class GraphService {
    private final Repository<Long, Utilizator> userRepo;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepo;

    /**
     * Constructs a {@code GraphService} with user and friendship repositories.
     *
     * @param userRepo       the user repository
     * @param friendshipRepo the friendship repository
     */
    public GraphService(Repository<Long, Utilizator> userRepo, Repository<Tuple<Long, Long>, Friendship> friendshipRepo) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
    }

    /**
     * Builds an adjacency list representation of the network graph using Stream API.
     *
     * @return map from user ID to list of adjacent neighbor user IDs
     */
    private Map<Long, List<Long>> getAdjacencyList() {
        Map<Long, List<Long>> adj = StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .collect(Collectors.toMap(Utilizator::getId, u -> new ArrayList<>()));

        StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .forEach(f -> {
                    Long u1 = f.getIdUser1();
                    Long u2 = f.getIdUser2();
                    if (adj.containsKey(u1) && adj.containsKey(u2)) {
                        adj.get(u1).add(u2);
                        adj.get(u2).add(u1);
                    }
                });
        return adj;
    }

    /**
     * Returns all connected components (communities) of the social network.
     *
     * @return a list of communities, where each community is a list of users
     */
    public List<List<Utilizator>> getAllCommunities() {
        Set<Long> visited = new HashSet<>();
        Map<Long, List<Long>> adj = getAdjacencyList();

        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(u -> !visited.contains(u.getId()))
                .map(u -> {
                    List<Utilizator> community = new ArrayList<>();
                    dfsCollect(u.getId(), visited, community, adj);
                    return community;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total number of communities in the social network.
     * A community represents a connected component in the friendship graph.
     *
     * @return the number of existing communities
     */
    public int getNumberOfCommunities() {
        return getAllCommunities().size();
    }

    /**
     * Determines the most sociable community in the network using Stream API and {@link Comparator#comparingInt}.
     * The most sociable community is the connected component with the longest path (maximum diameter).
     *
     * @return the list of users that compose the most sociable community
     */
    public List<Utilizator> getMostSociableCommunity() {
        Map<Long, List<Long>> adj = getAdjacencyList();
        return getAllCommunities().stream()
                .max(Comparator.comparingInt(community -> calculateLongestPathInCommunity(community, adj)))
                .orElseGet(Collections::emptyList);
    }

    /**
     * Traverses adjacent unvisited nodes using Stream API filtering.
     *
     * @param currentId the current node ID
     * @param visited   the set of visited IDs
     * @param adj       the adjacency list
     */
    private void dfsVisit(Long currentId, Set<Long> visited, Map<Long, List<Long>> adj) {
        visited.add(currentId);
        adj.getOrDefault(currentId, Collections.emptyList()).stream()
                .filter(Predicate.not(visited::contains))
                .forEach(neighborId -> dfsVisit(neighborId, visited, adj));
    }

    /**
     * Collects all users in a connected component using Stream API filtering.
     *
     * @param currentId current node ID
     * @param visited   set of visited node IDs
     * @param community list accumulating the component's users
     * @param adj       adjacency list
     */
    private void dfsCollect(Long currentId, Set<Long> visited, List<Utilizator> community, Map<Long, List<Long>> adj) {
        visited.add(currentId);
        userRepo.findOne(currentId).ifPresent(community::add);
        adj.getOrDefault(currentId, Collections.emptyList()).stream()
                .filter(Predicate.not(visited::contains))
                .forEach(neighborId -> dfsCollect(neighborId, visited, community, adj));
    }

    /**
     * Calculates the longest shortest path (diameter) in a community using Stream API max.
     *
     * @param community the community users
     * @param adj       the adjacency list
     * @return the maximum distance found in the community
     */
    private int calculateLongestPathInCommunity(List<Utilizator> community, Map<Long, List<Long>> adj) {
        return community.stream()
                .mapToInt(u -> bfsMaxDistance(u.getId(), adj))
                .max()
                .orElse(0);
    }

    /**
     * Calculates maximum distance from a start node using BFS traversal with Stream neighbor processing.
     *
     * @param startId the starting node identifier
     * @param adj     the adjacency list
     * @return maximum distance reached from start node
     */
    private int bfsMaxDistance(Long startId, Map<Long, List<Long>> adj) {
        Map<Long, Integer> dist = new HashMap<>();
        Queue<Long> queue = new ArrayDeque<>();

        dist.put(startId, 0);
        queue.add(startId);
        int max = 0;

        while (!queue.isEmpty()) {
            Long curr = queue.poll();
            int currentDist = dist.get(curr);
            if (currentDist > max) {
                max = currentDist;
            }

            adj.getOrDefault(curr, Collections.emptyList()).stream()
                    .filter(Predicate.not(dist::containsKey))
                    .forEach(neighborId -> {
                        dist.put(neighborId, currentDist + 1);
                        queue.add(neighborId);
                    });
        }
        return max;
    }
}
