import java.util.*;

public class PackageInstaller {
    private Map<String, List<String>> adjacencyMap; // Could be in a separate class

    public PackageInstaller() {
        adjacencyMap = new HashMap<>();
    }

    public void addDependency(String pkg, String dep) {
        adjacencyMap.putIfAbsent(pkg, new ArrayList<>());
        adjacencyMap.get(pkg).add(dep);
        // Also ensure dep is at least in adjacencyMap so we know about it
        adjacencyMap.putIfAbsent(dep, new ArrayList<>());
    }

    public List<String> getInstallationOrder(String packageName) throws Exception {
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();
        List<String> result = new ArrayList<>();
        
        dfs(packageName, visited, inStack, result);

        // 'result' is in reverse order if we add at the end of DFS
        // reverse it or build it in a consistent topological order
        Collections.reverse(result);
        return result;
    }

    private void dfs(String pkg, Set<String> visited, Set<String> inStack, List<String> result) throws Exception {
        if (inStack.contains(pkg)) {
            throw new Exception("Cycle detected - no valid install order for " + pkg);
        }
        if (visited.contains(pkg)) {
            // Already fully processed
            return;
        }

        // Mark start of recursion for pkg
        inStack.add(pkg);

        // Get dependencies
        List<String> deps = adjacencyMap.getOrDefault(pkg, new ArrayList<>());
        for (String dep : deps) {
            dfs(dep, visited, inStack, result);
        }

        // Mark end of recursion for pkg
        inStack.remove(pkg);
        visited.add(pkg);
        
        // Add this pkg to the ordering
        result.add(pkg);
    }
}


class Main {
    public static void main(String[] args) {
        try {
            PackageInstaller installer = new PackageInstaller();

            // For example, A depends on B and C
            installer.addDependency("A", "B");
            installer.addDependency("A", "C");

            // B depends on D, E, F
            installer.addDependency("B", "D");
            installer.addDependency("B", "E");
            installer.addDependency("B", "F");

            // C depends on F
            installer.addDependency("C", "F");

            // F depends on G
            installer.addDependency("F", "G");

            // H depends on I, J
            installer.addDependency("H", "I");
            installer.addDependency("H", "J");

            // J depends on G
            installer.addDependency("J", "G");

            // Installation order for A
            List<String> orderForA = installer.getInstallationOrder("A");
            System.out.println("Install order for A: " + orderForA);

            // e.g., possible valid sequence: [G, F, C, E, D, B, A]
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}



/*
Time Complexity:
Building the adjacency list is O(E) where E is the number of edges (dependencies).
The DFS approach visits each node and edge exactly once, yielding O(V + E) overall, where V is the number of packages (nodes) and E is the number of dependencies (edges).
Space Complexity:
We store the adjacency map, which is O(V + E).
The recursion stack and visited sets are O(V).
Hence, total space is also O(V + E).

. Incremental or Repeated Calls
Q: “What if the user requests the installation order for Package A multiple times, or for different packages repeatedly? Can we optimize repeated calls?”

A:

We could cache partial or complete topological sorts. For example, once we compute the installation order for a set of dependencies, we could store that result. If the same query (for the same package) comes again, we return the cached sequence.
If dependencies change or new packages are added, we’d need to invalidate or recompute the cache.

Versioning or Multiple Versions of the Same Package
Q: “How would you handle versioned dependencies (e.g., A depends on B=1.0, but C depends on B=2.0)?”

A:

We could treat each version as a separate node in the graph. For instance, B=1.0 and B=2.0 are distinct nodes. That way, the adjacency map might have edges like A → B=1.0, C → B=2.0.
This can get more complex if multiple versions can’t coexist or have partial overlap in files, etc., but conceptually, you’d treat them as distinct packages with their own dependencies.
 
┌────────────────────────────────────────┐
│           PackageInstaller            │
│────────────────────────────────────────│
│ - dependencyGraph: DependencyGraph    │
│────────────────────────────────────────│
│ + PackageInstaller()                  │
│ + addDependency(pkg: String, dep: String): void
│ + getInstallationOrder(pkg: String): List<String>
└────────────────────────────────────────┘
                 ▲
                 │ has
                 │
┌────────────────────────────────────────┐
│         DependencyGraph (optional)    │
│────────────────────────────────────────│
│ - adjacencyMap: Map<String, List<String>> 
│────────────────────────────────────────│
│ + addDependency(pkg: String, dep: String): void
│ + getDependencies(pkg: String): List<String>
└────────────────────────────────────────┘

*/