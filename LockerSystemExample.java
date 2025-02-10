import java.time.Instant;
import java.util.*;

/**
 * Enum for locker types.
 */
enum LockerType {
    STANDARD, FREEZE, GLASS
}

/**
 * Represents a locker with a given size, base type, and possibly special attributes.
 */
class Locker {
    private final int size;
    private final LockerType type; // Could be STANDARD, FREEZE, GLASS, etc.
    private final int lockerId;    // Unique ID for the locker

    public Locker(int lockerId, int size, LockerType type) {
        this.lockerId = lockerId;
        this.size = size;
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public LockerType getType() {
        return type;
    }

    public int getLockerId() {
        return lockerId;
    }
}

/**
 * Represents a package with a required locker size and possibly a preferred locker type.
 * For simplicity, assume packages only need a size and optional preference.
 */
class Package {
    private final String packageId;
    private final int requiredSize;
    private final LockerType preferredType; // Could be null if any type is acceptable

    public Package(String packageId, int requiredSize, LockerType preferredType) {
        this.packageId = packageId;
        this.requiredSize = requiredSize;
        this.preferredType = preferredType;
    }

    public String getPackageId() {
        return packageId;
    }

    public int getRequiredSize() {
        return requiredSize;
    }

    public LockerType getPreferredType() {
        return preferredType;
    }
}

/**
 * This class keeps track of an assigned locker info: which package and when it was assigned.
 */
class LockerAssignment {
    private final Locker locker;
    private final Package pkg;
    private final Instant assignedTime;

    public LockerAssignment(Locker locker, Package pkg) {
        this.locker = locker;
        this.pkg = pkg;
        this.assignedTime = Instant.now();
    }

    public Locker getLocker() {
        return locker;
    }

    public Package getPkg() {
        return pkg;
    }

    public Instant getAssignedTime() {
        return assignedTime;
    }
}

/**
 * The main Manager class that maintains multiple queues of empty lockers and 
 * a map of currently used lockers.
 */
class LockerManager {
    // Mapping from (size, type) to a queue of empty lockers
    private final Map<KeyForQueue, Deque<Locker>> emptyLockersMap = new HashMap<>();

    // Used lockers keyed by package ID
    private final Map<String, LockerAssignment> usedLockers = new HashMap<>();

    // A key class to handle size and type combination
    private static class KeyForQueue {
        final int size;
        final LockerType type;

        KeyForQueue(int size, LockerType type) {
            this.size = size;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof KeyForQueue)) return false;
            KeyForQueue k = (KeyForQueue)o;
            return k.size == this.size && k.type == this.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(size, type);
        }
    }

    /**
     * Add an empty locker to the system.
     */
    public void addEmptyLocker(Locker locker) {
        KeyForQueue key = new KeyForQueue(locker.getSize(), locker.getType());
        emptyLockersMap.putIfAbsent(key, new ArrayDeque<>());
        emptyLockersMap.get(key).offer(locker);
    }

    /**
     * Assign a locker to a package.
     * If the package prefers a specific type, we try that first.
     * If "any" is acceptable (null type), we need to find the best possible locker:
     *   - That means scanning through locker types for that size or possibly 
     *     using a strategy: 
     *     Here, we just pick any suitable queue that isn't empty.
     */
    public boolean assignLockerForPackage(Package pkg) {
        // If the package has a preferred type
        if (pkg.getPreferredType() != null) {
            KeyForQueue key = new KeyForQueue(pkg.getRequiredSize(), pkg.getPreferredType());
            if (tryAssignFromQueue(key, pkg)) {
                return true;
            }
            // If not found in preferred type, maybe fallback to standard?
            // This depends on requirements. If strictly needs preferred type, return false here.
            return false;
        } else {
            // Package can accept any type. Try standard first, then others.
            // This logic depends on how we want to prioritize. For now, just check all known types.
            for (LockerType t : LockerType.values()) {
                KeyForQueue key = new KeyForQueue(pkg.getRequiredSize(), t);
                if (tryAssignFromQueue(key, pkg)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean tryAssignFromQueue(KeyForQueue key, Package pkg) {
        Deque<Locker> queue = emptyLockersMap.get(key);
        if (queue != null && !queue.isEmpty()) {
            Locker assignedLocker = queue.poll();
            usedLockers.put(pkg.getPackageId(), new LockerAssignment(assignedLocker, pkg));
            return true;
        }
        return false;
    }

    /**
     * Release a locker when the package is picked up or expired.
     * Add it back to the emptyLockersMap for reuse.
     */
    public void releaseLocker(String packageId) {
        LockerAssignment assignment = usedLockers.remove(packageId);
        if (assignment != null) {
            Locker locker = assignment.getLocker();
            KeyForQueue key = new KeyForQueue(locker.getSize(), locker.getType());
            emptyLockersMap.putIfAbsent(key, new ArrayDeque<>());
            emptyLockersMap.get(key).offer(locker);
        }
    }

    /**
     * Detect stale packages (those not picked up in certain time)
     * If we had a priority data structure or timestamps, we could remove them:
     * For simplicity, not implemented fully here. But you could iterate over usedLockers and 
     * check their assigned times.
     */
    public void cleanupStalePackages(Instant threshold) {
        // pseudo-logic:
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, LockerAssignment> e : usedLockers.entrySet()) {
            if (e.getValue().getAssignedTime().isBefore(threshold)) {
                toRemove.add(e.getKey());
            }
        }
        // Release them after collecting to avoid concurrent modification
        for (String pkgId: toRemove) {
            releaseLocker(pkgId);
        }
    }

    // Additional methods for stats, etc., can be added.
}

// Example usage:
public class LockerSystemExample {
    public static void main(String[] args) {
        LockerManager manager = new LockerManager();
        // Add some empty lockers
        manager.addEmptyLocker(new Locker(1, 10, LockerType.STANDARD));
        manager.addEmptyLocker(new Locker(2, 10, LockerType.FREEZE));
        manager.addEmptyLocker(new Locker(3, 10, LockerType.STANDARD));

        Package pkg1 = new Package("pkgA", 10, LockerType.FREEZE);
        Package pkg2 = new Package("pkgB", 10, null);

        boolean assigned1 = manager.assignLockerForPackage(pkg1);
        boolean assigned2 = manager.assignLockerForPackage(pkg2);

        System.out.println("Assigned pkg1: " + assigned1);
        System.out.println("Assigned pkg2: " + assigned2);

        // Suppose pkg1 picked up now
        manager.releaseLocker("pkgA");

        // Now pkg1's locker is back in queue for reuse
        Package pkg3 = new Package("pkgC", 10, LockerType.STANDARD);
        boolean assigned3 = manager.assignLockerForPackage(pkg3);
        System.out.println("Assigned pkg3: " + assigned3);
    }
}


//python
/*
from collections import deque
from datetime import datetime, timedelta
from enum import Enum
from typing import Dict, Optional


class LockerType(Enum):
    STANDARD = "STANDARD"
    FREEZE = "FREEZE"
    GLASS = "GLASS"


class Locker:
    def __init__(self, locker_id: int, size: int, locker_type: LockerType):
        self.locker_id = locker_id
        self.size = size
        self.type = locker_type

    def __repr__(self):
        return f"Locker(id={self.locker_id}, size={self.size}, type={self.type})"


class Package:
    def __init__(self, package_id: str, required_size: int, preferred_type: Optional[LockerType]):
        self.package_id = package_id
        self.required_size = required_size
        self.preferred_type = preferred_type

    def __repr__(self):
        return f"Package(id={self.package_id}, size={self.required_size}, type={self.preferred_type})"


class LockerAssignment:
    def __init__(self, locker: Locker, pkg: Package):
        self.locker = locker
        self.pkg = pkg
        self.assigned_time = datetime.now()

    def __repr__(self):
        return f"LockerAssignment(locker={self.locker}, pkg={self.pkg}, assigned_time={self.assigned_time})"


class LockerManager:
    def __init__(self):
        # Mapping (size, type) to a queue of empty lockers
        self.empty_lockers_map: Dict[tuple, deque] = {}

        # Used lockers keyed by package ID
        self.used_lockers: Dict[str, LockerAssignment] = {}

    def add_empty_locker(self, locker: Locker):
        key = (locker.size, locker.type)
        if key not in self.empty_lockers_map:
            self.empty_lockers_map[key] = deque()
        self.empty_lockers_map[key].append(locker)

    def assign_locker_for_package(self, pkg: Package) -> bool:
        if pkg.preferred_type:
            key = (pkg.required_size, pkg.preferred_type)
            if self._try_assign_from_queue(key, pkg):
                return True
            return False  # Strict type match required
        else:
            # Try all locker types for the given size
            for locker_type in LockerType:
                key = (pkg.required_size, locker_type)
                if self._try_assign_from_queue(key, pkg):
                    return True
            return False

    def _try_assign_from_queue(self, key: tuple, pkg: Package) -> bool:
        if key in self.empty_lockers_map and self.empty_lockers_map[key]:
            assigned_locker = self.empty_lockers_map[key].popleft()
            self.used_lockers[pkg.package_id] = LockerAssignment(assigned_locker, pkg)
            return True
        return False

    def release_locker(self, package_id: str):
        if package_id in self.used_lockers:
            assignment = self.used_lockers.pop(package_id)
            key = (assignment.locker.size, assignment.locker.type)
            if key not in self.empty_lockers_map:
                self.empty_lockers_map[key] = deque()
            self.empty_lockers_map[key].append(assignment.locker)

    def cleanup_stale_packages(self, threshold: datetime):
        to_remove = [
            package_id
            for package_id, assignment in self.used_lockers.items()
            if assignment.assigned_time < threshold
        ]
        for package_id in to_remove:
            self.release_locker(package_id)


# Example usage:
if __name__ == "__main__":
    manager = LockerManager()

    # Add some empty lockers
    manager.add_empty_locker(Locker(1, 10, LockerType.STANDARD))
    manager.add_empty_locker(Locker(2, 10, LockerType.FREEZE))
    manager.add_empty_locker(Locker(3, 10, LockerType.STANDARD))

    # Create packages
    pkg1 = Package("pkgA", 10, LockerType.FREEZE)
    pkg2 = Package("pkgB", 10, None)  # No preferred type

    # Assign lockers
    assigned1 = manager.assign_locker_for_package(pkg1)
    assigned2 = manager.assign_locker_for_package(pkg2)

    print(f"Assigned pkg1: {assigned1}")
    print(f"Assigned pkg2: {assigned2}")

    # Release a locker
    manager.release_locker("pkgA")

    # Assign a new package
    pkg3 = Package("pkgC", 10, LockerType.STANDARD)
    assigned3 = manager.assign_locker_for_package(pkg3)
    print(f"Assigned pkg3: {assigned3}")

    # Cleanup stale packages
    manager.cleanup_stale_packages(datetime.now() - timedelta(hours=1))

 */