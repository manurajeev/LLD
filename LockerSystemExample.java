/*
 +------------------+
|  LockerManager  |
+------------------+
| - emptyLockersMap |
| - usedLockers    |
+------------------+
| + addEmptyLocker() |
| + assignLockerForPackage() |
| + releaseLocker() |
| + cleanupStalePackages() |
+------------------+

+----------------+
|  Locker       |
+----------------+
| - lockerId    |
| - size        |
| - type        |
+----------------+
| + getLockerId() |
| + getSize()     |
| + getType()     |
+----------------+

+----------------+
|  Package      |
+----------------+
| - packageId   |
| - requiredSize|
| - preferredType |
+----------------+
| + getPackageId() |
| + getRequiredSize() |
| + getPreferredType() |
+----------------+
 */

 import java.time.Instant;
 import java.util.*;
 
 /**
  * Enum for locker sizes
  */
 enum LockerSize {
     S, M, L
 }
 
 /**
  * Enum for locker types.
  */
enum LockerType {
    STANDARD, FREEZE, GLASS
}
 
/**
  * Represents a locker with a given size and type.
  */
class Locker {
    private final int lockerId;
     private final LockerSize size;
     private final LockerType type;
 
     public Locker(int lockerId, LockerSize size, LockerType type) {
         this.lockerId = lockerId;
         this.size = size;
         this.type = type;
     }
 
     public int getLockerId() {
         return lockerId;
     }
 
     public LockerSize getSize() {
         return size;
     }
 
     public LockerType getType() {
         return type;
     }
 
     @Override
     public String toString() {
         return "Locker(id=" + lockerId + ", size=" + size + ", type=" + type + ")";
     }
 }
 
 /**
  * Represents a package with a required locker size and possibly a preferred locker type.
  */
 class Package {
     private final String packageId;
     private final LockerSize requiredSize;
     private final LockerType preferredType;
 
     public Package(String packageId, LockerSize requiredSize, LockerType preferredType) {
         this.packageId = packageId;
         this.requiredSize = requiredSize;
         this.preferredType = preferredType;
     }
 
     public String getPackageId() {
         return packageId;
     }
 
     public LockerSize getRequiredSize() {
         return requiredSize;
     }
 
     public LockerType getPreferredType() {
         return preferredType;
     }
 
     @Override
     public String toString() {
         return "Package(id=" + packageId + ", size=" + requiredSize + ", type=" + preferredType + ")";
     }
 }
 
 /**
  * Represents an assigned locker to a package.
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
 
     @Override
     public String toString() {
         return "LockerAssignment(locker=" + locker + ", pkg=" + pkg + ", assignedTime=" + assignedTime + ")";
     }
 }
 
 /**
  * The LockerManager class manages empty and assigned lockers.
  */
 class LockerManager {
     private final Map<KeyForQueue, Deque<Locker>> emptyLockersMap = new HashMap<>();
     private final Map<String, LockerAssignment> usedLockers = new HashMap<>();
 
     private static class KeyForQueue {
         final LockerSize size;
         final LockerType type;
 
         KeyForQueue(LockerSize size, LockerType type) {
             this.size = size;
             this.type = type;
         }
 
         @Override
         public boolean equals(Object o) {
             if (!(o instanceof KeyForQueue)) return false;
             KeyForQueue k = (KeyForQueue) o;
             return k.size == this.size && k.type == this.type;
         }
 
         @Override
         public int hashCode() {
             return Objects.hash(size, type);
         }
     }
 
     public void addEmptyLocker(Locker locker) {
         KeyForQueue key = new KeyForQueue(locker.getSize(), locker.getType());
         emptyLockersMap.putIfAbsent(key, new ArrayDeque<>());
         emptyLockersMap.get(key).offer(locker);
     }
 
     public boolean assignLockerForPackage(Package pkg) {
         LockerSize[] possibleSizes = LockerSize.values();
         int startIndex = Arrays.asList(possibleSizes).indexOf(pkg.getRequiredSize());
 
         for (int i = startIndex; i < possibleSizes.length; i++) {
             LockerSize size = possibleSizes[i];
 
             if (pkg.getPreferredType() != null) {
                 KeyForQueue key = new KeyForQueue(size, pkg.getPreferredType());
                 if (tryAssignFromQueue(key, pkg)) return true;
             } else {
                 for (LockerType type : LockerType.values()) {
                     KeyForQueue key = new KeyForQueue(size, type);
                     if (tryAssignFromQueue(key, pkg)) return true;
                 }
             }
         }
         return false;
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
 
     public void releaseLocker(String packageId) {
         LockerAssignment assignment = usedLockers.remove(packageId);
         if (assignment != null) {
             Locker locker = assignment.getLocker();
             KeyForQueue key = new KeyForQueue(locker.getSize(), locker.getType());
             emptyLockersMap.putIfAbsent(key, new ArrayDeque<>());
             emptyLockersMap.get(key).offer(locker);
         }
     }
 
     public void cleanupStalePackages(Instant threshold) {
         List<String> toRemove = new ArrayList<>();
         for (Map.Entry<String, LockerAssignment> e : usedLockers.entrySet()) {
             if (e.getValue().getAssignedTime().isBefore(threshold)) {
                 toRemove.add(e.getKey());
             }
         }
         for (String pkgId : toRemove) {
             releaseLocker(pkgId);
         }
     }
 }
 
 public class LockerSystemExample {
     public static void main(String[] args) {
         LockerManager manager = new LockerManager();
 
         // Add empty lockers
         manager.addEmptyLocker(new Locker(1, LockerSize.S, LockerType.STANDARD));
         manager.addEmptyLocker(new Locker(2, LockerSize.M, LockerType.FREEZE));
         manager.addEmptyLocker(new Locker(3, LockerSize.L, LockerType.GLASS));
         manager.addEmptyLocker(new Locker(4, LockerSize.XL, LockerType.STANDARD));
 
         // Create packages
         Package pkg1 = new Package("pkgA", LockerSize.M, LockerType.FREEZE);
         Package pkg2 = new Package("pkgB", LockerSize.L, null);
         Package pkg3 = new Package("pkgC", LockerSize.S, LockerType.STANDARD);
         Package pkg4 = new Package("pkgD", LockerSize.XL, null);
 
         // Assign lockers
         System.out.println("Assigned pkg1: " + manager.assignLockerForPackage(pkg1));
         System.out.println("Assigned pkg2: " + manager.assignLockerForPackage(pkg2));
         System.out.println("Assigned pkg3: " + manager.assignLockerForPackage(pkg3));
         System.out.println("Assigned pkg4: " + manager.assignLockerForPackage(pkg4));
 
         // Release a locker
         manager.releaseLocker("pkgA");
 
         // Assign another package after release
         Package pkg5 = new Package("pkgE", LockerSize.S, LockerType.STANDARD);
         System.out.println("Assigned pkg5: " + manager.assignLockerForPackage(pkg5));
 
         // Cleanup stale packages
         manager.cleanupStalePackages(Instant.now().minusSeconds(3600));
     }
 }
 

//python
/*
from collections import deque
from datetime import datetime, timedelta
from enum import Enum
from typing import Dict, Optional


class LockerSize(Enum):
    S = "S"
    M = "M"
    L = "L"
    XL = "XL"


class LockerType(Enum):
    STANDARD = "STANDARD"
    FREEZE = "FREEZE"
    GLASS = "GLASS"


class Locker:
    def __init__(self, locker_id: int, size: LockerSize, locker_type: LockerType):
        self.locker_id = locker_id
        self.size = size
        self.type = locker_type

    def __repr__(self):
        return f"Locker(id={self.locker_id}, size={self.size}, type={self.type})"


class Package:
    def __init__(self, package_id: str, required_size: LockerSize, preferred_type: Optional[LockerType] = None):
        self.package_id = package_id
        self.required_size = required_size
        self.preferred_type = preferred_type  # Can be None if any type is acceptable

    def __repr__(self):
        return f"Package(id={self.package_id}, size={self.required_size}, type={self.preferred_type})"


class LockerAssignment:
    def __init__(self, locker: Locker, pkg: Package):
        self.locker = locker
        self.pkg = pkg
        self.assigned_time = datetime.now()

    def __repr__(self):
        return f"LockerAssignment(locker={self.locker}, pkg={self.pkg}, assigned_time={self.assigned_time})"


        LockerSize, Deque
class LockerManager:
    def __init__(self):
        self.empty_lockers_map: Dict[tuple, deque] = {}  # (size, type) -> Queue of Lockers
        self.used_lockers: Dict[str, LockerAssignment] = {}  # package_id ->  LockerAssignment

    def add_empty_locker(self, locker: Locker):
        key = (locker.size, locker.type)
        if key not in self.empty_lockers_map:
            self.empty_lockers_map[key] = deque()
        self.empty_lockers_map[key].append(locker)

    def assign_locker_for_package(self, pkg: Package) -> bool:
        """
        Tries to assign the smallest available locker that fits the package.
        If a preferred type is specified, it attempts to find a matching locker type first.
        Otherwise, it searches all available locker types.
        """
        possible_sizes = list(LockerSize)  # Ordered from smallest to largest
        start_index = possible_sizes.index(pkg.required_size)  # Start from the required size

        for size in possible_sizes[start_index:]:  # Check required size or larger
            if pkg.preferred_type:
                key = (size, pkg.preferred_type)
                if self._try_assign_from_queue(key, pkg):
                    return True
            else:
                for locker_type in LockerType:  # Try all locker types
                    key = (size, locker_type)
                    if self._try_assign_from_queue(key, pkg):
                        return True
        return False  # No suitable locker found

    def _try_assign_from_queue(self, key: tuple, pkg: Package) -> bool:
        """Attempts to assign a locker from a given queue if available."""
        if key in self.empty_lockers_map and self.empty_lockers_map[key]:
            assigned_locker = self.empty_lockers_map[key].popleft()
            self.used_lockers[pkg.package_id] = LockerAssignment(assigned_locker, pkg)
            return True
        return False

    def release_locker(self, package_id: str):
        """Releases a locker when a package is picked up or expires."""
        if package_id in self.used_lockers:
            assignment = self.used_lockers.pop(package_id)
            key = (assignment.locker.size, assignment.locker.type)
            if key not in self.empty_lockers_map:
                self.empty_lockers_map[key] = deque()
            self.empty_lockers_map[key].append(assignment.locker)

    def cleanup_stale_packages(self, threshold: datetime):
        """Removes packages that have been left unclaimed for too long."""
        to_remove = [
            package_id
            for package_id, assignment in self.used_lockers.items()
            if assignment.assigned_time + threshold > datetime.now()
        ]
        for package_id in to_remove:
            self.release_locker(package_id)


# Example usage:
if __name__ == "__main__":
    manager = LockerManager()

    # Add some empty lockers of different sizes and types
    manager.add_empty_locker(Locker(1, LockerSize.S, LockerType.STANDARD))
    manager.add_empty_locker(Locker(2, LockerSize.M, LockerType.FREEZE))
    manager.add_empty_locker(Locker(3, LockerSize.L, LockerType.GLASS))
    manager.add_empty_locker(Locker(4, LockerSize.XL, LockerType.STANDARD))

    # Create packages with different size requirements
    pkg1 = Package("pkgA", LockerSize.M, LockerType.FREEZE)  # Prefers FREEZE
    pkg2 = Package("pkgB", LockerSize.L)  # No preference, will take any type
    pkg3 = Package("pkgC", LockerSize.S, LockerType.STANDARD)  # Prefers STANDARD
    pkg4 = Package("pkgD", LockerSize.XL)  # Requires XL locker

    # Try assigning lockers
    assigned1 = manager.assign_locker_for_package(pkg1)
    assigned2 = manager.assign_locker_for_package(pkg2)
    assigned3 = manager.assign_locker_for_package(pkg3)
    assigned4 = manager.assign_locker_for_package(pkg4)

    print(f"Assigned pkg1: {assigned1}")
    print(f"Assigned pkg2: {assigned2}")
    print(f"Assigned pkg3: {assigned3}")
    print(f"Assigned pkg4: {assigned4}")

    # Release a locker (pkg1 is picked up)
    manager.release_locker("pkgA")

    # Assign another package after releasing a locker
    pkg5 = Package("pkgE", LockerSize.S, LockerType.STANDARD)
    assigned5 = manager.assign_locker_for_package(pkg5)

    print(f"Assigned pkg5: {assigned5}")

    # Cleanup stale packages older than 1 hour
    manager.cleanup_stale_packages(datetime.now() - timedelta(hours=1))

 */