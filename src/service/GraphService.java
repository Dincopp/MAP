package service;

import domain.Friendship;
import domain.Tuple;
import domain.Utilizator;
import repository.Repository;

import java.util.*;

/**
 * Service responsible for graph-related operations on the social network,
 * such as calculating the number of communities (connected components)
 * and finding the most sociable community (the component with the longest path).
 */
public class GraphService {
    private Repository<Long, Utilizator> userRepo;
    private Repository<Tuple<Long, Long>, Friendship> friendshipRepo;

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

    private Map<Long, List<Long>> getAdjacencyList() {
        Map<Long, List<Long>> adj = new HashMap<>();
        for (Utilizator u : userRepo.findAll()) {
            adj.put(u.getId(), new ArrayList<>());
        }
        for (Friendship f : friendshipRepo.findAll()) {
            Long u1 = f.getIdUser1();
            Long u2 = f.getIdUser2();
            if (adj.containsKey(u1) && adj.containsKey(u2)) {
                adj.get(u1).add(u2);
                adj.get(u2).add(u1);
            }
        }
        return adj;
    }

    /**
     * Calculates the total number of communities in the social network.
     * A community represents a connected component in the friendship graph.
     *
     * @return the number of existing communities
     */
    public int getNumberOfCommunities() {
        Set<Long> visited = new HashSet<>();
        int count = 0;
        Map<Long, List<Long>> adj = getAdjacencyList();

        for (Utilizator u : userRepo.findAll()) {
            if (!visited.contains(u.getId())) {
                count++;
                dfsVisit(u.getId(), visited, adj);
            }
        }
        return count;
    }

    /**
     * Determines the most sociable community in the network.
     * The most sociable community is the connected component with the longest path (maximum diameter).
     *
     * @return the list of users that compose the most sociable community
     */
    public List<Utilizator> getMostSociableCommunity() {
        Set<Long> visited = new HashSet<>();
        List<Utilizator> maxCommunity = new ArrayList<>();
        int maxPath = -1;
        Map<Long, List<Long>> adj = getAdjacencyList();

        for (Utilizator u : userRepo.findAll()) {
            if (!visited.contains(u.getId())) {
                List<Utilizator> currentCommunity = new ArrayList<>();
                dfsCollect(u.getId(), visited, currentCommunity, adj);

                int longestPath = calculateLongestPathInCommunity(currentCommunity, adj);
                if (longestPath > maxPath) {
                    maxPath = longestPath;
                    maxCommunity = currentCommunity;
                }
            }
        }
        return maxCommunity;
    }

    private void dfsVisit(Long currentId, Set<Long> visited, Map<Long, List<Long>> adj) {
        visited.add(currentId);
        List<Long> neighbors = adj.getOrDefault(currentId, Collections.emptyList());
        for (Long neighborId : neighbors) {
            if (!visited.contains(neighborId)) {
                dfsVisit(neighborId, visited, adj);
            }
        }
    }

    private void dfsCollect(Long currentId, Set<Long> visited, List<Utilizator> community, Map<Long, List<Long>> adj) {
        visited.add(currentId);
        userRepo.findOne(currentId).ifPresent(community::add);
        List<Long> neighbors = adj.getOrDefault(currentId, Collections.emptyList());
        for (Long neighborId : neighbors) {
            if (!visited.contains(neighborId)) {
                dfsCollect(neighborId, visited, community, adj);
            }
        }
    }

    private int calculateLongestPathInCommunity(List<Utilizator> community, Map<Long, List<Long>> adj) {
        int longestPath = 0;
        for (Utilizator u : community) {
            longestPath = Math.max(longestPath, bfsMaxDistance(u.getId(), adj));
        }
        return longestPath;
    }

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

            for (Long neighborId : adj.getOrDefault(curr, Collections.emptyList())) {
                if (!dist.containsKey(neighborId)) {
                    dist.put(neighborId, currentDist + 1);
                    queue.add(neighborId);
                }
            }
        }
        return max;
    }
}
