# Warehouse Simulation Design

**Video link:**  #TO NOT FORGET 

## 1) Overview
This project simulates a concurrent warehouse where deliveries arrive over time, stockers move boxes from a staging area into sections, and pickers remove boxes from sections. Time is modeled in ticks (1000 ticks/day), and each actor thread uses a shared simulation clock to keep timing consistent.

Current implementation status: core simulation loop and logging are in place.

## 2) List of working functionality

| Description | Status | Notes |
|---|---|---|
| Shared tick-based simulation clock | Working | Single `SimulationClock` thread; all actors use `sleepTicks(...)`. |
| Tick duration configurable via JSON | Working | Reads `ms_per_tick` (fallback `tick_time_ms`) with minimum 50 ms. |
| Delivery generation | Working | Delivery thread uses per-tick probability and display `delivery_arrived`. |
| Staging area concurrency | Working | Synchronized updates and synchronized take operations. |
| Multiple stocker threads | Working | Configurable `stockers` count; each stocker carries up to 10 boxes. |
| Picker threads + random fixed section per attempt | Working | Pickers select one section per attempt, log `pick_start` before waiting, and log `pick_done` with `waited_ticks`. |
| Section capacity from config | Working | `Section` now uses per-section `capacity` from JSON. |
| Section lock model (pickers vs stockers) | Partly working | Locking primitives exist, but full end-to-end stocker -> section behavior is not finished. |
| Stocker priority route choice | Partly working | Stockers prioritize by carried load; picker-wait-aware prioritisation not yet implemented. |
| Trolleys | Not working | Not implemented yet. |
| Stocker breaks | Not working | Not implemented yet. |

## 3) Division of work


## 4) Running the code 

1. **Prerequisites**
    - Gradle

2. **Build**
   - From project root:
   - `./gradlew build`

3. **Run**
   - `./gradlew run`

4. **Configuration file**
   - `Warehouse1.json`
   - Important fields:
     - `ms_per_tick`
     - `delivery_per_tick_probability`
     - `boxes_per_delivery`
     - `stockers`, `pickers`
     - `sections[]` with `name`, `starting_capacity`, `capacity`

5. **Expected output format**
   - One event per line.
   - Space-separated `key=value` fields.
   - Includes events such as `delivery_arrived`, `move`, `stock_end`, `pick_start`, `pick_done`.

## 5) Tasks and dependencies

- **SimulationClock**: provides global tick timeline.
- **DeliveryGenerator**: probabilistically creates deliveries and pushes boxes into staging.
- **StagingArea**: shared buffer between deliveries and stockers; synchronized methods enforce mutual exclusion.
- **Stocker threads**: collect up to carry limit, move between locations with tick costs, and perform stocking operations.
- **Section**: holds inventory with lock protection and capacity limit.
- **Picker threads**: choose one section per attempt, wait if empty, then pick one box.
- **Selection strategies**: pluggable classes for picker/stocker section choice.

## 6) Concurrency patterns / strategies used

- Thread-per-actor model (`DeliveryGenerator`, `Stocker`, `Picker`, `SimulationClock`).
- Mutual exclusion in staging area (`synchronized`).
- Section-level lock protection (`ReentrantReadWriteLock`).
- Atomic counters for unique IDs.
- Strategy pattern for section selection logic.

## 7) Fairness, starvation, and progress discussion


## 8) Known limitations and pending work

- Good-tier features missing: trolleys and stocker breaks.

## 9) Short conclusion

The project satisfies core minimal requirements (concurrent actors, probabilistic deliveries, picker behavior, logging, and configurable simulation timing).
