/*
┌───────────────────────────────┐          ┌───────────────────────────┐
│          ParkingLot           │ <> ------│          Address          │
│-------------------------------│          │---------------------------│
│ - nameOfParkingLot: String    │          │ - street: String          │
│ - address: Address            │          │ - block: String           │
│ - parkingFloors: List<ParkingFloor>     │ - city: String            │
│-------------------------------│          │ - state: String           │
│ + getInstance(...) : ParkingLot         │ - country: String         │
│ + assignTicket(vehicle: Vehicle) : Ticket                          │
│ + scanAndPay(ticket: Ticket) : double                              │
└───────────────────────────────┘          └───────────────────────────┘
        1..*  │
            │
            ▼
┌────────────────────────────────────────┐
│             ParkingFloor              │
│---------------------------------------│
│ - name: String                        │
│ - parkingSlots: Map<ParkingSlotType,  │
│                  Map<String,ParkingSlot>> 
│---------------------------------------│
│ + getRelevantSlotForVehicleAndPark(   │
│       vehicle: Vehicle) : ParkingSlot │
└────────────────────────────────────────┘
            1..*
                │
                ▼
┌──────────────────────────────────┐
│           ParkingSlot           │
│---------------------------------│
│ - name: String                  │
│ - isAvailable: boolean          │
│ - vehicle: Vehicle              │
│ - parkingSlotType: ParkingSlotType
│---------------------------------│
│ + addVehicle(vehicle: Vehicle)  │
│ + removeVehicle(vehicle: Vehicle) │
└──────────────────────────────────┘
           ▲
           │ 0..1
           │
┌───────────────────────────────────┐
│            Vehicle               │
│----------------------------------│
│ - vehicleNumber: String          │
│ - vehicleCategory: VehicleCategory
│----------------------------------│
└───────────────────────────────────┘


┌─────────────────────────────────────┐
│              Ticket                │
│-------------------------------------│
│ - ticketNumber: String             │
│ - startTime: long                  │
│ - endTime: long                    │
│ - vehicle: Vehicle                 │
│ - parkingSlot: ParkingSlot         │
│-------------------------------------│
│ + createTicket(v: Vehicle,         │
│       slot: ParkingSlot): Ticket   │
└─────────────────────────────────────┘


┌───────────────────────────────────┐    ┌───────────────────────────────────┐
│      VehicleCategory (enum)      │    │     ParkingSlotType (enum)       │
│----------------------------------│    │----------------------------------│
│ - TwoWheeler                     │    │ - TwoWheeler                     │
│ - Hatchback                      │    │ - Compact                        │
│ - Sedan                          │    │ - Medium                         │
│ - SUV                            │    │ - Large                          │
│ - Bus                            │    │----------------------------------│
└───────────────────────────────────┘    │ + getPriceForParking(duration): double
                                       └───────────────────────────────────┘

 */

// @Getter
// @Setter
// @Builder

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Address {
    String street;
    String block;
    String city;
    String state;
    String country;
}

enum ParkingSlotType {
    TwoWheeler{
        public double getPriceForParking(long duration){
            return duration*0.05;
        }
    },
    Compact{
        public double getPriceForParking(long duration){
            return duration*0.075;
        }
    },
    Medium{
        public double getPriceForParking(long duration){
            return duration*0.09;
        }
    },
    Large{
        public double getPriceForParking(long duration){
            return duration*0.10;
        }
    };

   public abstract double getPriceForParking(long duration);
}

// @Builder
// @Getter
// @Setter
class Ticket {
    String ticketNumber;
    long startTime;
    long endTime;
    Vehicle vehicle;
    ParkingSlot parkingSlot;

    public static Ticket createTicket(Vehicle vehicle,ParkingSlot parkingSlot){
        return Ticket.builder()
                .parkingSlot(parkingSlot)
                .startTime(System.currentTimeMillis())
                .vehicle(vehicle)
                .ticketNumber(vehicle.getVehicleNumber()+System.currentTimeMillis())
                .build();
    }
}

// @Setter
// @Getter
// @EqualsAndHashCode
class Vehicle {
    String vehicleNumber;
    VehicleCategory vehicleCategory;
}

enum VehicleCategory {
    TwoWheeler,
    Hatchback,
    Sedan,
    SUV,
    Bus
}


class ParkingFloor {
    String name;
    Map<ParkingSlotType,Map<String,ParkingSlot>> parkingSlots;
    public ParkingFloor(String name , Map<ParkingSlotType,Map<String,ParkingSlot>> parkingSlots) {
        this.name=name;
        this.parkingSlots = parkingSlots;
    }

    public ParkingSlot getRelevantSlotForVehicleAndPark(Vehicle vehicle) {
        VehicleCategory vehicleCategory = vehicle.getVehicleCategory();
        ParkingSlotType parkingSlotType = pickCorrectSlot(vehicleCategory);
        Map<String,ParkingSlot> relevantParkingSlot = parkingSlots.get(parkingSlotType);
        ParkingSlot slot =null ;
        for(Map.Entry<String,ParkingSlot> m : relevantParkingSlot.entrySet()){
            if(m.getValue().isAvailable) {
                slot = m.getValue();
                slot.addVehicle(vehicle);
                break;
            }
        }

        return slot;
    }

    private ParkingSlotType pickCorrectSlot(VehicleCategory vehicleCategory) {
        if(vehicleCategory.equals(VehicleCategory.TwoWheeler)) return ParkingSlotType.TwoWheeler;
        else if(vehicleCategory.equals(VehicleCategory.Hatchback) || vehicleCategory.equals(VehicleCategory.Sedan)) return ParkingSlotType.Compact;
        else if(vehicleCategory.equals(VehicleCategory.SUV)) return ParkingSlotType.Medium;
        else if(vehicleCategory.equals(VehicleCategory.Bus)) return ParkingSlotType.Large;

        return null;
    }
}

// @Getter
// @Setter
class ParkingSlot {
    String name;
    // @Builder.Default
    boolean isAvailable = true;
    Vehicle vehicle;
    ParkingSlotType parkingSlotType;

    public ParkingSlot(String name, ParkingSlotType parkingSlotType) {
        this.name = name;
        this.parkingSlotType = parkingSlotType;
    }

    protected void addVehicle(Vehicle vehicle){
        this.vehicle = vehicle;
        this.isAvailable=false;
    }

    protected void removeVehicle(Vehicle vehicle){
        this.vehicle=null;
        this.isAvailable=true;
    }
}

// @Getter
// @Setter
class ParkingLot {
    private String nameOfParkingLot;
    private Address address;
    private List<ParkingFloor> parkingFloors;
    private static ParkingLot parkingLot=null;

    private  ParkingLot(String nameOfParkingLot, Address address, List<ParkingFloor> parkingFloors) {
        this.nameOfParkingLot = nameOfParkingLot;
        this.address = address;
        this.parkingFloors = parkingFloors;
    }

    public static ParkingLot getInstance (String nameOfParkingLot, Address address, List<ParkingFloor> parkingFloors) {
        if(parkingLot == null){
            parkingLot = new ParkingLot(nameOfParkingLot,address,parkingFloors);
        }
        return parkingLot;
    }

    public void addFloors(String name, Map<ParkingSlotType, Map<String,ParkingSlot>> parkSlots){
        ParkingFloor parkingFloor = new ParkingFloor(name,parkSlots);
        parkingFloors.add(parkingFloor);
    }

    public void removeFloors(ParkingFloor parkingFloor){
        parkingFloors.remove(parkingFloor);
    }

    public Ticket assignTicket(Vehicle vehicle){
        //to assign ticket we need parking slot for this vehicle
        ParkingSlot parkingSlot = getParkingSlotForVehicleAndPark(vehicle);
        if(parkingSlot == null) return null;
        Ticket parkingTicket = createTicketForSlot(parkingSlot,vehicle);
        //persist ticket to database
        return parkingTicket;
    }

    public double scanAndPay(Ticket ticket){
        long endTime = System.currentTimeMillis();
        ticket.getParkingSlot().removeVehicle(ticket.getVehicle());
        int duration = (int) (endTime-ticket.getStartTime())/1000;
        double price = ticket.getParkingSlot().getParkingSlotType().getPriceForParking(duration);
        //persist record to database
        return price;
    }

    private Ticket createTicketForSlot(ParkingSlot parkingSlot, Vehicle vehicle) {
        return Ticket.createTicket(vehicle,parkingSlot);
    }

    private ParkingSlot getParkingSlotForVehicleAndPark(Vehicle vehicle) {
        ParkingSlot parkingSlot=null;
        for(ParkingFloor floor : parkingFloors){
            parkingSlot = floor.getRelevantSlotForVehicleAndPark(vehicle);
            if(parkingSlot!= null) break;
        }
        return parkingSlot;
    }

}

public class ParkingLotMain {
    public static void main(String[] args) throws InterruptedException {
        String nameOfParkingLot ="Pintosss Parking Lot";
        Address address = Address.builder().city("Bangalore").country("India").state("KA").build();
        Map<ParkingSlotType, Map<String,ParkingSlot>>  allSlots = new HashMap<>();
        Map<String,ParkingSlot> compactSlot = new HashMap<>();
        compactSlot.put("C1",new ParkingSlot("C1",ParkingSlotType.Compact));
        compactSlot.put("C2",new ParkingSlot("C2",ParkingSlotType.Compact));
        compactSlot.put("C3",new ParkingSlot("C3",ParkingSlotType.Compact));
        allSlots.put(ParkingSlotType.Compact,compactSlot);

        Map<String,ParkingSlot> largeSlot = new HashMap<>();
        largeSlot.put("L1",new ParkingSlot("L1",ParkingSlotType.Large));
        largeSlot.put("L2",new ParkingSlot("L2",ParkingSlotType.Large));
        largeSlot.put("L3",new ParkingSlot("L3",ParkingSlotType.Large));
        allSlots.put(ParkingSlotType.Large,largeSlot);
        ParkingFloor parkingFloor = new ParkingFloor("1",allSlots);
        List<ParkingFloor> parkingFloors = new ArrayList<>();
        parkingFloors.add(parkingFloor);
        ParkingLot parkingLot = ParkingLot.getInstance(nameOfParkingLot,address,parkingFloors);

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleCategory(ParkingLotMain.Hatchback);
        vehicle.setVehicleNumber("KA-01-MA-9999");

        Ticket ticket = parkingLot.assignTicket(vehicle);
        System.out.println(" ticket number >> "+ticket.getTicketNumber());
        //persist the ticket to db here
        Thread.sleep(10000);
        double price = parkingLot.scanAndPay(ticket);
        System.out.println("price is >>" + price);
    }
}